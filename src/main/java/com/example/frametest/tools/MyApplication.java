package com.example.frametest.tools;

import android.app.Application;
import android.content.Context;

import interfaces.heweather.com.interfacesmodule.view.HeConfig;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;

public class MyApplication extends Application {
    private static Context context;
    private static MyApplication instance;
    private static String moublefhoneUser;

    @Override
    public void onCreate() {
        //获取去全局context
        context = getApplicationContext();
        super.onCreate();
        //实例个人Application类
        instance = this;
        HeConfig.init("HE1909171034071268","54bd7c993b3b4d4588a5258b1e7bc85d");
        HeConfig.switchToFreeServerNode();
    }
    public static MyApplication getInstance(){
        return instance;
    }

    public static Context getContext() {
        return context;
    }

    public static void setMoublefhoneUser(String moublefhoneUser) {
        MyApplication.moublefhoneUser = moublefhoneUser;
    }

    public static String getMoublefhoneUser() {
        return moublefhoneUser;
    }

}
