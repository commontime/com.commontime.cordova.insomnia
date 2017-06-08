module.exports = {
    acquireWakeLock: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, 'Insomnia', 'acquireWakeLock', []);
    },
    releaseWakeLock: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, 'Insomnia', 'releaseWakeLock', []);
    },
    stopBatteryOptimization: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, 'Insomnia', 'stopBatteryOptimization', []);
    },
    isIgnoringBatteryOptimization: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, 'Insomnia', 'isIgnoringBatteryOptimization', []);
    },
    switchOnScreenAndForeground: function (successCallback, errorCallback, options) {
        cordova.exec(successCallback, errorCallback, 'Insomnia', 'switchOnScreenAndForeground', [options]);
    }
}
