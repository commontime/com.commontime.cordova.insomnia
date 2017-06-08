# com.commontime.cordova.insomnia

Cordova plugin for keeping your android awake.


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
cordova plugins add ../com.commontime.cordova.insomnia --variable WAKELOCK=false --save --variable BATTOP=false
```

```
<plugin name="com.commontime.cordova.insomnia" spec="../com.commontime.cordova.insomnia">
    <variable name="WAKELOCK" value="true" />
    <variable name="BATTOP" value="true" />
</plugin>
```

## Turning on the screen and unlocking for app

```
plugins.insomnia.switchOnScreenAndForeground(function() {$success();}, function() {$fail();}, {
  "showWhenLocked": true,  // default true
  "turnScreenOn": true,    // default true
  "dismissKeyGuard": true, // default true
  "keepScreenOn": true     // default true
});
```

### showWhenLocked:

Flag to let windows be shown when the screen is locked. This will let application windows take precedence over key guard or any other lock screens. Can be used with ```keepScreenOn``` to turn screen on and display windows directly before showing the key guard window. Can be used with dismissKeyGuard to automatically fully dismisss non-secure keyguards.

### turnScreenOn: 

Poke the power manager's user activity (as if the user had woken up the device) to turn the screen on.

### dismissKeyGuard:

Cause the keyguard to be dismissed, only if it is not a secure lock keyguard. Because such a keyguard is not needed for security, it will never re-appear if the user navigates to another window (in contrast to ```showWhenLocked```, which will only temporarily hide both secure and non-secure keyguards but ensure they reappear when the user moves to another UI that doesn't hide them). If the keyguard is currently active and is secure (requires an unlock credential) than the user will still need to confirm it before seeing this window, unless ```showWhenLocked``` has also been set.

### keepScreenOn:

As long as this window is visible to the user, keep the device's screen turned on and bright.






