package com.commontime.cordova.plugins.insomnia;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;

import android.content.BroadcastReceiver;
import android.util.Log;
import android.widget.Toast;
import android.content.IntentFilter;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;

import static android.content.Context.POWER_SERVICE;

public class Insomnia extends CordovaPlugin {

    private static final String TAG = "Insomnia";

    private static final int OP = 139690458;

    private static final String ACQUIRE_WAKE_LOCK = "acquireWakeLock";
    private static final String RELEASE_WAKE_LOCK = "releaseWakeLock";
    private static final String STOP_BATTERY_OPTIMIZATION = "stopBatteryOptimization";
    private static final String IS_IGNORING_BATTERY_OPTIMIZATION = "isIgnoringBatteryOptimization";
    private static final String SWITCH_ON_SCREEN_AND_FOREGROUND = "switchOnScreenAndForeground";
    private static final String CLEAR_KEEP_SCREEN_ON = "clearKeepScreenOn";
    private static final String ENABLE_RESTART_SERVICE = "enableRestartService";
    private static final String ENABLE_FOREGROUND_SERVICE = "enableForegroundService";

    static final String CHANNEL_ID = "Insomnia_Channel_ID";
    
    String wakeLockTag = "Commontime::Insomnia";
    String wifiLockTag = "Commontime::WifiInsomnia";
    
    private PowerManager.WakeLock lock;
    private WifiManager.WifiLock wifiLock;
    private CallbackContext batteryCallback;
    private ForegroundService mForegroundService;
    private boolean mBound;
    private String fgServiceMainString;
    private String fgServiceSubString;

    private boolean showWhenLocked;
    private boolean turnScreenOn;
    private boolean dismissKeyGuard;
    private boolean keepScreenOn;

    boolean foreground = false;

    private Bundle configBundle;

    private static Field appViewField;
    static {
        try {
            Class<?> cdvActivityClass = CordovaActivity.class;
            Field wvField = cdvActivityClass.getDeclaredField("appView");
            wvField.setAccessible(true);
            appViewField = wvField;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
    
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            ForegroundService.LocalBinder binder = (ForegroundService.LocalBinder) service;
            mForegroundService = binder.getService();
            mBound = true;
            mForegroundService.startForeground(fgServiceMainString, fgServiceSubString);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    public Insomnia() {
    }
    
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            StringBuilder sb = new StringBuilder();
            sb.append("Action: " + intent.getAction() + "\n");
            sb.append("URI: " + intent.toUri(Intent.URI_INTENT_SCHEME).toString() + "\n");
            String log = sb.toString();
            Log.d(TAG, log);
            Toast.makeText(context, log, Toast.LENGTH_LONG).show();

            Insomnia.this.switchOnScreenAndForeground(null);
        }
    };
    
    @Override
    protected void pluginInitialize() {

        Settings.getInstance().setup(cordova.getActivity());
        
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.commontime.cordova.plugins.insomnia.action_WAKE_UP");
        cordova.getActivity().registerReceiver(this.broadcastReceiver, intentFilter);

        ApplicationInfo ai = null;
        try {
            ai = cordova.getActivity().getPackageManager().getApplicationInfo(cordova.getActivity().getPackageName(), PackageManager.GET_META_DATA);
            Bundle aBundle = ai.metaData;
            configBundle = aBundle;
            boolean wakelock = aBundle.getBoolean("acquireWakeLockOnStart");
            if( wakelock ) {
                requestWakeLock();
            }
            boolean battOp = aBundle.getBoolean("requestStopBatteryOptimizationOnStartup");
            if( battOp ) {
                stopBatteryOptimization(null);
            }
            boolean fgService = aBundle.getBoolean("useForegroundService");
            if( fgService ) {
                startForegroundService(true);
            }

            boolean appRestartService = aBundle.getBoolean("appRestartService");
            if(appRestartService) {
                Intent i2 = new Intent(cordova.getActivity(), RestarterService.class);
                cordova.getActivity().bindService(i2, connection, Context.BIND_AUTO_CREATE);
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    private void createNotificationChannel(String channelName, String description) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {            
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = cordova.getActivity().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onResume(boolean multiTask) {
        super.onResume(multiTask);
        Window window = cordova.getActivity().getWindow();
        if( showWhenLocked ) {            
            try {
                cordova.getActivity().setShowWhenLocked(true);
            } catch(NoSuchMethodError e) {
                window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            }
        }
        if( turnScreenOn ) {
            
            try {
                cordova.getActivity().setTurnScreenOn(true);
            } catch(NoSuchMethodError e) {
                window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            }
        }
        if( dismissKeyGuard ) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        }
        if( keepScreenOn ) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        foreground = true;
    }

    @Override
    public void onPause(boolean multitask) {
        super.onPause(multitask);
        foreground = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cordova.getActivity().unbindService(connection);
    }

    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if( action.equals(ACQUIRE_WAKE_LOCK)) {
            requestWakeLock();
            callbackContext.success();
            return true;
        } else if( action.equals(RELEASE_WAKE_LOCK)) {
            releaseWakeLock();
            callbackContext.success();
            return true;
        } else if( action.equals(STOP_BATTERY_OPTIMIZATION)) {
            stopBatteryOptimization(callbackContext);
            return true;
        } else if( action.equals(IS_IGNORING_BATTERY_OPTIMIZATION)) {
            checkBatteryOptimization(callbackContext);
            return true;
        } else if( action.equals(SWITCH_ON_SCREEN_AND_FOREGROUND)) {
            JSONObject options = args.getJSONObject(0);
            showWhenLocked = options.optBoolean("showWhenLocked", true);
            turnScreenOn = options.optBoolean("turnScreenOn", true);
            dismissKeyGuard = options.optBoolean("dismissKeyGuard", false);
            keepScreenOn = options.optBoolean("keepScreenOn", true);
            switchOnScreenAndForeground(callbackContext);
            return true;
        } else if( action.equals(CLEAR_KEEP_SCREEN_ON)) {
            clearKeepScreenOn(callbackContext);
            showWhenLocked = false;
            turnScreenOn = false;
            dismissKeyGuard = false;
            keepScreenOn = false;
            return true;
        } else if( action.equals(ENABLE_RESTART_SERVICE)) {
            boolean enable = args.getJSONObject(0).getBoolean("enable");
            Settings.enableRestartService(enable);
            callbackContext.success();
            return true;
        } else if( action.equals(ENABLE_FOREGROUND_SERVICE)) {
            boolean enable = args.getJSONObject(0).getBoolean("enable");
            startForegroundService(enable);
            callbackContext.success();
            return true;
        } else if( action.equals("wakeTimers")) {
            try {
                final CordovaWebView webView = (CordovaWebView) appViewField.get(cordova.getActivity());
                Handler mainHandler = new Handler(cordova.getActivity().getMainLooper());
                final Looper myLooper = Looper.myLooper();
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        ((WebView)webView.getView()).resumeTimers();
                        ((WebView)webView.getView()).onResume();
                        new Handler(myLooper).post(new Runnable() {
                            @Override
                            public void run() {
                                callbackContext.success();
                            }
                        });
                    }
                });

            } catch (Throwable e) {
                callbackContext.error(e.getMessage());
            }
            return true;
        }

