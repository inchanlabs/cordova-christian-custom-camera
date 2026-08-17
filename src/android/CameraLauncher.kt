package com.christian.customcamera

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Base64
import org.apache.cordova.CallbackContext
import org.apache.cordova.CordovaInterface
import org.apache.cordova.CordovaPlugin
import org.apache.cordova.CordovaWebView
import org.json.JSONArray
import java.io.ByteArrayOutputStream

class CameraLauncher : CordovaPlugin() {

    private var callbackContext: CallbackContext? = null
    private val REQUEST_IMAGE_CAPTURE = 1001

    override fun execute(
        action: String,
        args: JSONArray,
        callbackContext: CallbackContext
    ): Boolean {
        this.callbackContext = callbackContext

        if ("takePicture" == action) {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            
            // Launch the native camera activity
            cordova.startActivityForResult(this, takePictureIntent, REQUEST_IMAGE_CAPTURE)
            return true
        }

        callbackContext.error("Action not recognized: $action")
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == Activity.RESULT_OK && intent != null) {
                // Extract thumbnail bitmap returned by camera intent
                val imageBitmap = intent.extras?.get("data") as? Bitmap

                if (imageBitmap != null) {
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)
                    val byteArray = byteArrayOutputStream.toByteArray()
                    val base64Image = Base64.encodeToString(byteArray, Base64.NO_WRAP)

                    // Return Base64 image back to OutSystems
                    callbackContext?.success("data:image/jpeg;base64,$base64Image")
                } else {
                    callbackContext?.error("Failed to capture image bitmap.")
                }
            } else {
                callbackContext?.error("Camera action cancelled.")
            }
        }
    }
}
