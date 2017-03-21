package com.commontime.cordova.plugins.insomnia;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;

public class ForegroundService extends Service {

    private static final int NOTIFIC = 278342742;
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        ForegroundService getService() {
            return ForegroundService.this;
        }
    }

    public ForegroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void startForeground(String main, String sub) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {

            int resId = getResources().getIdentifier("ic_stat_name", "drawable", getPackageName());
            Bitmap icon = BitmapFactory.decodeResource(getResources(), resId);

            int bigId = getResources().getIdentifier("screen", "drawable", getPackageName());
            Bitmap bigIcon = BitmapFactory.decodeResource(getResources(), resId);

            Notification notification = null;

            notification = new Notification.Builder(this)
                    .setContentTitle(main)
                    .setTicker(main)
                    .setContentText(sub)
                    .setSmallIcon(resId)
                    .setLargeIcon(bigIcon)
                    .setContentIntent(null)
                    .setOngoing(true).build();
            startForeground(NOTIFIC, notification);
        }
    }
}
