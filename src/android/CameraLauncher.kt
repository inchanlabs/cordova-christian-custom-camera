package com.christian.customcamera

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import androidx.core.content.FileProvider
import org.apache.cordova.CallbackContext
import org.apache.cordova.CordovaPlugin
import org.json.JSONArray
import java.io.ByteArrayOutputStream
import java.io.File

class CameraLauncher : CordovaPlugin() {

    private var callbackContext: CallbackContext? = null
    private val REQUEST_IMAGE_CAPTURE = 1001
    private val CAMERA_PERMISSION_REQ_CODE = 2001
    private var photoUri: Uri? = null
    private var imageFilePath: String? = null

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
                val context = cordova.activity.applicationContext
                val storageDir = context.cacheDir
                val photoFile = File.createTempFile("photo_", ".jpg", storageDir)
                imageFilePath = photoFile.absolutePath

                val authority = context.packageName + ".provider"
                photoUri = FileProvider.getUriForFile(context, authority, photoFile)

                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)

                // Grant dynamic permissions to all matching camera packages
                val resInfoList = context.packageManager.queryIntentActivities(
                    intent,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
                for (resolveInfo in resInfoList) {
                    val packageName = resolveInfo.activityInfo.packageName
                    context.grantUriPermission(
                        packageName,
                        photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }

                if (intent.resolveActivity(context.packageManager) != null) {
                    cordova.startActivityForResult(this, intent, REQUEST_IMAGE_CAPTURE)
                } else {
                    callbackContext?.error("No camera app found on device.")
                }
            } catch (e: Exception) {
                callbackContext?.error("Launch error: " + e.localizedMessage)
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
                callbackContext?.error("Camera permission denied.")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == Activity.RESULT_OK) {
                cordova.threadPool.execute {
                    try {
                        val path = imageFilePath
                        val file = if (path != null) File(path) else null

                        if (file != null && file.exists() && file.length() > 0) {

                            // Compress and decode to avoid memory crashes
                            val options = BitmapFactory.Options().apply {
                                inJustDecodeBounds = true
                            }
                            BitmapFactory.decodeFile(file.absolutePath, options)

                            val maxDimension = 1920
                            var inSampleSize = 1
                            if (options.outHeight > maxDimension || options.outWidth > maxDimension) {
                                val halfHeight = options.outHeight / 2
                                val halfWidth = options.outWidth / 2
                                while (halfHeight / inSampleSize >= maxDimension && halfWidth / inSampleSize >= maxDimension) {
                                    inSampleSize *= 2
                                }
                            }

                            val decodeOptions = BitmapFactory.Options().apply {
                                inSampleSize = inSampleSize
                            }

                            val fullBitmap = BitmapFactory.decodeFile(file.absolutePath, decodeOptions)

                            if (fullBitmap != null) {
                                val byteArrayOutputStream = ByteArrayOutputStream()
                                fullBitmap.compress(Bitmap.CompressFormat.JPEG, 85, byteArrayOutputStream)
                                val byteArray = byteArrayOutputStream.toByteArray()
                                val base64Image = Base64.encodeToString(byteArray, Base64.NO_WRAP)

                                file.delete() // Cleanup
                                callbackContext?.success("data:image/jpeg;base64,$base64Image")
                            } else {
                                callbackContext?.error("Bitmap conversion failed.")
                            }
                        } else {
                            callbackContext?.error("Captured photo file missing or 0 bytes.")
                        }
                    } catch (e: Exception) {
                        callbackContext?.error("Processing exception: " + e.localizedMessage)
                    }
                }
            } else {
                callbackContext?.error("Camera action cancelled by user.")
            }
        }
    }
}
