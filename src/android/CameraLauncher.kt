```kotlin
package com.christian.customcamera

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import androidx.core.content.FileProvider
import org.apache.cordova.CallbackContext
import org.apache.cordova.CordovaPlugin
import org.json.JSONArray
import java.io.File
import java.io.FileInputStream

class CameraLauncher : CordovaPlugin() {

    private var callbackContext: CallbackContext? = null
    private var imageUri: Uri? = null

    private val REQUEST_IMAGE_CAPTURE = 1001
    private val CAMERA_PERMISSION_REQ_CODE = 2001

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
                cordova.requestPermission(
                    this,
                    CAMERA_PERMISSION_REQ_CODE,
                    Manifest.permission.CAMERA
                )
            }

            return true
        }

        callbackContext.error(
            "Action not recognized: $action"
        )

        return false
    }

    private fun launchCamera() {

        cordova.activity.runOnUiThread {

            try {

                val imageFile = File.createTempFile(
                    "christian_camera_",
                    ".jpg",
                    cordova.activity.cacheDir
                )

                imageUri = FileProvider.getUriForFile(
                    cordova.activity,
                    cordova.activity.packageName +
                        ".customcamera.fileprovider",
                    imageFile
                )

                val takePictureIntent =
                    Intent(MediaStore.ACTION_IMAGE_CAPTURE)

                takePictureIntent.putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    imageUri
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
                    "Failed to launch camera: " +
                    e.message
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

            if (
                grantResults != null &&
                grantResults.isNotEmpty() &&
                grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
            ) {

                launchCamera()

            } else {

                callbackContext?.error(
                    "Camera permission denied by user."
                )
            }
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        intent: Intent?
    ) {

        super.onActivityResult(
            requestCode,
            resultCode,
            intent
        )

        if (requestCode != REQUEST_IMAGE_CAPTURE) {
            return
        }

        if (resultCode != Activity.RESULT_OK) {

            callbackContext?.error(
                "Camera action cancelled."
            )

            cleanupImageFile()

            return
        }

        try {

            val uri = imageUri

            if (uri == null) {

                callbackContext?.error(
                    "Camera image URI is missing."
                )

                return
            }

            val imageFile = File(
                uri.path ?: ""
            )

            /*
             * Read the full-resolution image directly
             * from the temporary camera file.
             */
            val fileBytes = FileInputStream(imageFile).use {
                it.readBytes()
            }

            if (fileBytes.isEmpty()) {

                callbackContext?.error(
                    "Captured image is empty."
                )

                cleanupImageFile()

                return
            }

            val base64Image =
                Base64.encodeToString(
                    fileBytes,
                    Base64.NO_WRAP
                )

            callbackContext?.success(
                "data:image/jpeg;base64,$base64Image"
            )

            cleanupImageFile()

        } catch (e: Exception) {

            callbackContext?.error(
                "Failed to process captured image: " +
                e.message
            )

            cleanupImageFile()
        }
    }

    private fun cleanupImageFile() {

        try {

            imageUri?.path?.let { path ->

                val file = File(path)

                if (file.exists()) {
                    file.delete()
                }
            }

        } catch (_: Exception) {
            // Ignore cleanup errors
        }

        imageUri = null
    }
}
```
