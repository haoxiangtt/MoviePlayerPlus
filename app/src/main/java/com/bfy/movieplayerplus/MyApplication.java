package com.bfy.movieplayerplus;

import android.app.Application;

import com.bfy.movieplayerplus.event.ContextEventDispatcher;
import com.bfy.movieplayerplus.event.ContextReceiver;
import com.bfy.movieplayerplus.event.DefaultEventDispatcher;
import com.bfy.movieplayerplus.event.base.EventFactory;
import com.bfy.movieplayerplus.http.RequestManager;
import com.bfy.movieplayerplus.model.base.ModelFactory;
import com.bfy.movieplayerplus.model.bizImpl.MainModel;
import com.bfy.movieplayerplus.utils.Constant;

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


    @Override
    public void onCreate() {
        super.onCreate();
        //初始化volley网络框架
        RequestManager.getInstance().init(this);
        //注册业务模型
        ModelFactory.getInstance().registModelProxy(this, MainModel.class, Constant.MAIN_MODEL);

        //将业务模型工厂注册到事件处理工厂中
        EventFactory.getEventRegisterFactory().registRegister(Constant.EVENT_TYPE_MODEL, ModelFactory.getRegister());
        EventFactory.getEventRegisterFactory().registRegister(Constant.EVENT_TYPE_CONTEXT, ContextReceiver.getRegisterInstance());
        //为业务工厂分配分发器
        EventFactory.getEventRegisterFactory().registDispatcher(Constant.EVENT_TYPE_MODEL, new DefaultEventDispatcher());
        EventFactory.getEventRegisterFactory().registDispatcher(Constant.EVENT_TYPE_CONTEXT, new ContextEventDispatcher());

    }
}
