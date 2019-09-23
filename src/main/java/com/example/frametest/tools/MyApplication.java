package com.example.frametest.tools;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.example.frametest.fontsliderbar.PreferencesHelper;

import java.util.Stack;

import interfaces.heweather.com.interfacesmodule.view.HeConfig;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;

import static com.mob.tools.utils.DeviceHelper.getApplication;

public class MyApplication extends Application {
    private static Context context;
    private static MyApplication instance;
    private static String moublefhoneUser;
    private PreferencesHelper ph;
    private static Stack<Activity> activityStack;
    // 单例模式获取唯一的Application实例
    public static Application getInstance() {
        return instance.getApplication();
    }

    public static MyApplication getMyInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        //获取去全局context
        context = getApplicationContext();
        super.onCreate();
        //实例个人Application类
        instance = this;
        HeConfig.init("HE1909171034071268","54bd7c993b3b4d4588a5258b1e7bc85d");
        HeConfig.switchToFreeServerNode();
        ph = new PreferencesHelper(getApplication(), "test");
        MultiDex.install(this);
    }
    public PreferencesHelper getPreferencesHelper() {
        return ph;
    }
    /**
     * @return 获取字体缩放比例
     */
    public float getFontScale() {
        int currentIndex = ph.getValueInt("currentIndex", 0);
        return 1 + currentIndex * 0.1f;
    }

    private Application getApplication() {
        return this;
    }
    /**
     * add Activity 添加Activity到栈
     */
    public static void addActivity(Activity activity) {
        if (activityStack == null) {
            activityStack = new Stack<Activity>();
        }
        activityStack.add(activity);
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
    /**
     * 结束所有Activity
     */
    public static void finishAllActivity() {
        for (int i = 0, size = activityStack.size(); i < size; i++) {
            if (null != activityStack.get(i)) {
                activityStack.get(i).finish();
            }
        }
        activityStack.clear();
    }
    /*protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        try {
            MultiDex.install(this);
        } catch (RuntimeException e) {
            // Do something with it. At least log it
            e.printStackTrace();
        }

    }*/


}
