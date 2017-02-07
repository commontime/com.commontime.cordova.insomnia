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

eg:

```
#!javascript


plugins.insomnia.stopBatteryOptimization( function() {
    plugins.insomnia.acquireWakeLock( function() {
        connect();
    }, function() {
        alert("broken!");
    });
}, function() {
    alert("Rejected!");
});
```