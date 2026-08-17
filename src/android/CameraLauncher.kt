package com.christian.customcamera

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import androidx.core.content.FileProvider
import org.apache.cordova.CallbackContext
import org.apache.cordova.CordovaPlugin
import org.json.JSONArray
import java.io.ByteArrayOutputStream
import java.io.File

class CameraLauncher : CordovaPlugin() {

    private var callbackContext: CallbackContext? = null
    private val REQUEST_IMAGE_CAPTURE = 1001
    private val CAMERA_PERMISSION_REQ_CODE = 2001
    private var photoFile: File? = null

    override fun execute(
        action: String,
        args: JSONArray,
        callbackContext: CallbackContext
    ): Boolean {
        this.callbackContext = callbackContext

        if ("takePicture" == action) {
            if (cordova.hasPermission(Manifest.permission.CAMERA)) {
                launchCamera()
            } else {
                cordova.requestPermission(this, CAMERA_PERMISSION_REQ_CODE, Manifest.permission.CAMERA)
            }
            return true
        }

        callbackContext.error("Action not recognized: $action")
        return false
    }

    private fun launchCamera() {
        cordova.activity.runOnUiThread {
            try {
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                
                // Create temporary file for full-size photo
                val context = cordova.activity.applicationContext
                val storageDir = context.cacheDir
                photoFile = File.createTempFile("captured_image_", ".jpg", storageDir)

                val photoURI: Uri = FileProvider.getUriForFile(
                    context,
                    context.packageName + ".provider",
                    photoFile!!
                )

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                cordova.startActivityForResult(this, takePictureIntent, REQUEST_IMAGE_CAPTURE)
            } catch (e: Exception) {
                callbackContext?.error("Failed to launch camera: " + e.message)
            }
        }
    }

    override fun onRequestPermissionResult(
        requestCode: Int,
        permissions: Array<out String>?,
        grantResults: IntArray?
    ) {
        if (requestCode == CAMERA_PERMISSION_REQ_CODE) {
            if (grantResults != null && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera()
            } else {
                callbackContext?.error("Camera permission denied.")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == Activity.RESULT_OK && photoFile != null && photoFile!!.exists()) {
                try {
                    // Load the full-size original photo from disk
                    val fullBitmap = BitmapFactory.decodeFile(photoFile!!.absolutePath)

                    if (fullBitmap != null) {
                        val byteArrayOutputStream = ByteArrayOutputStream()
                        
                        // Compress quality (90% quality JPEG)
                        fullBitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)
                        val byteArray = byteArrayOutputStream.toByteArray()
                        val base64Image = Base64.encodeToString(byteArray, Base64.NO_WRAP)

                        // Clean up temporary file
                        photoFile?.delete()

                        callbackContext?.success("data:image/jpeg;base64,$base64Image")
                    } else {
                        callbackContext?.error("Failed to decode full resolution image.")
                    }
                } catch (e: Exception) {
                    callbackContext?.error("Error processing full image: " + e.message)
                }
            } else {
                callbackContext?.error("Camera action cancelled.")
            }
        }
    }
}
