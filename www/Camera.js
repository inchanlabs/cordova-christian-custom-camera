var exec = require("cordova/exec");

var ChristianCustomCamera = {
    getPicture: function (successCallback, errorCallback, options) {
        var success = typeof successCallback === "function" ? successCallback : function () {};
        var error = typeof errorCallback === "function" ? errorCallback : function () {};

        exec(
            function(result) {
                success(result);
            },
            function(err) {
                error(err);
            },
            "CustomCamera",
            "takePicture",
            []
        );
    }
};

module.exports = ChristianCustomCamera;
