var exec = require("cordova/exec");

var Camera = {

    DestinationType: {
        DATA_URL: 0,
        FILE_URI: 1
    },

    EncodingType: {
        JPEG: 0,
        PNG: 1
    },

    MediaType: {
        PICTURE: 0
    },

    PictureSourceType: {
        PHOTOLIBRARY: 0,
        CAMERA: 1,
        SAVEDPHOTOALBUM: 2
    },

    CameraDirection: {
        BACK: 0,
        FRONT: 1
    },

    getPicture: function (successCallback, errorCallback, options) {

        options = options || {};

        exec(
            successCallback,
            errorCallback,
            "CustomCamera",
            "takePicture",
            [options]
        );
    }
};

module.exports = Camera;
