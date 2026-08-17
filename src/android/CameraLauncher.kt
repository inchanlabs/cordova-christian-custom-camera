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
            // Immediate success response to test the native bridge
            callbackContext.success("NATIVE BRIDGE IS WORKING PERFECTLY!")
            return true
        }

        callbackContext.error("Unknown action: $action")
        return false
    }
}
