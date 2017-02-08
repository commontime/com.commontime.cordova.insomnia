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
    plugins.insomnia.isIgnoringBatteryOptimization( function(response) {   
        if( !response.isIgnoringBatteryOptimization ) {
            plugins.insomnia.stopBatteryOptimization( function() {
                connect();
            }, function() {
                alert("Rejected!");
            }););
        }
    }, function() {});
}, function() {
    alert("broken!");
});

```