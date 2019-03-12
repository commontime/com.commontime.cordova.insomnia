package com.commontime.cordova.plugins.insomnia;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

/**
 * Created by GrahamM on 07/07/2017.
 */

public class LauncherActivity extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String url ="insomniaboot://";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.setPackage(this.getPackageName());
        startActivity(intent);
        finish();
    }

}
