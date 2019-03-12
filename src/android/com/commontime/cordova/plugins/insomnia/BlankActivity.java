package com.commontime.cordova.plugins.insomnia;

import android.app.Activity;
import android.content.Intent;
import android.os.SystemClock;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by gjm on 07/06/17.
 */

public class BlankActivity extends Activity {

    @Override
    public void onResume() {
        super.onResume();

        // boolean turnScreenOn = getIntent().getExtras().getBoolean("turnScreenOn");
        // if(turnScreenOn) {
        //     Window window = getWindow();
        //     window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        // }
        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(100);
                BlankActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        
                        finish();
                        // Intent i = new Intent("com.commontime.cordova.insomnia.BOOT");
                        // i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        // i.setPackage(getPackageName());
                        // startActivity(i);
                    }
                });
            }
        }).start();
    }
}
