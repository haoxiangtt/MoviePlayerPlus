package com.bfy.movieplayerplus.utils;

import android.Manifest.permission;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by Suma on 2016/8/16.
 */
public class PermissionUtils {

    /**
     * check current sdk if >= 23
     * @return true is need requestPermission
     */
    public static boolean isNeedRequestPermission(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     *
     * @param context
     * @param permission  {@link permission} or {@link android.Manifest.permission_group}
     * @return true need request Permission
     */
    public static boolean  checkSelfPermission(Context context, String permission){
       int result = ContextCompat.checkSelfPermission(context,permission);
        return result != PackageManager.PERMISSION_GRANTED;
    }

    /**
     * request Some ManiFest.Permission </br>
     * use this method you need override {@link Activity#onRequestPermissionsResult(int, String[], int[])}
     * @param activity
     * @param permissions {@link permission} or {@link android.Manifest.permission_group}
     * @param requestCode can yo do some for onAactivityResult
     */
    public static void requestPermission(Activity activity, String[] permissions, int requestCode){
        ActivityCompat.requestPermissions(activity,permissions,requestCode);
    }

    /**
     * user deny and never ask again</br>
     * 当用户勾选了申请权限时不再显示，并且拒绝授权时 ，调用该方法检测，返回false 则用户不授予权限，需要弹窗告知用户需要权限的理由，并让其前往系统设置
     * @param activity
     * @param permission
     * @return
     */
    public static boolean shouldShowRequestPermissiomRationale(Activity activity, String permission){
        return ActivityCompat.shouldShowRequestPermissionRationale(activity,permission);
    }

}
