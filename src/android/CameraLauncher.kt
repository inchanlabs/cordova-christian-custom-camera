
package com.christian.customcamera

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.core.content.FileProvider
import org.apache.cordova.CallbackContext
import org.apache.cordova.CordovaPlugin
import org.apache.cordova.PluginResult
import org.json.JSONArray
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream

class CameraLauncher : CordovaPlugin() {

    companion object {
        private const val REQUEST_CAMERA = 1001

        private const val QUALITY = "quality"
        private const val DESTINATION_TYPE = "destinationType"
        private const val CAMERA_DIRECTION = "cameraDirection"

        private const val DATA_URL = 0
        private const val FILE_URI = 1

        private const val BACK = 0
        private const val FRONT = 1
    }

    private var callbackContext: CallbackContext? = null
    private var imageUri: Uri? = null

    private var quality: Int = 80
    private var destinationType: Int = DATA_URL
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
            destinationType =
                options?.optInt(DESTINATION_TYPE, DATA_URL) ?: DATA_URL

            cameraDirection =
                options?.optInt(CAMERA_DIRECTION, BACK) ?: BACK

            if (quality < 0) {
                quality = 0
            }

            if (quality > 100) {
                quality = 100
            }

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

        if (!cordova.activity.packageManager.hasSystemFeature(
                PackageManager.FEATURE_CAMERA_ANY
            )
        ) {

            callbackContext?.error(
                "No camera is available on this device."
            )

            return
        }

        val intent = Intent(
            MediaStore.ACTION_IMAGE_CAPTURE
        )

        /*
         * Request the front/back camera.
         *
         * Android camera applications commonly recognize
         * this Camera2-compatible intent extra.
         */
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

        /*
         * Create temporary image file.
         */
        val imageFile = File.createTempFile(
            "custom_camera_",
            ".jpg",
            cordova.activity.cacheDir
        )

        imageUri = FileProvider.getUriForFile(
            cordova.activity,
            "${cordova.activity.packageName}.customcamera.fileprovider",
            imageFile
        )

        intent.putExtra(
            MediaStore.EXTRA_OUTPUT,
            imageUri
        )

        intent.addFlags(
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
        )

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

            cleanup()

            return
        }

        try {

            val uri = imageUri

            if (uri == null) {

                callbackContext?.error(
                    "Camera returned no image."
                )

                cleanup()

                return
            }

            if (destinationType == FILE_URI) {

                callbackContext?.success(
                    uri.toString()
                )

            } else {

                val base64 = convertToBase64(uri)

                if (base64.isNullOrEmpty()) {

                    callbackContext?.error(
                        "Unable to read captured image."
                    )

                } else {

                    callbackContext?.success(
                        base64
                    )
                }
            }

        } catch (e: Exception) {

            callbackContext?.error(
                "Error processing image: ${e.message}"
            )

        } finally {

            cleanup()
        }
    }

    private fun convertToBase64(
        uri: Uri
    ): String? {

        val inputStream: InputStream? =
            cordova.activity.contentResolver.openInputStream(uri)

        inputStream.use { input ->

            if (input == null) {
                return null
            }

            val bitmap =
                BitmapFactory.decodeStream(input)
                    ?: return null

            val outputStream =
                ByteArrayOutputStream()

            bitmap.compress(
                Bitmap.CompressFormat.JPEG,
                quality,
                outputStream
            )

            bitmap.recycle()

            return android.util.Base64.encodeToString(
                outputStream.toByteArray(),
                android.util.Base64.NO_WRAP
            )
        }
    }

    private fun cleanup() {

        try {

            imageUri?.let { uri ->

                val cursor =
                    cordova.activity.contentResolver.query(
                        uri,
                        null,
                        null,
                        null,
                        null
                    )

                cursor?.close()
            }

        } catch (_: Exception) {
        }

        imageUri = null
    }
}
