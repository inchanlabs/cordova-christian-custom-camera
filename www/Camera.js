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

        console.log("CAMERA: ENTERED getPicture");

        try {

            console.log("CAMERA: exec type = " + typeof exec);

            if (typeof exec !== "function") {
                throw new Error("cordova/exec is not a function");
            }

            console.log("CAMERA: BEFORE EXEC");

            exec(
                function (result) {
                    console.log("CAMERA: EXEC SUCCESS");

                    if (successCallback) {
                        successCallback(result);
                    }
                },
                function (error) {
                    console.log("CAMERA: EXEC ERROR = " + error);

                    if (errorCallback) {
                        errorCallback(error);
                    }
                },
                "CustomCamera",
                "takePicture",
                [options || {}]
            );

            console.log("CAMERA: AFTER EXEC");

        } catch (e) {

            console.log("CAMERA: EXEC EXCEPTION = " + e.message);

            if (errorCallback) {
                errorCallback(e.message);
            }
        }
    }
};

module.exports = Camera;
