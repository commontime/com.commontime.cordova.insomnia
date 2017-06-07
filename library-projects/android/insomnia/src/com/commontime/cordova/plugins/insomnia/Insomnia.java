package com.commontime.cordova.plugins.insomnia;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.UUID;

public class Insomnia extends CordovaPlugin {

    private static final String TAG = "Insomnia";

    private static final int OP = 139690458;

    private static final String ACQUIRE_WAKE_LOCK = "acquireWakeLock";
    private static final String RELEASE_WAKE_LOCK = "releaseWakeLock";
    private static final String STOP_BATTERY_OPTIMIZATION = "stopBatteryOptimization";
    private static final String IS_IGNORING_BATTERY_OPTIMIZATION = "isIgnoringBatteryOptimization";
    private static final String SWITCH_ON_SCREEN_AND_FOREGROUND = "switchOnScreenAndForeground";

    String wakeLockTag = UUID.randomUUID().toString();
    private PowerManager.WakeLock lock;
    private CallbackContext batteryCallback;
    private ForegroundService mForegroundService;
    private boolean mBound;
    private String fgServiceMainString;
    private String fgServiceSubString;
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

    @Override
    protected void pluginInitialize() {
        ApplicationInfo ai = null;
        try {
            ai = cordova.getActivity().getPackageManager().getApplicationInfo(cordova.getActivity().getPackageName(), PackageManager.GET_META_DATA);
            Bundle aBundle = ai.metaData;
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

                fgServiceMainString = aBundle.getString("fgServiceMainString", "Foreground Service");
                fgServiceSubString = aBundle.getString("fgServiceSubString", "Preventing app from being stopped");

                Intent intent = new Intent(cordova.getActivity(), ForegroundService.class);
                cordova.getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
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

    @Override
    public void onResume(boolean multiTask) {
        super.onResume(multiTask);
        Window window = cordova.getActivity().getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cordova.getActivity().unbindService(connection);
    }

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
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
            switchOnScreenAndForeground(callbackContext);
            return true;
        }

        return false;
    }

    private void switchOnScreenAndForeground(final CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Intent i2 = new Intent("com.commontime.cordova.plugins.insomnia.BlankActivity");
                i2.setPackage(cordova.getActivity().getPackageName());
                cordova.getActivity().startActivity(i2);

                Intent i = new Intent("com.commontime.cordova.insomnia.BOOT");
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                i.setPackage(cordova.getActivity().getPackageName());
                cordova.getActivity().startActivity(i);
                callbackContext.success();
            }
        });

    }

    private void checkBatteryOptimization(CallbackContext callbackContext) {
        String packageName = cordova.getActivity().getPackageName();
        PowerManager pm = (PowerManager) cordova.getActivity().getSystemService(Context.POWER_SERVICE);
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
        PowerManager pm = (PowerManager) cordova.getActivity().getSystemService(Context.POWER_SERVICE);
        lock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, wakeLockTag);
        lock.acquire();
    }

    private void releaseWakeLock() {
        lock.release();
    }

    private void stopBatteryOptimization(CallbackContext callbackContext) {
        Intent intent = new Intent();
        String packageName = cordova.getActivity().getPackageName();
        PowerManager pm = (PowerManager) cordova.getActivity().getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
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