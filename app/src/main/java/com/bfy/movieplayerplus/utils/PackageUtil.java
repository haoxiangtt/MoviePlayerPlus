package com.bfy.movieplayerplus.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.lang.ref.WeakReference;

/**
 * author : Pan
 * time   : 2017/5/10
 * desc   : xxxx描述
 * version: 1.0
 * <p>
 * Copyright: Copyright (c) 2017
 * Company:深圳彩讯科技有限公司
 */

public class PackageUtil {
    private static final String TAG = "PackageUtil";
    private static PackageUtil instance;
    private PackageManager packageManager;
    private static WeakReference<Context> context;

    private PackageUtil(Context context) {
        this.context = new WeakReference<Context>(context.getApplicationContext());
    }

    public static PackageUtil getInstance(Context context) {
        if (instance == null || PackageUtil.context.get() == null) {
            instance = new PackageUtil(context);
            instance.getPackageManager();
        }
        return instance;
    }

    /**
     * 获取包管理器
     *
     * @return
     */
    public PackageManager getPackageManager() {
        if (packageManager == null && context.get() != null) {
            packageManager = context.get().getPackageManager();
        }
        return packageManager;
    }

    /**
     * 获取包信息
     *
     * @return
     */
    public PackageInfo getPackageInfo() {
        try {
            if (context.get() != null) {
                return packageManager.getPackageInfo(
                        context.get().getPackageName(), 0);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取包名
     *
     * @return
     */
    public String getPackageName() {
        return getPackageInfo().packageName;
    }

    /**
     * 获取版本名称
     *
     * @return
     */
    public String getVersionName() {
        return getPackageInfo().versionName;
    }

    /**
     * 获取版本号
     *
     * @return
     */
    public int getVersionCode() {
        return getPackageInfo().versionCode;
    }

}
