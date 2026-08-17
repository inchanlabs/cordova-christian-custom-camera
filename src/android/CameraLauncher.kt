```kotlin
package com.christian.customcamera

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Base64
import org.apache.cordova.CallbackContext
import org.apache.cordova.CordovaPlugin
import org.apache.cordova.PluginResult
import org.json.JSONArray
import java.io.ByteArrayOutputStream

class CameraLauncher : CordovaPlugin() {

    private var callbackContext: CallbackContext? = null
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

                val takePictureIntent =
                    Intent(MediaStore.ACTION_IMAGE_CAPTURE)

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

        if (requestCode == REQUEST_IMAGE_CAPTURE) {

            if (resultCode == Activity.RESULT_OK) {

                val imageBitmap =
                    intent?.extras?.get("data") as? Bitmap

                if (imageBitmap != null) {

                    val byteArrayOutputStream =
                        ByteArrayOutputStream()

                    imageBitmap.compress(
                        Bitmap.CompressFormat.JPEG,
                        80,
                        byteArrayOutputStream
                    )

                    val byteArray =
                        byteArrayOutputStream.toByteArray()

                    val base64Image =
                        Base64.encodeToString(
                            byteArray,
                            Base64.NO_WRAP
                        )

                    callbackContext?.success(
                        "data:image/jpeg;base64,$base64Image"
                    )

                } else {

                    callbackContext?.error(
                        "Failed to capture image bitmap."
                    )
                }

            } else {

                callbackContext?.error(
                    "Camera action cancelled."
                )
            }
        }
    }
}
```
