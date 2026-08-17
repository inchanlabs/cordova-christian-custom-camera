var exec = require("cordova/exec");

var ChristianCustomCamera = {
    getPicture: function (successCallback, errorCallback, options) {
        var opts = options || {};

        console.log("[ChristianCustomCamera] getPicture called");
        console.log("[ChristianCustomCamera] options:", opts);

        exec(
            function(result) {
                console.log("[ChristianCustomCamera] SUCCESS callback received");
                console.log("[ChristianCustomCamera] result:", result);

                if (typeof successCallback === "function") {
                    successCallback(result);
                } else {
                    console.warn("[ChristianCustomCamera] No success callback provided");
                }
            },
            function(err) {
                console.error("[ChristianCustomCamera] ERROR callback received");
                console.error("[ChristianCustomCamera] error:", err);

                if (typeof errorCallback === "function") {
                    errorCallback(err);
                } else {
                    console.warn("[ChristianCustomCamera] No error callback provided");
                }
            },
            "CustomCamera",
            "takePicture",
            [opts]
        );
    }
};

module.exports = ChristianCustomCamera;
