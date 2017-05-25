package com.commontime.cordova.plugins.insomnia;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RestarterService extends Service {
    private Messenger messenger = new Messenger(new IncomingHandler());
    private CountDownLatch cdl;

    public RestarterService() {

    }

    public void onCreate() {
        Intent i = new Intent(this, RestarterService.class);
        this.startService(i);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!MonitorSingleton.getInstance().connected) {
            Intent i = new Intent("com.commontime.cordova.insomnia.BOOT");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setPackage(RestarterService.this.getPackageName());
            startActivity(i);
        }
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        MonitorSingleton.getInstance().connected = true;
        return messenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        MonitorSingleton.getInstance().connected = false;
        return false;
    }

    private class IncomingHandler extends Handler {
    }
}