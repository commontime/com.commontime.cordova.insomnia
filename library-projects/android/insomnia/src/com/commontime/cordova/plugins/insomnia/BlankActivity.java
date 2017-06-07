package com.commontime.cordova.plugins.insomnia;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by gjm on 07/06/17.
 */

public class BlankActivity extends Activity {
    @Override
    public void onResume() {
        super.onResume();
        finish();
    }
}
