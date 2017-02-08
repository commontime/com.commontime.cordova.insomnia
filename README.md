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

Alternatively, you can start the wakelock and stop battery optimizations on start-up by using plugin variables:


```
#!bash

cordova plugins add ../com.commontime.cordova.insomnia --variable WAKELOCK=false --save --variable BATTOP=false
```

```
#!xml

<plugin name="com.commontime.cordova.insomnia" spec="../com.commontime.cordova.insomnia">
    <variable name="WAKELOCK" value="true" />
    <variable name="BATTOP" value="rue" />
</plugin>
```