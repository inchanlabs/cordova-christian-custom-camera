var exec = require("cordova/exec");

var Camera = {

    TEST_VALUE: "CHRISTIAN_CUSTOM_CAMERA_V1",

    CameraDirection: {
        BACK: 0,
        FRONT: 1
    },

    getPicture: function (successCallback, errorCallback, options) {

        exec(
            successCallback,
            errorCallback,
            "CustomCamera",
            "takePicture",
            []
        );
    }
};

module.exports = Camera;
