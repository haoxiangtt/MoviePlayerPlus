package com.bfy.movieplayerplus.model.base;

import android.content.Context;
import android.text.TextUtils;

import com.bfy.movieplayerplus.event.base.EventReceiver;
import com.bfy.movieplayerplus.event.base.EventRegister;
import com.bfy.movieplayerplus.utils.LogUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;


/**
 * 业务模型构建工厂
 * @author ouyangjinfu
 * @data on 2016/5/27.
 */
public class ModelFactory implements EventRegister{

//    private static final String KEY_CODE = "mamimamihong";
//
//    private static final String KEY_ACCOUNT_MODEL = "account_model";
//    private static final String KEY_MAIN_MODEL = "main_model";

    private static ModelFactory mModelFactory;

    private ModelFactory(){}

    public static EventRegister getRegister(){
        return getInstance();
    }

    public static ModelFactory getInstance() {
        if (mModelFactory == null) {
            mModelFactory = new ModelFactory();
        }
        return mModelFactory;
    }


    private final Map<String,Object> singleMap = new HashMap<String, Object>();

    protected <T> void generateModelProxy(Context context, Class<T> realCls, String key){

        if(!TextUtils.isEmpty(key) && singleMap.get(key) != null){
            return;
        }

        Object realObj = null;
        ModelFilter filter = null;
        Object proxy = null;
        try {
            Constructor con = realCls.getDeclaredConstructor();
            con.setAccessible(true);
            realObj = con.newInstance();
            filter = ModelFilter.obtain(realObj,context.getApplicationContext());
            proxy = Proxy.newProxyInstance(realCls.getClassLoader(),realCls.getInterfaces(),filter);

        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return;
        } catch (InstantiationException e) {
            e.printStackTrace();
            return;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return;
        }
        if(proxy != null) {
            singleMap.put(key, proxy);
        }
    }


    public <T> void registModelProxy(Context context, Class<T> cls, String key) {
        generateModelProxy(context, cls, key);
    }

    public void registModelProxy(Context context, String clsName, String key) {
        try {
            Class<?> cls = Class.forName(clsName);
            registModelProxy(context, cls, key);
        } catch (ClassNotFoundException e) {
            LogUtils.e("BaseModel", ">>>>>class '" + clsName + "'" + " was not found!");
        }
    }

    public  <T> T getModelProxy(String key) {
        if(!TextUtils.isEmpty(key) && singleMap.get(key) != null){
            return (T)singleMap.get(key);
        }
        return null;
    }


    @Override
    public EventReceiver getReceiver(String key){
        EventReceiver receiver = getModelProxy(key);
        return receiver;
    }


}
