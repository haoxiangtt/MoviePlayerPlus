package com.bfy.movieplayerplus;

import android.app.Application;
import android.content.Context;

import com.bfy.movieplayerplus.event.ContextEventDispatcher;
import com.bfy.movieplayerplus.event.ContextReceiver;
import com.bfy.movieplayerplus.event.DefaultEventDispatcher;
import com.bfy.movieplayerplus.event.base.EventFactory;
import com.bfy.movieplayerplus.http.RequestManager;
import com.bfy.movieplayerplus.model.base.ModelFactory;
import com.bfy.movieplayerplus.model.bizImpl.MainModel;
import com.bfy.movieplayerplus.utils.Constant;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXX
 * @company    : XXXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * @e-mail     : jinfu123.-@163.com
 * @createDate : 2017/6/12 0012
 * @modifyDate : 2017/6/12 0012
 * @version    : 1.0
 * @desc       :
 * </pre>
 */

public class MyApplication extends Application {


    private static Context mContext;
    private static RefWatcher mRefWatcher;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        //初始化volley网络框架
        RequestManager.getInstance().init(this);
        //注册业务模型
        ModelFactory.getInstance().registModelProxy(this, MainModel.class, Constant.MAIN_MODEL);

        //注册分发器和注册者业务类后，Event的registerType和receiverKey参数才能生效.
        //将业务模型工厂注册到事件处理工厂中
        EventFactory.getEventRegisterFactory().registRegister(Constant.EVENT_TYPE_MODEL, ModelFactory.getRegister());
        EventFactory.getEventRegisterFactory().registRegister(Constant.EVENT_TYPE_CONTEXT, ContextReceiver.getRegisterInstance());
        //为业务工厂分配分发器
        EventFactory.getEventRegisterFactory().registDispatcher(Constant.EVENT_TYPE_MODEL, new DefaultEventDispatcher());
        EventFactory.getEventRegisterFactory().registDispatcher(Constant.EVENT_TYPE_CONTEXT, new ContextEventDispatcher());

        mRefWatcher = LeakCanary.install(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        mContext = null;
        mRefWatcher = null;
    }

    public static final Context getContext(){
        return mContext;
    }

    public static final RefWatcher getRefWatcher () {
        return mRefWatcher;
    }

}
