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
        if (action == "takePicture") {
            callbackContext.success("NATIVE BRIDGE IS WORKING!")
            return true
        }

        callbackContext.error("Invalid action: $action")
        return false
    }
}
