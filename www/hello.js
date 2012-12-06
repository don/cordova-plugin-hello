/*global cordova*/
cordova.define("cordova/plugin/hello",
    function (require, exports, module) {

        var exec = cordova.require('cordova/exec');

        function greet(name, win, fail) {
            exec(win, fail, "Hello", "greet", [name]);
        }

        module.exports = {
            greet: greet
        }
    }
);
