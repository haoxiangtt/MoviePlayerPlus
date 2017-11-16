package com.bfy.movieplayerplus.utils;

import android.util.Log;

import com.bfy.movieplayerplus.BuildConfig;


public class LogUtils {
    public static boolean isDebug = BuildConfig.DEBUG;
    public static final LogUtils instance = new LogUtils();

    public static LogUtils getInstance() {
        return instance;
    }

    public static void setDebugMode(boolean isDebug) {
        LogUtils.isDebug = isDebug;
    }

    public static final void e(String tag, String msg) {
        if (isDebug)
            Log.e(tag, msg);
    }

    public static final void w(String tag, String msg) {
        if (isDebug)
            Log.w(tag, msg);
    }

    public static final void d(String tag, String msg) {
        if (isDebug)
            Log.d(tag, msg);
    }

    public static final void i(String tag, String msg) {
        if (isDebug)
            Log.i(tag, msg);
    }

    public static final void v(String tag, String msg) {
        if (isDebug)
            Log.v(tag, msg);
    }

    public static final void debug(String tag, String msg) {
        if (isDebug)
            Log.d("outer", "[" + tag + "] : " + msg);
    }
}
