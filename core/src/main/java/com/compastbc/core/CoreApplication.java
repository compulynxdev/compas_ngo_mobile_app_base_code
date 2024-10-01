package com.compastbc.core;

import android.app.Application;

import com.compastbc.core.data.AppDataManager;


/**
 * Created by hemant.
 * Date: 30/8/18
 * Time: 2:59 PM
 */

public class CoreApplication extends Application {

    public static double LATITUDE = 0.0236;
    public static double LONGITUDE = 37.9062;
    private static CoreApplication instance;
    private AppDataManager appInstance;

    public static synchronized CoreApplication getInstance() {
        if (instance != null) {
            return instance;
        }
        return new CoreApplication();
    }

    public AppDataManager getDataManager() {
        return appInstance;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        appInstance = AppDataManager.getInstance(this);
    }

}
