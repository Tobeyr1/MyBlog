package com.example.frametest.tools;

import android.app.Application;
import android.content.Context;


import com.mob.MobSDK;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class MyApplication extends Application {
    private static Context context;
    private static MyApplication instance;

    @Override
    public void onCreate() {
        context = getApplicationContext();
        MobSDK.init(this);
        super.onCreate();
        instance = this;
    }
    public static MyApplication getInstance(){
        return instance;
    }

    public static Context getContext() {
        return context;
    }
}
