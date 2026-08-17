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
                val context = cordova.activity.applicationContext
                val storageDir = context.cacheDir
                photoFile = File.createTempFile("captured_image_", ".jpg", storageDir)

                val photoURI: Uri = FileProvider.getUriForFile(
                    context,
                    context.packageName + ".provider",
                    photoFile!!
                )

                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

                // Grant temporary write permissions to camera activity
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

                if (takePictureIntent.resolveActivity(context.packageManager) != null) {
                    cordova.startActivityForResult(this, takePictureIntent, REQUEST_IMAGE_CAPTURE)
                } else {
                    callbackContext?.error("No camera app found on device.")
                }
            } catch (e: Exception) {
                callbackContext?.error("Launch error: " + e.localizedMessage)
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
            if (resultCode == Activity.RESULT_OK) {
                cordova.threadPool.execute {
                    try {
                        val file = photoFile
                        if (file != null && file.exists() && file.length() > 0) {

                            // Decode bounds to scale down extremely large images and prevent Memory Crashes
                            val options = BitmapFactory.Options().apply {
                                inJustDecodeBounds = true
                            }
                            BitmapFactory.decodeFile(file.absolutePath, options)

                            val maxDimension = 1920
                            var inSampleSize = 1
                            if (options.outHeight > maxDimension || options.outWidth > maxDimension) {
                                val halfHeight = options.outHeight / 2
                                val halfWidth = options.outWidth / 2
                                while (halfHeight / inSampleSize >= maxDimension && halfWidth / inSampleSize >= maxDimension) {
                                    inSampleSize *= 2
                                }
                            }

                            val decodeOptions = BitmapFactory.Options().apply {
                                inSampleSize = inSampleSize
                            }

                            val fullBitmap = BitmapFactory.decodeFile(file.absolutePath, decodeOptions)

                            if (fullBitmap != null) {
                                val byteArrayOutputStream = ByteArrayOutputStream()
                                fullBitmap.compress(Bitmap.CompressFormat.JPEG, 85, byteArrayOutputStream)
                                val byteArray = byteArrayOutputStream.toByteArray()
                                val base64Image = Base64.encodeToString(byteArray, Base64.NO_WRAP)

                                file.delete() // Clean temp file
                                callbackContext?.success("data:image/jpeg;base64,$base64Image")
                            } else {
                                callbackContext?.error("Bitmap decoding returned null.")
                            }
                        } else {
                            callbackContext?.error("Image file empty or not found on disk.")
                        }
                    } catch (e: Exception) {
                        callbackContext?.error("Processing error: " + e.localizedMessage)
                    }
                }
            } else {
                callbackContext?.error("Camera action cancelled by user.")
            }
        }
    }
}
