var Camera = {

    TEST_VALUE: "STEP_7F_CAMERA_JS",

    CameraDirection: {
        BACK: 0,
        FRONT: 1
    },

    getPicture: function (successCallback, errorCallback, options) {

        console.log("STEP 7F: CAMERA.JS GETPICTURE ENTERED");

        if (successCallback) {
            successCallback("STEP 7F CAMERA JS WORKS");
        }

        console.log("STEP 7F: CAMERA.JS GETPICTURE EXITED");
    }
};

module.exports = Camera;
