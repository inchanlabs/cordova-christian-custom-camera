package com.christian.customcamera

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
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

    companion object {
        private const val TAG = "ChristianCustomCamera"
    }

    override fun execute(
        action: String,
        args: JSONArray,
        callbackContext: CallbackContext
    ): Boolean {

        this.callbackContext = callbackContext

        Log.d(TAG, "execute() called. action=$action")

        if ("takePicture" == action) {

            if (cordova.hasPermission(Manifest.permission.CAMERA)) {
                Log.d(TAG, "Camera permission already granted.")
                launchCamera()
            } else {
                Log.d(TAG, "Requesting camera permission.")

                cordova.requestPermission(
                    this,
                    CAMERA_PERMISSION_REQ_CODE,
                    Manifest.permission.CAMERA
                )
            }

            return true
        }

        Log.e(TAG, "Action not recognized: $action")
        callbackContext.error("Action not recognized: $action")
        return false
    }

    private fun launchCamera() {

        cordova.activity.runOnUiThread {

            try {

                Log.d(TAG, "Preparing camera intent.")

                val takePictureIntent =
                    Intent(MediaStore.ACTION_IMAGE_CAPTURE)

                /*
                 * Create a temporary JPEG file inside the application's
                 * private cache directory.
                 *
                 * This does not require storage permissions.
                 */
                val photoFile = File.createTempFile(
                    "IMG_",
                    ".jpg",
                    cordova.activity.cacheDir
                )

                Log.d(
                    TAG,
                    "Temporary photo file created: ${photoFile.absolutePath}"
                )

                /*
                 * Convert the file into a content:// URI that can safely
                 * be shared with the camera application.
                 */
                photoUri = FileProvider.getUriForFile(
                    cordova.activity,
                    "${cordova.activity.packageName}.fileprovider",
                    photoFile
                )

                Log.d(
                    TAG,
                    "Photo URI created: $photoUri"
                )

                /*
                 * Tell the camera application to save the FULL image
                 * into this URI instead of returning a thumbnail.
                 */
                takePictureIntent.putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    photoUri
                )

                /*
                 * Grant the camera application permission to write
                 * to the temporary image URI.
                 */
                takePictureIntent.addFlags(
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                Log.d(
                    TAG,
                    "Launching camera with output URI: $photoUri"
                )

                cordova.startActivityForResult(
                    this,
                    takePictureIntent,
                    REQUEST_IMAGE_CAPTURE
                )

                Log.d(
                    TAG,
                    "Camera activity launched successfully."
                )

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "Failed to launch camera.",
                    e
                )

                callbackContext?.error(
                    "Failed to launch camera: ${e.javaClass.simpleName}: ${e.message}"
                )
            }
        }
    }

    override fun onRequestPermissionResult(
        requestCode: Int,
        permissions: Array<out String>?,
        grantResults: IntArray?
    ) {

        Log.d(
            TAG,
            "onRequestPermissionResult() requestCode=$requestCode"
        )

        if (requestCode == CAMERA_PERMISSION_REQ_CODE) {

            if (
                grantResults != null &&
                grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {

                Log.d(TAG, "Camera permission granted.")
                launchCamera()

            } else {

                Log.e(TAG, "Camera permission denied.")

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

        Log.d(
            TAG,
            "onActivityResult() requestCode=$requestCode resultCode=$resultCode photoUri=$photoUri"
        )

        if (requestCode != REQUEST_IMAGE_CAPTURE) {
            return
        }

        try {

            /*
             * User cancelled the camera.
             */
            if (resultCode != Activity.RESULT_OK) {

                Log.d(
                    TAG,
                    "Camera action cancelled. resultCode=$resultCode"
                )

                photoUri = null

                callbackContext?.error(
                    "Camera action cancelled."
                )

                return
            }

            /*
             * Get the URI of the full-resolution image.
             */
            val imageFileUri = photoUri

            if (imageFileUri == null) {

                Log.e(
                    TAG,
                    "Image URI is missing."
                )

                callbackContext?.error(
                    "Image URI is missing."
                )

                return
            }

            Log.d(
                TAG,
                "Opening captured image URI: $imageFileUri"
            )

            /*
             * Open the actual JPEG file.
             */
            val inputStream =
                cordova.activity.contentResolver.openInputStream(
                    imageFileUri
                )

            if (inputStream == null) {

                Log.e(
                    TAG,
                    "Failed to open captured image."
                )

                callbackContext?.error(
                    "Failed to open captured image."
                )

                return
            }

            /*
             * Read the complete JPEG into memory.
             */
            val imageBytes = inputStream.use {
                it.readBytes()
            }

            Log.d(
                TAG,
                "Image bytes read: ${imageBytes.size}"
            )

            if (imageBytes.isEmpty()) {

                Log.e(
                    TAG,
                    "Captured image is empty."
                )

                callbackContext?.error(
                    "Captured image is empty."
                )

                return
            }

            /*
             * Convert the ORIGINAL JPEG bytes to Base64.
             *
             * No Bitmap conversion.
             * No target width.
             * No target height.
             * No additional JPEG compression.
             */
            val base64Image = Base64.encodeToString(
                imageBytes,
                Base64.NO_WRAP
            )

            Log.d(
                TAG,
                "Base64 generated. Length=${base64Image.length}"
            )

            /*
             * Return the full-resolution image to Cordova/ODC.
             */
            callbackContext?.success(
                "data:image/jpeg;base64,$base64Image"
            )

            Log.d(
                TAG,
                "Success callback sent."
            )

            /*
             * Clear the URI reference.
             */
            photoUri = null

        } catch (e: Exception) {

            Log.e(
                TAG,
                "ERROR processing captured image.",
                e
            )

            callbackContext?.error(
                "Failed to process captured image: " +
                    "${e.javaClass.simpleName}: ${e.message}"
            )

            photoUri = null
        }
    }
}
