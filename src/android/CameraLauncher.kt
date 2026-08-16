package com.christian.customcamera

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
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
                options?.optInt(
                    DESTINATION_TYPE,
                    DATA_URL
                ) ?: DATA_URL

            cameraDirection =
                options?.optInt(
                    CAMERA_DIRECTION,
                    BACK
                ) ?: BACK

            // Keep quality within valid JPEG range
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

        /*
         * Check whether the device has a camera.
         */
        if (!cordova.activity.packageManager.hasSystemFeature(
                PackageManager.FEATURE_CAMERA_ANY
            )
        ) {

            callbackContext?.error(
                "No camera is available on this device."
            )

            return
        }

        /*
         * Create camera intent.
         */
        val intent = Intent(
            MediaStore.ACTION_IMAGE_CAPTURE
        )

        /*
         * Request front/back camera.
         *
         * These extras are commonly supported by Android camera
         * applications. The actual behavior can depend on the
         * device's camera application.
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
         * Create a temporary JPEG file in the application's cache.
         */
        val imageFile = File.createTempFile(
            "custom_camera_",
            ".jpg",
            cordova.activity.cacheDir
        )

        /*
         * Convert the file into a FileProvider URI.
         */
        imageUri = FileProvider.getUriForFile(
            cordova.activity,
            "${cordova.activity.packageName}.customcamera.fileprovider",
            imageFile
        )

        /*
         * Tell the camera application where to save
         * the captured image.
         */
        intent.putExtra(
            MediaStore.EXTRA_OUTPUT,
            imageUri
        )

        /*
         * Give the camera application temporary access
         * to the FileProvider URI.
         */
        intent.addFlags(
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
        )

        /*
         * Use Cordova's activity-result mechanism.
         *
         * This replaces the deprecated:
         *
         * activity.startActivityForResult(...)
         */
        cordova.startActivityForResult(
            this,
            intent,
            REQUEST_CAMERA
        )
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        intent: Intent?
    ) {

        /*
         * Ignore results that do not belong to our camera request.
         */
        if (requestCode != REQUEST_CAMERA) {
            return
        }

        /*
         * User cancelled the camera.
         */
        if (resultCode != Activity.RESULT_OK) {

            callbackContext?.error(
                "Camera cancelled."
            )

            cleanup()

            return
        }

        try {

            val uri = imageUri

            /*
             * Make sure we have the captured image URI.
             */
            if (uri == null) {

                callbackContext?.error(
                    "Camera returned no image."
                )

                cleanup()

                return
            }

            /*
             * Return the captured image as a File URI.
             */
            if (destinationType == FILE_URI) {

                callbackContext?.success(
                    uri.toString()
                )

            } else {

                /*
                 * Return the captured image as Base64.
                 */
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

    /*
     * Convert the captured image into Base64.
     *
     * The returned value is ONLY the Base64 string.
     *
     * It does NOT contain:
     *
     * data:image/jpeg;base64,
     */
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
                android.graphics.Bitmap.CompressFormat.JPEG,
                quality,
                outputStream
            )

            bitmap.recycle()

            return Base64.encodeToString(
                outputStream.toByteArray(),
                Base64.NO_WRAP
            )
        }
    }

    /*
     * Clear the temporary URI reference.
     *
     * The actual temporary file is left in the application's
     * cache directory and can be cleaned by Android.
     */
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
            // Ignore cleanup errors
        }

        imageUri = null
    }

    /*
     * Preserve important state if Android destroys and recreates
     * the Cordova activity while the camera is open.
     */
    override fun onSaveInstanceState(): Bundle {

        val state = Bundle()

        state.putInt(
            "quality",
            quality
        )

        state.putInt(
            "destinationType",
            destinationType
        )

        state.putInt(
            "cameraDirection",
            cameraDirection
        )

        imageUri?.let {
            state.putString(
                "imageUri",
                it.toString()
            )
        }

        return state
    }

    override fun onRestoreStateForActivityResult(
        state: Bundle,
        callbackContext: CallbackContext
    ) {

        this.callbackContext = callbackContext

        quality = state.getInt(
            "quality",
            80
        )

        destinationType = state.getInt(
            "destinationType",
            DATA_URL
        )

        cameraDirection = state.getInt(
            "cameraDirection",
            BACK
        )

        val savedUri =
            state.getString("imageUri")

        if (!savedUri.isNullOrEmpty()) {

            imageUri = Uri.parse(
                savedUri
            )
        }
    }
}
