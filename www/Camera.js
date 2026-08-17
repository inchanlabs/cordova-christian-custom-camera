var exec = require("cordova/exec");

var Camera = {

    TEST_VALUE: "STEP6_EXEC_LOADED",

    CameraDirection: {
        BACK: 0,
        FRONT: 1
    },

    getPicture: function (successCallback, errorCallback, options) {

        console.log("STEP 6: GETPICTURE ENTERED");

        console.log(
            "STEP 6: EXEC TYPE = " + typeof exec
        );

        if (successCallback) {
            successCallback(
                "STEP 6 EXEC LOADED"
            );
        }

        console.log("STEP 6: GETPICTURE EXITED");
    }
};

module.exports = Camera;
