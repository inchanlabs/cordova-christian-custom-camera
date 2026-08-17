var exec = require("cordova/exec");

var ChristianCustomCamera = {

    TEST_VALUE: "CHRISTIAN_CUSTOM_CAMERA_V1",

    CameraDirection: {
        BACK: 0,
        FRONT: 1
    },

    getPicture: function (successCallback, errorCallback, options) {
        var success = typeof successCallback === "function" ? successCallback : function () {};
        var error = typeof errorCallback === "function" ? errorCallback : function () {};
        var args = options ? [options] : [];

        exec(
            success,
            error,
            "CustomCamera",
            "takePicture",
            args
        );
    }
};

module.exports = ChristianCustomCamera;
