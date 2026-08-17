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

        console.log("CAMERA JS: ENTERED");

        if (successCallback) {
            successCallback(
                "CAMERA JS REACHED WITH EXEC LOADED"
            );
        }

        console.log("CAMERA JS: EXITED");
    }
};

module.exports = Camera;
