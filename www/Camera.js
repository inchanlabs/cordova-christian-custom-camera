var exec = require("cordova/exec");

var Camera = {

    TEST_VALUE: "STEP7_EXEC_TEST",

    CameraDirection: {
        BACK: 0,
        FRONT: 1
    },

    getPicture: function (successCallback, errorCallback, options) {

        console.log("STEP 7: BEFORE EXEC");

        try {

            exec(
                function (result) {

                    console.log(
                        "STEP 7: EXEC SUCCESS: " + result
                    );

                    if (successCallback) {
                        successCallback(result);
                    }
                },

                function (error) {

                    console.log(
                        "STEP 7: EXEC ERROR: " + error
                    );

                    if (errorCallback) {
                        errorCallback(error);
                    }
                },

                "FakeTestPlugin",
                "test",
                []
            );

            console.log("STEP 7: AFTER EXEC");

        } catch (error) {

            console.log(
                "STEP 7: EXEC EXCEPTION: " + error
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
