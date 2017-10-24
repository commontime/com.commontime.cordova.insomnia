# com.commontime.cordova.insomnia

Cordova plugin for keeping your android and iOS device awake.

#### Important to bear in mind if using across both Android and iOS:

Due to platform limitations on iOS the plugin's behavior is limited to enabling or disabling background running when the app is dismissed by the user when pressing the home button. But compared to Android, when using this plugin on an iOS device:

* Background running cannot be restarted if it is killed off by the user or OS
* The app or background running cannot be started after the device has turned on or restarted
* It is not possible to turn on the screen or unlock the device.

#### App store submission for iOS:

To achieve the insomnia ability the plugin private API's and therefore will most likely be rejected on an app store submission.

## Android

#### Wake lock

If you need to keep the CPU running in order to complete some work before the device goes to sleep, you can use a PowerManager system service feature called wake locks. Wake locks allow your application to control the power state of the host device.

```
plugins.insomnia.acquireWakeLock( function() {
    console.info("done");
}, function(error) {
    console.error(error);
});
```

```
plugins.insomnia.releaseWakeLock( function() {
    console.info("done");
}, function(error) {
    console.error(error);
});
```

#### Battery optimization

Request to place the app on the battery optimization white-list.  An app that is whitelisted can use the network and hold partial wake locks during Doze and App Standby. However, other restrictions still apply to the whitelisted app, just as they do to other apps.

https://developer.android.com/training/monitoring-device-state/doze-standby.html

```
plugins.insomnia.stopBatteryOptimization( function() {
    console.info("done");
}, function(error) {
    console.error(error);
});
```

```
plugins.insomnia.isIgnoringBatteryOptimization( function(response) {   
    console.log(response.isIgnoringBatteryOptimization);
}, function() {});

```

#### App Restart Service

Enables/disables a service that restarts the app if it is closed by the user or by the OS.

```
plugins.insomnia.enableRestartService( function(response) {   
    console.info("done");
}, function(error) {
    console.error(error);
}, {
    "enable": true
});

```

#### Foreground service

Enables/disables a the icon in the status bar that helps keep the app from being stopped.

https://developer.android.com/guide/components/services.html#Foreground

```
plugins.insomnia.enableForegroundService( function(response) {   
    console.info("done");
}, function(error) {
    console.error(error);
}, {
    "enable": true
});

```


eg:

```
plugins.insomnia.acquireWakeLock( function() {
    plugins.insomnia.stopBatteryOptimization( function() {
        connect();
    }, function() {
        alert("Rejected!");
    });       
}, function() {
    alert("broken!");
});

```

Alternatively, you can start the wakelock and stop battery optimizations on start-up by using plugin variables:


```
cordova plugins add ../com.commontime.cordova.insomnia 
    --variable WAKELOCK=true
    --variable BATTOP=true
    --variable FGSERVICEENABLED=true
    --variable FGSERVICEMAIN="Foreground service"
    --variable FGSERVICESUB="..."
    --variable APPRESTARTSERVICE=true
    --variable STARTONBOOT=true
    --save
```

```
<plugin name="com.commontime.cordova.insomnia" spec="../com.commontime.cordova.insomnia">
    <variable name="WAKELOCK" value="true"/>
    <variable name="BATTOP" value="true"/>
    <variable name="FGSERVICEENABLED" value="true"/>
    <variable name="FGSERVICEMAIN" value="Incident Responder"/>
    <variable name="FGSERVICESUB" value="..."/>
    <variable name="APPRESTARTSERVICE" value="true"/>
    <variable name="STARTONBOOT" value="true"/>
</plugin>
```

### Turning on the screen and unlocking for app

```
plugins.insomnia.switchOnScreenAndForeground(function() {console.log("done");}, function() {console.error("error");}, {
  "showWhenLocked": true,  // default true
  "turnScreenOn": true,    // default true
  "dismissKeyGuard": true, // default true
  "keepScreenOn": true     // default true
});
```

#### showWhenLocked:

Flag to let windows be shown when the screen is locked. This will let application windows take precedence over key guard or any other lock screens. Can be used with ```keepScreenOn``` to turn screen on and display windows directly before showing the key guard window. Can be used with dismissKeyGuard to automatically fully dismisss non-secure keyguards.

#### turnScreenOn:

Poke the power manager's user activity (as if the user had woken up the device) to turn the screen on.

#### dismissKeyGuard:

Cause the keyguard to be dismissed, only if it is not a secure lock keyguard. Because such a keyguard is not needed for security, it will never re-appear if the user navigates to another window (in contrast to ```showWhenLocked```, which will only temporarily hide both secure and non-secure keyguards but ensure they reappear when the user moves to another UI that doesn't hide them). If the keyguard is currently active and is secure (requires an unlock credential) than the user will still need to confirm it before seeing this window, unless ```showWhenLocked``` has also been set.

#### keepScreenOn:

As long as this window is visible to the user, keep the device's screen turned on and bright.

```
plugins.insomnia.clearKeepScreenOn(function() {console.log("done");}, function() {console.error("failed!"});
```

Allows the screen to go off again

## iOS

Funtionality on iOS is very limited; it is only possible to enbale or disable the insomnia ability. Please note that the following functions are iOS only. If you try to call them from an Android device the error callback will be fired along with the message "iOS Only". The same principle applies if you are on an iOS device and try to call a function under the Android section. In this case you will get the message "Android Only" in the error callback.

### enable:

```
plugins.insomnia.enable( function(response) {   
    console.info("done");
}, function(error) {
    console.error(error);
}, {
    "enable": true
});

```

### disable

```
plugins.insomnia.disable( function(response) {   
    console.info("done");
}, function(error) {
    console.error(error);
}, {
    "enable": false
});

```

### Foregrounding the app

```
plugins.insomnia.switchOnScreenAndForeground(function() {console.log("done");}, function() {console.error("error");});
```