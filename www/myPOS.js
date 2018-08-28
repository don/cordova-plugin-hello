/*global cordova, module*/

module.exports = {
    payment: function (amount, connectionType, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "myPOS", "payment", [amount, connectionType]);
    }
};
