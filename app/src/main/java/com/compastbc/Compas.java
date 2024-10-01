package com.compastbc;

import android.content.Context;

import androidx.multidex.MultiDex;

import com.compastbc.core.CoreApplication;


/**
 * Created by hemant.
 * Date: 30/8/18
 * Time: 2:59 PM
 */

public class Compas extends CoreApplication {
    private static Compas instance;

    public static synchronized Compas getInstance() {
        if (instance != null) {
            return instance;
        }
        return new Compas();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

}
