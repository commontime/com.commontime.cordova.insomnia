package com.commontime.cordova.plugins.insomnia;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;

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

    String wakeLockTag = UUID.randomUUID().toString();
    private PowerManager.WakeLock lock;
    private CallbackContext batteryCallback;

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

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
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
        }

        return false;
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

}