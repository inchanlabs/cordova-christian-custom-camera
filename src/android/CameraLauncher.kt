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

        android.util.Log.d(
            "ChristianCustomCamera",
            "EXECUTE CALLED: action=$action"
        )

        if (action == "takePicture") {

            android.util.Log.d(
                "ChristianCustomCamera",
                "TAKE PICTURE ACTION REACHED"
            )

            callbackContext.success(
                "NATIVE CAMERA PLUGIN REACHED"
            )

            return true
        }

        callbackContext.error(
            "Unknown action: $action"
        )

        return false
    }
}
