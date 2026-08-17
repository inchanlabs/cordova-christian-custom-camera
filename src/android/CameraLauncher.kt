package com.christian.customcamera

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import org.apache.cordova.CallbackContext
import org.apache.cordova.CordovaPlugin
import org.apache.cordova.PluginResult
import org.json.JSONArray
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CameraLauncher : CordovaPlugin() {

    private var callbackContext: CallbackContext? = null
    private var imageUri: Uri? = null

    companion object {
        private const val REQUEST_CODE_CAMERA = 1001
    }

    override fun execute(
        action: String,
        args: JSONArray,
        callbackContext: CallbackContext
    ): Boolean {
        this.callbackContext = callbackContext

        if (action == "takePicture") {
            takePicture()
            return true
        }
        return false
    }

    private fun takePicture() {
        val activity = cordova.activity
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (intent.resolveActivity(activity.packageManager) != null) {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                callbackContext?.error("Error creating file: ${ex.localizedMessage}")
                null
            }

            photoFile?.let { file ->
                val authority = "${activity.packageName}.provider"
                imageUri = FileProvider.getUriForFile(activity, authority, file)

                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

                cordova.startActivityForResult(this, intent, REQUEST_CODE_CAMERA)
            }
        } else {
            callbackContext?.error("No camera application available")
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = cordova.activity.cacheDir
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        if (requestCode == REQUEST_CODE_CAMERA) {
            if (resultCode == Activity.RESULT_OK) {
                imageUri?.let { uri ->
                    callbackContext?.success(uri.toString())
                } ?: run {
                    callbackContext?.error("Image URI is null")
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                callbackContext?.error("Camera cancelled")
            } else {
                callbackContext?.error("Failed to capture image")
            }
        }
    }
}
