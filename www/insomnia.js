
var exec    = require('cordova/exec'),
    channel = require('cordova/channel'),
    _isIos = false,
    _isAndroid = false;

module.exports = {

    // ANDROID
    acquireWakeLock: function (successCallback, errorCallback) {
        if(_isAndroid) {
            cordova.exec(successCallback, errorCallback, 'Insomnia', 'acquireWakeLock', []);
        } else {
        	errorCallback("Android Only");
        }
    },
    releaseWakeLock: function (successCallback, errorCallback) {
        if(_isAndroid) {
            cordova.exec(successCallback, errorCallback, 'Insomnia', 'releaseWakeLock', []);
        } else {
        	errorCallback("Android Only");
        }
    },
    stopBatteryOptimization: function (successCallback, errorCallback) {
        if(_isAndroid) {
            cordova.exec(successCallback, errorCallback, 'Insomnia', 'stopBatteryOptimization', []);
        } else {
        	errorCallback("Android Only");
        }
    },
    isIgnoringBatteryOptimization: function (successCallback, errorCallback) {
        if(_isAndroid) {
            cordova.exec(successCallback, errorCallback, 'Insomnia', 'isIgnoringBatteryOptimization', []);
        } else {
        	errorCallback("Android Only");
        }
    },
    switchOnScreenAndForeground: function (successCallback, errorCallback, options) {
        if(_isAndroid) {
            cordova.exec(successCallback, errorCallback, 'Insomnia', 'switchOnScreenAndForeground', [options]);
        } else {
        	errorCallback("Android Only");
        }
    },
    clearKeepScreenOn: function (successCallback, errorCallback) {
        if(_isAndroid) {
            cordova.exec(successCallback, errorCallback, 'Insomnia', 'clearKeepScreenOn', []);
        } else {
        	errorCallback("Android Only");
        }
    },
    enableRestartService: function (successCallback, errorCallback, options) {
        if(_isAndroid) {
            cordova.exec(successCallback, errorCallback, 'Insomnia', 'enableRestartService', [options]);
        } else {
        	errorCallback("Android Only");
        }
    },
    enableForegroundService: function (successCallback, errorCallback, options) {
        if(_isAndroid) {
            cordova.exec(successCallback, errorCallback, 'Insomnia', 'enableForegroundService', [options]);
        } else {
        	errorCallback("Android Only");
        }
    },
    // ANDROID

    // IOS
    enable: function (successCallback, errorCallback) {
        if(_isIos) {
            cordova.exec(successCallback, errorCallback, 'Insomnia', 'enable', []);
        } else {
        	errorCallback("iOS Only");
        }
    },
    disable: function (successCallback, errorCallback) {
        if(_isIos) {
            cordova.exec(successCallback, errorCallback, 'Insomnia', 'disable', []);
        } else {
        	errorCallback("iOS Only");
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
