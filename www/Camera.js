var exec = require("cordova/exec");

var Camera = {

    TEST_VALUE: "CHRISTIAN_CUSTOM_CAMERA_V1",

    CameraDirection: {
        BACK: 0,
        FRONT: 1
    },

    getPicture: function (options) {

        options = options || {};

        return new Promise(function (resolve, reject) {

            exec(
                function (result) {
                    resolve(result);
                },
                function (error) {
                    reject(error);
                },
                "CustomCamera",
                "takePicture",
                [options]
            );

        });
    }
};

module.exports = Camera;
