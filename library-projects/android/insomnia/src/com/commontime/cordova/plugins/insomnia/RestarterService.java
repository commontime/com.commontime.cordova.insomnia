package com.commontime.cordova.plugins.insomnia;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;

public class RestarterService extends Service {
    private Messenger messenger = new Messenger(new IncomingHandler());

    public RestarterService() {
    }

    public void onCreate() {
        // Wait and see if it connects
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!MonitorSingleton.getInstance().connected) {
                    Intent i = new Intent("com.commontime.cordova.insomnia.BOOT");
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.setPackage(RestarterService.this.getPackageName());
                    startActivity(i);
                }
            }
        }, 1000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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