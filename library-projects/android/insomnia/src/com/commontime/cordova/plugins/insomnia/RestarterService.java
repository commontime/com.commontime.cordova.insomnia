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
        // Wait and see if it connects
        cdl = new CountDownLatch(1);

        Handler h = new Handler();
        h.post(new Runnable() {
            @Override
            public void run() {
                try {
                    cdl.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                }
                if(!MonitorSingleton.getInstance().connected) {
                    Intent i = new Intent("com.commontime.cordova.insomnia.BOOT");
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.setPackage(RestarterService.this.getPackageName());
                    startActivity(i);
                }
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        cdl.countDown();
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        MonitorSingleton.getInstance().connected = true;
        cdl.countDown();
        return messenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        MonitorSingleton.getInstance().connected = false;
        cdl.countDown();
        return false;
    }

    private class IncomingHandler extends Handler {
    }
}