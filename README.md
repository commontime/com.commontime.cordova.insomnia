# com.commontime.cordova.insomnia

Cordova plugin for keeping your android awake.


```
#!javascript

plugins.insomnia.acquireWakeLock( function() {
    console.info("done");
}, function(error) {
    console.error(error);
});
```


```
#!javascript

plugins.insomnia.releaseWakeLock( function() {
    console.info("done");
}, function(error) {
    console.error(error);
});
```


```
#!javascript

plugins.insomnia.stopBatteryOptimization( function() {
    console.info("done");
}, function(error) {
    console.error(error);
});
```

```
#!javascript

plugins.insomnia.isIgnoringBatteryOptimization( function(response) {   
    console.log(response.isIgnoringBatteryOptimization);
}, function() {});

```

eg:

```
#!javascript

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

You can also start the wakelock and stop batt optimizations on startup by using plugin variables:


```
#!bash

cordova plugins add ../com.commontime.cordova.insomnia --variable WAKELOCK=false --save --variable BATTOP=false
```

```
#!xml

<plugin name="com.commontime.cordova.insomnia" spec="../com.commontime.cordova.insomnia">
    <variable name="WAKELOCK" value="false" />
    <variable name="BATTOP" value="false" />
</plugin>
```