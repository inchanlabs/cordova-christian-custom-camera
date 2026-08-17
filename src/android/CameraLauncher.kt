package com.christian.customcamera

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
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
                 * Create temporary JPEG file inside the application's
                 * private cache directory.
                 */
                val photoFile = File.createTempFile(
                    "IMG_",
                    ".jpg",
                    cordova.activity.cacheDir
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
                 * Create FileProvider URI.
                 */
                photoUri = FileProvider.getUriForFile(
                    cordova.activity,
                    "${cordova.activity.packageName}.fileprovider",
                    photoFile
                )

                Log.d(
                    TAG,
                    "Photo URI: $photoUri"
                )

                /*
                 * Give the camera application the output URI.
                 */
                takePictureIntent.putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    photoUri
                )

                /*
                 * Explicit URI permissions.
                 */
                takePictureIntent.addFlags(
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                /*
                 * Some camera applications require the URI to also
                 * be supplied through ClipData for the URI permission
                 * grant to be recognized correctly.
                 */
                takePictureIntent.clipData =
                    ClipData.newRawUri(
                        "CameraOutput",
                        photoUri
                    )

                /*
                 * Find the application that will handle the camera intent.
                 */
                val resolvedActivity =
                    takePictureIntent.resolveActivity(
                        cordova.activity.packageManager
                    )

                Log.d(
                    TAG,
                    "Resolved camera activity: $resolvedActivity"
                )

                if (resolvedActivity == null) {

                    callbackContext?.error(
                        "No camera application is available."
                    )

                    photoUri = null
                    return@runOnUiThread
                }

                /*
                 * Explicitly grant the resolved camera application
                 * permission to access the output URI.
                 */
                val cameraPackage =
                    resolvedActivity.packageName

                Log.d(
                    TAG,
                    "Granting URI permission to: $cameraPackage"
                )

                cordova.activity.grantUriPermission(
                    cameraPackage,
                    photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                Log.d(
                    TAG,
                    "URI permission granted."
                )

                /*
                 * Launch the camera.
                 */
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

                photoUri = null

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
         * TEMPORARY DEBUG ONLY.
         *
         * We are still testing whether Android returns RESULT_OK.
         */
        callbackContext?.success(
            "DEBUG: onActivityResult reached. " +
            "requestCode=$requestCode " +
            "resultCode=$resultCode"
        )

        /*
         * Do not process the image yet.
         */
        return
    }
}
