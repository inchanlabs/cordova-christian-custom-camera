package com.christian.customcamera

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import androidx.core.content.FileProvider
import org.apache.cordova.CallbackContext
import org.apache.cordova.CordovaPlugin
import org.json.JSONArray
import java.io.File

class CameraLauncher : CordovaPlugin() {

    private var callbackContext: CallbackContext? = null
    private var photoUri: Uri? = null

    private val REQUEST_IMAGE_CAPTURE = 1001
    private val CAMERA_PERMISSION_REQ_CODE = 2001

    override fun execute(
        action: String,
        args: JSONArray,
        callbackContext: CallbackContext
    ): Boolean {
        this.callbackContext = callbackContext

        if ("takePicture" == action) {
            // Check if CAMERA permission is granted at runtime
            if (cordova.hasPermission(Manifest.permission.CAMERA)) {
                launchCamera()
            } else {
                // Request camera permission dynamically
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

            val photoFile = File.createTempFile(
                "IMG_",
                ".jpg",
                cordova.activity.cacheDir
            )

            photoUri = FileProvider.getUriForFile(
                cordova.activity,
                "${cordova.activity.packageName}.fileprovider",
                photoFile
            )

            takePictureIntent.putExtra(
                MediaStore.EXTRA_OUTPUT,
                photoUri
            )

            takePictureIntent.addFlags(
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            cordova.startActivityForResult(
                this,
                takePictureIntent,
                REQUEST_IMAGE_CAPTURE
            )

        } catch (e: Exception) {
            callbackContext?.error(
                "Failed to launch camera: " + e.message
            )
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
                callbackContext?.error("Camera permission denied by user.")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
    super.onActivityResult(requestCode, resultCode, intent)

    if (requestCode == REQUEST_IMAGE_CAPTURE) {
        if (resultCode == Activity.RESULT_OK) {
            try {
                val imageFileUri = photoUri

                if (imageFileUri == null) {
                    callbackContext?.error("Image URI is missing.")
                    return
                }

                val inputStream = cordova.activity.contentResolver.openInputStream(imageFileUri)

                if (inputStream == null) {
                    callbackContext?.error("Failed to open captured image.")
                    return
                }

                val imageBytes = inputStream.use {
                    it.readBytes()
                }

                if (imageBytes.isEmpty()) {
                    callbackContext?.error("Captured image is empty.")
                    return
                }

                val base64Image = Base64.encodeToString(
                    imageBytes,
                    Base64.NO_WRAP
                )

                callbackContext?.success(
                    "data:image/jpeg;base64,$base64Image"
                )

                photoUri = null

            } catch (e: Exception) {
                callbackContext?.error(
                    "Failed to process captured image: " + e.message
                )
            }
        } else {
            photoUri = null
            callbackContext?.error("Camera action cancelled.")
        }
    }
}
}
