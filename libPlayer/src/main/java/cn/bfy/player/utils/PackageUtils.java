package cn.bfy.player.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import java.lang.reflect.Field;
import java.security.MessageDigest;

/**
 * 获取app包信息
 */
public class PackageUtils {

    private static final String TAG = "PackageUtils";

    /**
     * 获取包管理器
     *
     * @return
     */
    public static PackageManager getPackageManager(Context context) {
        return context.getPackageManager();
    }

    /**
     * 获取包信息
     *
     * @return
     */
    public static PackageInfo getPackageInfo(Context context) {
        try {
            PackageInfo packInfo = getPackageManager(context).getPackageInfo(
                    context.getPackageName(), 0);
            return packInfo;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取包名
     *
     * @return
     */
    public static String getPackageName(Context context) {
        return getPackageInfo(context).packageName;
    }

    public static Drawable getPackegeIcon(Context context){
        return getPackageInfo(context).
                applicationInfo.loadIcon(getPackageManager(context));
    }

    /**
     * 获取版本名称
     *
     * @return
     */
    public static String getVersionName(Context context) {
        return getPackageInfo(context).versionName;
    }

    /**
     * 获取应用渠道号
     *
     * @param meta_data_key
     * @return
     */
    public static final String getMetaData(Context context, String meta_data_key) {
        if(context == null) { return ""; }
        try {
            PackageManager packageManager = getPackageManager(context);
            ApplicationInfo applicationInfo = packageManager
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            if (applicationInfo != null && applicationInfo.metaData != null) {
                return String.valueOf(applicationInfo.metaData
                        .get(meta_data_key));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * 获取app签名信息
     * @return
     */
    public static String getSingInfo(Context context) {
        if (context == null) { return ""; }
        try {
            PackageInfo packageInfo = getPackageManager(context).getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            Signature[] signs = packageInfo.signatures;
            Signature sign = signs[0];
            return getMessageDigest(sign.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 解析应用签名md5值
     *
     * @param paramArrayOfByte
     * @return
     */
    private static String getMessageDigest(byte[] paramArrayOfByte) {
        char[] arrayOfChar1 = {48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 97, 98,
                99, 100, 101, 102};
        try {
            MessageDigest localMessageDigest = MessageDigest.getInstance("MD5");
            localMessageDigest.update(paramArrayOfByte);
            byte[] arrayOfByte = localMessageDigest.digest();
            int i = arrayOfByte.length;
            char[] arrayOfChar2 = new char[i * 2];
            int j = 0;
            int k = 0;
            while (true) {
                if (j >= i)
                    return new String(arrayOfChar2);
                int m = arrayOfByte[j];
                int n = k + 1;
                arrayOfChar2[k] = arrayOfChar1[(0xF & m >>> 4)];
                k = n + 1;
                arrayOfChar2[n] = arrayOfChar1[(m & 0xF)];
                j++;
            }
        } catch (Exception localException) {
        }
        return null;
    }


    /**
     * 动态获取资源id
     *
     * @param context activity界面或者application
     * @param name    资源名
     * @param defType 资源所属的类 drawable, id, string, layout等
     * @return 资源id
     */
    public static int getIdentifier(Context context, String name, String defType) {
        int id = context.getResources().getIdentifier(name, defType,
                context.getPackageName());
        return id;
    }

    /**
     * 动态获取string资源id
     *
     * @param context
     * @param name
     * @return
     */
    public static int getIdentifierString(Context context, String name) {
        int id = getIdentifier(context, name, "string");
        if (id == 0) {
            throw new Resources.NotFoundException(name);
        }
        return id;
    }

    /**
     * 动态获取id资源id
     *
     * @param context
     * @param name
     * @return
     */
    public static int getIdentifierId(Context context, String name) {
        int id = getIdentifier(context, name, "id");
        if (id == 0) {
            throw new Resources.NotFoundException(name);
        }
        return id;
    }

    /**
     * 动态获取layout资源id
     *
     * @param context
     * @param name
     * @return
     */
    public static int getIdentifierLayout(Context context, String name) {
        int id = getIdentifier(context, name, "layout");
        if (id == 0) {
            throw new Resources.NotFoundException(name);
        }
        return id;
    }


    /**
     * 动态获取drawable资源id
     *
     * @param context
     * @param name
     * @return
     */
    public static int getIdentifierDrawable(Context context, String name) {
        int id = getIdentifier(context, name, "drawable");
        if (id == 0) {
            throw new Resources.NotFoundException(name);
        }
        return id;
    }

    /**
     * 动态获取anim资源id
     *
     * @param context
     * @param name
     * @return
     */
    public static int getIdentifierAnim(Context context, String name) {
        int id = getIdentifier(context, name, "anim");
        if (id == 0) {
            throw new Resources.NotFoundException(name);
        }
        return id;
    }

    public static int[] getStyleableIntArrayIds(Context context, String name){
        return (int[])getResourceId(context, name, "styleable");
    }

    public static int getStyleableResId(Context context, String name) {
        return Integer.valueOf(getResourceId(context, name, "styleable").toString()).intValue();
    }


    /**

     * 对于context.getResources().getIdentifier无法获取的数据,或者数组

     * 资源反射值

     * paramcontext

     * @param name

     * @param type

     * @return

     */

    public static Object getResourceId(Context context,String name, String type) {

        String className = context.getPackageName() +".R";

        try {

            Class<?> cls = Class.forName(className);

            for (Class<?> childClass : cls.getClasses()) {

                String simple = childClass.getSimpleName();

                if (simple.equals(type)) {

                    for (Field field : childClass.getFields()) {

                        String fieldName = field.getName();

                        if (fieldName.equals(name)) {

                            System.out.println(fieldName);

                            return field.get(null);

                        }

                    }

                }

            }

        } catch (Exception e) {

            e.printStackTrace();

        }

        return 0;

    }


}
