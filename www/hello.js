/*global cordova, module*/

module.exports = {
    payment: function (amount, successCallback, errorCallback) {
        console.log(cordova.exec(successCallback, errorCallback, "Hello", "greet", [amount]));
    }
};
