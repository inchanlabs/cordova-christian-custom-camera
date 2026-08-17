```kotlin
package com.christian.customcamera

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import androidx.core.content.FileProvider
import org.apache.cordova.CallbackContext
import org.apache.cordova.CordovaPlugin
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream

class CameraLauncher : CordovaPlugin() {

    private var callbackContext: CallbackContext? = null
    private var imageUri: Uri? = null
    private var imageFile: File? = null

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

                val file = File.createTempFile(
                    "christian_camera_",
                    ".jpg",
                    cordova.activity.cacheDir
                )

                imageFile = file

                imageUri = FileProvider.getUriForFile(
                    cordova.activity,
                    cordova.activity.packageName +
                        ".customcamera.fileprovider",
                    file
                )

                val takePictureIntent =
                    Intent(MediaStore.ACTION_IMAGE_CAPTURE)

                takePictureIntent.putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    imageUri
                )

                takePictureIntent.addFlags(
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                takePictureIntent.addFlags(
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

            val file = imageFile

            if (file == null || !file.exists()) {

                callbackContext?.error(
                    "Captured image file does not exist."
                )

                cleanupImageFile()
                return
            }

            // Get the actual captured image dimensions.
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true

            BitmapFactory.decodeFile(
                file.absolutePath,
                options
            )

            val width = options.outWidth
            val height = options.outHeight

            val fileSizeKB =
                file.length() / 1024

            // Read the full-resolution JPEG.
            val fileBytes =
                FileInputStream(file).use {
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

            // Build the diagnostic JSON result.
            val result = JSONObject()

            result.put(
                "base64",
                "data:image/jpeg;base64,$base64Image"
            )

            result.put(
                "width",
                width
            )

            result.put(
                "height",
                height
            )

            result.put(
                "sizeKB",
                fileSizeKB
            )

            // IMPORTANT:
            // Unique marker to prove this native version
            // is actually deployed in the ODC APK.
            callbackContext?.success(
                "NATIVE_STEP_2K:" +
                result.toString()
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

            imageFile?.let { file ->

                if (file.exists()) {
                    file.delete()
                }
            }

        } catch (_: Exception) {
            // Ignore cleanup errors.
        }

        imageFile = null
        imageUri = null
    }
}
```
