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

        Log.d(
            TAG,
            "execute() called. action=$action"
        )

        if ("takePicture" == action) {

            if (cordova.hasPermission(Manifest.permission.CAMERA)) {

                Log.d(
                    TAG,
                    "Camera permission already granted."
                )

                launchCamera()

            } else {

                Log.d(
                    TAG,
                    "Requesting camera permission."
                )

                cordova.requestPermission(
                    this,
                    CAMERA_PERMISSION_REQ_CODE,
                    Manifest.permission.CAMERA
                )
            }

            return true
        }

        Log.e(
            TAG,
            "Action not recognized: $action"
        )

        callbackContext.error(
            "Action not recognized: $action"
        )

        return false
    }

    private fun launchCamera() {

        cordova.activity.runOnUiThread {

            try {

                Log.d(
                    TAG,
                    "Preparing camera intent."
                )

                val takePictureIntent =
                    Intent(MediaStore.ACTION_IMAGE_CAPTURE)

                /*
                 * Create a temporary JPEG file in the application's
                 * private cache directory.
                 */
                val photoFile = File.createTempFile(
                    "IMG_",
                    ".jpg",
                    cordova.activity.cacheDir
                )

                Log.d(
                    TAG,
                    "Photo file created."
                )

                Log.d(
                    TAG,
                    "Photo file path: ${photoFile.absolutePath}"
                )

                Log.d(
                    TAG,
                    "Photo file exists: ${photoFile.exists()}"
                )

                Log.d(
                    TAG,
                    "Photo file canWrite: ${photoFile.canWrite()}"
                )

                /*
                 * Create the FileProvider URI.
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
                 * Tell the camera to save the actual photo
                 * into this URI.
                 */
                takePictureIntent.putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    photoUri
                )

                /*
                 * Grant the camera application permission
                 * to read and write the URI.
                 */
                takePictureIntent.addFlags(
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                /*
                 * Check whether Android can resolve a camera
                 * application for this intent.
                 */
                val resolvedActivities =
                    takePictureIntent.resolveActivity(
                        cordova.activity.packageManager
                    )

                Log.d(
                    TAG,
                    "Camera activity resolved: $resolvedActivities"
                )

                /*
                 * Launch camera.
                 */
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
                    "Camera activity launched."
                )

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "Failed to launch camera.",
                    e
                )

                callbackContext?.error(
                    "Failed to launch camera: " +
                    "${e.javaClass.simpleName}: ${e.message}"
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

                Log.d(
                    TAG,
                    "Camera permission granted."
                )

                launchCamera()

            } else {

                Log.e(
                    TAG,
                    "Camera permission denied."
                )

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
            "onActivityResult() requestCode=$requestCode " +
            "resultCode=$resultCode " +
            "photoUri=$photoUri"
        )

        /*
         * TEMPORARY DEBUG:
         *
         * We are only checking whether the camera returns
         * successfully to the Cordova plugin.
         */
        callbackContext?.success(
            "DEBUG: onActivityResult reached. " +
            "requestCode=$requestCode " +
            "resultCode=$resultCode"
        )

        /*
         * Stop here temporarily.
         *
         * We are not processing the image yet.
         */
        return
    }
}
