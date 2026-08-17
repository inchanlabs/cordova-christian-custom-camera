package com.christian.customcamera

import org.apache.cordova.CallbackContext
import org.apache.cordova.CordovaPlugin
import org.json.JSONArray

class CameraLauncher : CordovaPlugin() {

    override fun execute(
        action: String,
        args: JSONArray,
        callbackContext: CallbackContext
    ): Boolean {

        if (action != "takePicture") {
            callbackContext.error("Unknown action: $action")
            return false
        }

        callbackContext.success("NATIVE CAMERA PLUGIN REACHED")

        return true
    }
}
