package com.commontime.cordova.plugins.insomnia;

/**
 * Created by gjm on 09/05/17.
 */
public class MonitorSingleton {
    private static MonitorSingleton ourInstance = new MonitorSingleton();

    public static MonitorSingleton getInstance() {
        return ourInstance;
    }

    private MonitorSingleton() {
    }

    public boolean connected = false;
}

