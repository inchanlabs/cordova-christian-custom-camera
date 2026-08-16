package com.christian.customcamera

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.util.Base64
import org.apache.cordova.CallbackContext
import org.apache.cordova.CordovaPlugin
import org.apache.cordova.PluginResult
import org.json.JSONArray
import java.io.ByteArrayOutputStream

class CameraLauncher : CordovaPlugin() {

    companion object {
        private const val REQUEST_CAMERA = 1001

        private const val QUALITY = "quality"
        private const val CAMERA_DIRECTION = "cameraDirection"

        private const val BACK = 0
        private const val FRONT = 1
    }

    private var callbackContext: CallbackContext? = null
    private var quality: Int = 80
    private var cameraDirection: Int = BACK

    override fun execute(
        action: String,
        args: JSONArray,
        callbackContext: CallbackContext
    ): Boolean {

        if (action != "takePicture") {
            return false
        }

        this.callbackContext = callbackContext

        try {

            val options = args.optJSONObject(0)

            quality = options?.optInt(QUALITY, 80) ?: 80
            cameraDirection =
                options?.optInt(CAMERA_DIRECTION, BACK) ?: BACK

            quality = quality.coerceIn(0, 100)

            openCamera()

            val result = PluginResult(
                PluginResult.Status.NO_RESULT
            )

            result.keepCallback = true

            callbackContext.sendPluginResult(result)

            return true

        } catch (e: Exception) {

            callbackContext.error(
                "Camera initialization failed: ${e.message}"
            )

            return true
        }
    }

    private fun openCamera() {

        val intent = Intent(
            android.provider.MediaStore.ACTION_IMAGE_CAPTURE
        )

        if (cameraDirection == FRONT) {

            intent.putExtra(
                "android.intent.extras.CAMERA_FACING",
                1
            )

            intent.putExtra(
                "android.intent.extra.USE_FRONT_CAMERA",
                true
            )

        } else {

            intent.putExtra(
                "android.intent.extras.CAMERA_FACING",
                0
            )

            intent.putExtra(
                "android.intent.extra.USE_FRONT_CAMERA",
                false
            )
        }

        cordova.setActivityResultCallback(this)

        cordova.activity.startActivityForResult(
            intent,
            REQUEST_CAMERA
        )
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        intent: Intent?
    ) {

        if (requestCode != REQUEST_CAMERA) {
            return
        }

        if (resultCode != Activity.RESULT_OK) {

            callbackContext?.error(
                "Camera cancelled."
            )

            return
        }

        try {

            val bitmap =
                intent?.extras?.get("data") as? Bitmap

            if (bitmap == null) {

                callbackContext?.error(
                    "Camera returned no image."
                )

                return
            }

            val base64 = convertToBase64(bitmap)

            if (base64.isNullOrEmpty()) {

                callbackContext?.error(
                    "Unable to process captured image."
                )

                return
            }

            callbackContext?.success(base64)

        } catch (e: Exception) {

            callbackContext?.error(
                "Error processing image: ${e.message}"
            )
        }
    }

    private fun convertToBase64(
        bitmap: Bitmap
    ): String? {

        return try {

            val outputStream =
                ByteArrayOutputStream()

            bitmap.compress(
                Bitmap.CompressFormat.JPEG,
                quality,
                outputStream
            )

            Base64.encodeToString(
                outputStream.toByteArray(),
                Base64.NO_WRAP
            )

        } catch (e: Exception) {

            null
        }
    }
}
