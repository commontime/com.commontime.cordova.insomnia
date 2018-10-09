package com.commontime.cordova.plugins.insomnia;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

public class BackgroundOperationsManagerService extends Service {
    private static final int ALARM_REQUEST_CODE = 1234;
    private static final String TAG = "BackgroundOperationsManagerService";

    public BackgroundOperationsManagerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void onCreate() {
        Log.d(TAG, "insomniatest:BackgroundOperationsManagerService:onCreate");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "insomniatest:BackgroundOperationsManagerService:onStartCommand, intent: " + intent);
        
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent intent2 = new Intent(this, AlarmBroadCastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                ALARM_REQUEST_CODE, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT >= 23)
        {
            Log.d(TAG, "insomniatest:BackgroundOperationsManagerService:Setting alarm >= 23");
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+30000, pendingIntent);
        }
        else if (Build.VERSION.SDK_INT >= 19)
        {
            Log.d(TAG, "insomniatest:BackgroundOperationsManagerService:Setting alarm >= 19");
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+30000, pendingIntent);
        }
        else
        {
            Log.d(TAG, "insomniatest:BackgroundOperationsManagerService:Setting alarm < 19");
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+30000, pendingIntent);
        }
        
        return Service.START_NOT_STICKY;
    }


}
