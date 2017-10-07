/*global cordova, module*/

module.exports = {
    payment: function (amount, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Hello", "greet", [amount]);
    }
};
