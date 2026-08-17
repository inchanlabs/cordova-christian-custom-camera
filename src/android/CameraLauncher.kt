package com.christian.customcamera

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import org.apache.cordova.CallbackContext
import org.apache.cordova.CordovaPlugin
import org.apache.cordova.PluginResult
import org.json.JSONArray
import java.io.ByteArrayOutputStream

class CameraLauncher : CordovaPlugin() {

    private var callbackContext: CallbackContext? = null

    companion object {
        private const val TAG = "ChristianCustomCamera"
        private const val CAMERA_REQUEST_CODE = 1001
        private const val PERMISSION_REQUEST_CODE = 1002
    }

    override fun execute(
        action: String,
        args: JSONArray,
        callbackContext: CallbackContext
    ): Boolean {
        Log.d(TAG, "EXECUTE CALLED: action=$action")
        this.callbackContext = callbackContext

        if (action == "takePicture") {
            try {
                if (cordova.hasPermission(Manifest.permission.CAMERA)) {
                    launchCameraIntent()
                } else {
                    cordova.requestPermission(this, PERMISSION_REQUEST_CODE, Manifest.permission.CAMERA)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during takePicture launch", e)
                callbackContext.error("Native Launch Error: " + e.message)
            }
            return true
        }

        callbackContext.error("Unknown action: $action")
        return false
    }

    private fun launchCameraIntent() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        
        // Ensure intent can be handled safely
        val activity = cordova.activity
        if (activity != null) {
            try {
                cordova.startActivityForResult(this, cameraIntent, CAMERA_REQUEST_CODE)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start camera activity", e)
                callbackContext?.error("Could not launch system camera: " + e.message)
            }
        } else {
            callbackContext?.error("Cordova Activity is null.")
        }
    }

    override fun onRequestPermissionResult(
        requestCode: Int,
        permissions: Array<out String>?,
        grantResults: IntArray?
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults != null && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCameraIntent()
            } else {
                callbackContext?.error("Camera permission denied by user.")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val photo = intent?.extras?.get("data") as? Bitmap

                if (photo != null) {
                    val base64Image = bitmapToBase64(photo)
                    callbackContext?.success(base64Image)
                } else {
                    callbackContext?.error("Failed to capture image data.")
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                callbackContext?.error("Camera operation cancelled.")
            } else {
                callbackContext?.error("Failed to capture picture. ResultCode: $resultCode")
            }
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}
