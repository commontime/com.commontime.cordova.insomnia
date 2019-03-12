package com.commontime.cordova.plugins.insomnia;

import android.content.Context;
import android.content.ContextWrapper;

import com.pixplicity.easyprefs.library.Prefs;

/**
 * Created by gjm on 12/07/17.
 */

class Settings {
    private static final Settings ourInstance = new Settings();

    static Settings getInstance() {
        return ourInstance;
    }

    private Settings() {
    }

    private static final String ENABLE_APP_RESTART = "enableAppRestart";

    public void setup(Context context) {
        new com.pixplicity.easyprefs.library.Prefs.Builder()
                .setContext(context.getApplicationContext())
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(context.getApplicationContext().getPackageName() + this.getClass().getCanonicalName())
                .setUseDefaultSharedPreference(true)
                .build();
    }

    public static void enableRestartService(boolean enable) {
        Prefs.putBoolean(ENABLE_APP_RESTART, enable);
    }

    public static boolean getEnableRestartService(boolean def) {
        return Prefs.getBoolean(ENABLE_APP_RESTART, def);
    }
}