        return false;
    }

    private void startForegroundService(boolean enable) {
        Intent intent = new Intent(cordova.getActivity(), ForegroundService.class);
        if( enable ) {
            System.out.println("Starting foreground service");
            fgServiceMainString = configBundle.getString("fgServiceMainString", "Foreground Service");
            fgServiceSubString = configBundle.getString("fgServiceSubString", "Preventing app from being stopped");
            createNotificationChannel(fgServiceMainString, fgServiceSubString);
            cordova.getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        } else {
            System.out.println("Stopping foreground service");
            cordova.getActivity().unbindService(mConnection);
        }
    }

    private void clearKeepScreenOn(final CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                
                if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.N) {
                    Window window = cordova.getActivity().getWindow();
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                    window.clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
                    callbackContext.success();
                    return;
                }               
                
                Window window = cordova.getActivity().getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                try {
                    cordova.getActivity().setShowWhenLocked(false);
                } catch(NoSuchMethodError e) {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                }
                window.clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
                try {
                    cordova.getActivity().setTurnScreenOn(false);
                } catch(NoSuchMethodError e) {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
                }
                callbackContext.success();
            }
        });
    }

    private void switchOnScreenAndForeground(final CallbackContext callbackContext) {

        boolean screenOn = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            PowerManager powerManager = (PowerManager) cordova.getActivity().getSystemService(POWER_SERVICE);
            if (powerManager.isInteractive()) {
                screenOn = true;
            }
        } else {
            PowerManager powerManager = (PowerManager) cordova.getActivity().getSystemService(POWER_SERVICE);
            if (powerManager.isScreenOn()) {
                screenOn = true;
            }
        }

        if (!(screenOn && foreground)) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Intent i2 = new Intent("com.commontime.cordova.plugins.insomnia.BlankActivity");
                    i2.putExtra("turnScreenOn", turnScreenOn);
                    i2.setPackage(cordova.getActivity().getPackageName());
                    cordova.getActivity().startActivity(i2);
                }
            });
        }
        if( callbackContext != null ) {
            callbackContext.success();
        }
    }

    private void checkBatteryOptimization(CallbackContext callbackContext) {
        String packageName = cordova.getActivity().getPackageName();
        PowerManager pm = (PowerManager) cordova.getActivity().getSystemService(POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if( pm.isIgnoringBatteryOptimizations(packageName) ) {
                callbackContext.success("true");
            } else {
                callbackContext.success("false");
            }
            return;
        }
        callbackContext.success("true");
        return;
    }

    private void requestWakeLock() {
        System.out.println("Requesting Wake Lock");
        PowerManager pm = (PowerManager) cordova.getActivity().getSystemService(POWER_SERVICE);
        lock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, wakeLockTag);                           
        lock.acquire();
        
        WifiManager wifiManager = (WifiManager) cordova.getActivity().getSystemService(Context.WIFI_SERVICE);

        if( wifiLock == null )
            wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, wifiLockTag);

        wifiLock.setReferenceCounted(false);

        if( !wifiLock.isHeld() )
            wifiLock.acquire();
    }

    private void releaseWakeLock() {
        lock.release();
               		
        if( wifiLock != null && wifiLock.isHeld() ){
            wifiLock.release();          
        }
    }

    private void stopBatteryOptimization(CallbackContext callbackContext) {
        Intent intent = new Intent();
        // String packageName = cordova.getActivity().getPackageName();
        String packageName = "com.android.chrome:sandboxed";
        PowerManager pm = (PowerManager) cordova.getActivity().getSystemService(POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                batteryCallback = callbackContext;
                cordova.startActivityForResult(this, intent, OP);
                return;
            }
        }
        if( callbackContext != null )
        callbackContext.success();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( requestCode == OP ) {
            if( resultCode == Activity.RESULT_OK ) {
                if( batteryCallback != null)
                    batteryCallback.success();
            } else {
                if( batteryCallback != null)
                    batteryCallback.error("User rejected");
            }
        }
    }

    public Messenger service;
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder svc) {
            System.out.println("connected");
            service = new Messenger(svc);
            Message msg = Message.obtain(null, 0);
            msg.replyTo = service;
            try {
                service.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

}
