
var exec = require("cordova/exec");

var Camera = {

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
