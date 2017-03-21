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
