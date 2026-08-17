package com.christian.customcamera

import org.apache.cordova.CallbackContext
import org.apache.cordova.CordovaInterface
import org.apache.cordova.CordovaPlugin
import org.apache.cordova.CordovaWebView
import org.apache.cordova.PluginResult
import org.json.JSONArray

class CameraLauncher : CordovaPlugin() {

    override fun initialize(cordova: CordovaInterface, webView: CordovaWebView) {
        super.initialize(cordova, webView)
    }

    override fun execute(
        action: String,
        args: JSONArray,
        callbackContext: CallbackContext
    ): Boolean {
        if ("takePicture" == action) {
            val result = PluginResult(PluginResult.Status.OK, "NATIVE BRIDGE IS WORKING!")
            callbackContext.sendPluginResult(result)
            return true
        }

        callbackContext.error("Action not recognized: $action")
        return false
    }
}
