var exec = require("cordova/exec");

var ChristianCustomCamera = {
    getPicture: function (successCallback, errorCallback, options) {
        var options = options || {};
        
        exec(
            function(result) {
                if (typeof successCallback === "function") {
                    successCallback(result);
                }
            },
            function(err) {
                if (typeof errorCallback === "function") {
                    errorCallback(err);
                }
            },
            "CustomCamera",
            "takePicture",
            [options]
        );
    }
};

module.exports = ChristianCustomCamera;
