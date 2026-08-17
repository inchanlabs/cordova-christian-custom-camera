var exec = require("cordova/exec");

var Camera = {

    TEST_VALUE: "CHRISTIAN_CUSTOM_CAMERA_V1",

    CameraDirection: {
        BACK: 0,
        FRONT: 1
    },

    getPicture: function (successCallback, errorCallback, options) {

        options = options || {};

        console.log("CUSTOM CAMERA: BEFORE EXEC");

        try {

            exec(
                function (result) {

                    console.log(
                        "CUSTOM CAMERA: EXEC SUCCESS: " + result
                    );

                    if (successCallback) {
                        successCallback(result);
                    }
                },

                function (error) {

                    console.log(
                        "CUSTOM CAMERA: EXEC ERROR: " + error
                    );

                    if (errorCallback) {
                        errorCallback(error);
                    }
                },

                "CustomCamera",
                "takePicture",
                [options]
            );

            console.log("CUSTOM CAMERA: AFTER EXEC");

        } catch (error) {

            console.log(
                "CUSTOM CAMERA: EXEC EXCEPTION: " + error
            );

            if (errorCallback) {
                errorCallback(
                    "EXEC EXCEPTION: " + error
                );
            }
        }
    }
};

module.exports = Camera;
