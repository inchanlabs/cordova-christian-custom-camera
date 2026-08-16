var exec = require("cordova/exec");

var Camera = {

    TEST_VALUE: "CHRISTIAN_CUSTOM_CAMERA_V1",

    CameraDirection: {
        BACK: 0,
        FRONT: 1
    },

    getPicture: function (successCallback, errorCallback, options) {

        options = options || {};

        console.log("CUSTOM CAMERA: getPicture called");
        console.log("CUSTOM CAMERA: calling Cordova exec");
        console.log("CUSTOM CAMERA: service = CustomCamera");
        console.log("CUSTOM CAMERA: action = takePicture");

        exec(
            function (result) {
                console.log(
                    "CUSTOM CAMERA: SUCCESS = " + result
                );

                if (successCallback) {
                    successCallback(result);
                }
            },
            function (error) {
                console.log(
                    "CUSTOM CAMERA: ERROR = " + error
                );

                if (errorCallback) {
                    errorCallback(error);
                }
            },
            "CustomCamera",
            "takePicture",
            [options]
        );
    }
};

module.exports = Camera;
