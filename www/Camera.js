var exec = require("cordova/exec");

var Camera = {

    TEST_VALUE: "CHRISTIAN_CUSTOM_CAMERA_V1",

    CameraDirection: {
        BACK: 0,
        FRONT: 1
    },

    getPicture: function (
        successCallback,
        errorCallback,
        options
    ) {

        options = options || {};

        exec(
            function (result) {

                if (successCallback) {
                    successCallback(result);
                }

            },
            function (error) {

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
