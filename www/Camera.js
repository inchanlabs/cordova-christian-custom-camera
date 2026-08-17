var Camera = {

    TEST_VALUE: "STEP5_CAMERA_JS_2026",

    CameraDirection: {
        BACK: 0,
        FRONT: 1
    },

    getPicture: function (successCallback, errorCallback, options) {

        console.log("STEP 5: CAMERA.JS LOADED");
        console.log("STEP 5: GETPICTURE CALLED");

        if (successCallback) {
            successCallback("STEP 5 CAMERA JS WORKS");
        }
    }
};

module.exports = Camera;
