
var exec    = require('cordova/exec'),
    channel = require('cordova/channel'),
    _isIos = false,
    _isAndroid = false;

module.exports = {

    // ANDROID
    acquireWakeLock: function (successCallback, errorCallback) {
        if(_isAndroid) {
            cordova.exec(successCallback, errorCallback, 'Insomnia', 'acquireWakeLock', []);
        }
    },
    releaseWakeLock: function (successCallback, errorCallback) {
        if(_isAndroid) {
            cordova.exec(successCallback, errorCallback, 'Insomnia', 'releaseWakeLock', []);
        }
    },
    stopBatteryOptimization: function (successCallback, errorCallback) {
        if(_isAndroid) {
            cordova.exec(successCallback, errorCallback, 'Insomnia', 'stopBatteryOptimization', []);
        }
    },
    isIgnoringBatteryOptimization: function (successCallback, errorCallback) {
        if(_isAndroid) {
            cordova.exec(successCallback, errorCallback, 'Insomnia', 'isIgnoringBatteryOptimization', []);
        }
    },
    switchOnScreenAndForeground: function (successCallback, errorCallback, options) {
        if(_isAndroid) {
            cordova.exec(successCallback, errorCallback, 'Insomnia', 'switchOnScreenAndForeground', [options]);
        }
    },
    clearKeepScreenOn: function (successCallback, errorCallback) {
        if(_isAndroid) {
            cordova.exec(successCallback, errorCallback, 'Insomnia', 'clearKeepScreenOn', []);
        }
    },
    enableRestartService: function (successCallback, errorCallback, options) {
        if(_isAndroid) {
            cordova.exec(successCallback, errorCallback, 'Insomnia', 'enableRestartService', [options]);
        }
    },
    enableForegroundService: function (successCallback, errorCallback, options) {
        if(_isAndroid) {
            cordova.exec(successCallback, errorCallback, 'Insomnia', 'enableForegroundService', [options]);
        }
    },
    // ANDROID

    // IOS
    enable: function (successCallback, errorCallback) {
        if(_isIos) {
            cordova.exec(successCallback, errorCallback, 'Insomnia', 'enable', []);
        }
    },
    disable: function (successCallback, errorCallback) {
        if(_isIos) {
            cordova.exec(successCallback, errorCallback, 'Insomnia', 'disable', []);
        }
    }
    // IOS    

}

// Called before 'deviceready' listener will be called
channel.onCordovaReady.subscribe(function() {
    channel.onCordovaInfoReady.subscribe(function() {
        _isAndroid = device.platform.match(/^android/i) !== null;
        _isIos = device.platform.match(/^iOS/i) !== null;
    });
});
