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

        console.log("CAMERA JS: getPicture ENTERED");

        if (successCallback) {
            successCallback("CAMERA JS FUNCTION REACHED");
        }

        console.log("CAMERA JS: getPicture EXITED");
    }
};

module.exports = Camera;
