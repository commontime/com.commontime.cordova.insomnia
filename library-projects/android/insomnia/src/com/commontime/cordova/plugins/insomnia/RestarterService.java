package com.commontime.cordova.plugins.insomnia;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RestarterService extends Service {

    String TAG = RestarterService.class.getSimpleName();

    private Messenger messenger = new Messenger(new IncomingHandler());
    private CountDownLatch cdl;

    public RestarterService() {

    }

    public void onCreate() {
        Log.i(TAG, "onCreate");
        Intent i = new Intent(this, RestarterService.class);
        this.startService(i);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand, intent: " + intent);
        if( intent == null) {
            Intent i = new Intent("com.commontime.cordova.insomnia.BOOT");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setPackage(RestarterService.this.getPackageName());
            startActivity(i);
        }
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        return messenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind");
        System.exit(0);
        return false;
    }

    private class IncomingHandler extends Handler {
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
    }
}