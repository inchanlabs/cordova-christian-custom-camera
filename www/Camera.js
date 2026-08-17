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

        console.log("CAMERA JS: BEFORE EXEC");

        try {

            exec(
                function (result) {

                    console.log(
                        "CAMERA JS: EXEC SUCCESS: " + result
                    );

                    if (successCallback) {
                        successCallback(result);
                    }
                },
                function (error) {

                    console.log(
                        "CAMERA JS: EXEC ERROR: " + error
                    );

                    if (errorCallback) {
                        errorCallback(error);
                    }
                },
                "CustomCamera",
                "takePicture",
                []
            );

            console.log("CAMERA JS: AFTER EXEC");

        } catch (e) {

            console.log(
                "CAMERA JS: EXEC EXCEPTION: " + e.message
            );

            if (errorCallback) {
                errorCallback(
                    "EXEC EXCEPTION: " + e.message
                );
            }
        }
    }
};

module.exports = Camera;
