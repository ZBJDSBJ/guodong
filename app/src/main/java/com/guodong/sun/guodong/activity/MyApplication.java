package com.guodong.sun.guodong.activity;

import android.app.Application;

import com.guodong.sun.guodong.CrashHandler;

/**
 * Created by Administrator on 2016/9/19.
 */
public class MyApplication extends Application
{
    private static MyApplication sInstance;

    @Override
    public void onCreate()
    {
        super.onCreate();
        sInstance = this;
        CrashHandler crashHandler = CrashHandler.sInstance;
        crashHandler.init(this);
        crashHandler.setDebug(true);
    }

    public static MyApplication getsInstance()
    {
        return sInstance;
    }
}