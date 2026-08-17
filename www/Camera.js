var exec = require("cordova/exec");

var Camera = {

    TEST_VALUE: "STEP_8_EXEC_TEST",

    CameraDirection: {
        BACK: 0,
        FRONT: 1
    },

    getPicture: function (successCallback, errorCallback, options) {

        console.log("STEP 8: BEFORE EXEC");

        try {

            exec(
                function (result) {

                    console.log(
                        "STEP 8: EXEC SUCCESS: " + result
                    );

                    if (successCallback) {
                        successCallback(result);
                    }
                },

                function (error) {

                    console.log(
                        "STEP 8: EXEC ERROR: " + error
                    );

                    if (errorCallback) {
                        errorCallback(error);
                    }
                },

                "FakeTestPlugin",
                "test",
                []
            );

            console.log("STEP 8: AFTER EXEC");

        } catch (error) {

            console.log(
                "STEP 8: EXEC EXCEPTION: " + error
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
