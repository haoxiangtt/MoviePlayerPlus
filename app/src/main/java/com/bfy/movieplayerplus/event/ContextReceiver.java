package com.bfy.movieplayerplus.event;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import java.io.Serializable;

import com.bfy.movieplayerplus.event.base.EventBuilder;
import com.bfy.movieplayerplus.event.base.EventReceiver;
import com.bfy.movieplayerplus.event.base.EventRegister;
import com.bfy.movieplayerplus.utils.LogUtils;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　彩讯科技股份有限公司
 * @company    : 彩讯科技股份有限公司
 * @author     : OuyangJinfu
 * @e-mail     : ouyangjinfu@richinfo.cn
 * @createDate : 2017/4/19
 * @modifyDate : 2017/4/19
 * @version    : 1.0
 * @desc       : 页面跳转、广播发送、服务开启的模型（核心业务模型）
 * </pre>
 */

public final class ContextReceiver implements EventReceiver, EventRegister {

    private static final boolean DEBUG = LogUtils.isDebug;

    private static final String TAG = "ContextReceiver";

    public static final int TYPE_GO_ACTIVITY = 0;
    public static final int TYPE_SEND_BROADCAST = 1;
    public static final int TYPE_START_SERVICE = 2;

    public static final String KEY_INTENT = "intent_intent";
    public static final String KEY_BUNDLE = "intent_bundle";
    public static final String KEY_CLASS = "intent_class";
    public static final String KEY_ACTION = "intent_action";
    public static final String KEY_DATA = "intent_data";
    public static final String KEY_CATEGORY = "intent_category";

    public static final String KEY_LOCAL_BROACAST = "intent_is_local_broadcasr";

    public static final String KEY_STICKY_BROACAST = "intent_is_sticky_broadcast";

    private static ContextReceiver mInstance;

    private ContextReceiver(){

    }

    public static EventReceiver getReceiverInstance() {
        if (mInstance == null) {
            mInstance = new ContextReceiver();
        }
        return mInstance;
    }

    public static EventRegister getRegisterInstance() {
        if (mInstance == null) {
            mInstance = new ContextReceiver();
        }
        return mInstance;
    }

    protected void goActivity(EventBuilder.Event ev){
        Intent intent = getIntent(ev);
        if (ev.reference != null && ev.reference.get() != null) {
            ev.reference.get().startActivity(intent);
        } else {
            LogUtils.e(TAG, "ev.reference.get() is null,cannot start activity!");
            if (DEBUG) {
                throw new NullPointerException("ev.reference.get() is null,cannot start activity!");
            }
        }

    }

    protected void sendBroadcast(EventBuilder.Event ev){
        Intent intent = getIntent(ev);
        if (ev.reference.get() != null) {
            if (ev.requestBundle.getBoolean(KEY_LOCAL_BROACAST, false)) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(ev.reference.get());
                lbm.sendBroadcast(intent);
            } else {
                Context context = ev.reference.get();
                if (!ev.requestBundle.getBoolean(KEY_STICKY_BROACAST, false)) {
                    context.sendBroadcast(intent);
                } else {
                    context.sendStickyBroadcast(intent);
                }
            }
        } else {
            LogUtils.e(TAG, "ev.reference.get() is null,cannot send broadcast!");
            if (DEBUG) {
                throw new NullPointerException("ev.reference.get() is null,cannot send broadcast!");
            }
        }
    }

    protected void startService(EventBuilder.Event ev){
        Intent intent = getIntent(ev);
        if (ev.reference.get() != null) {
            ev.reference.get().startService(intent);
        } else {
            throw new NullPointerException("ev.reference.get() is null,cannot start activity!");
        }
    }

    protected Intent getIntent(EventBuilder.Event ev) {
        Intent intent = ev.requestBundle.getParcelable(KEY_INTENT);
        if (intent == null) {
            intent = new Intent();
            intent.putExtra(KEY_BUNDLE, ev.requestBundle.getBundle(KEY_BUNDLE));
            Serializable serializable = ev.requestBundle.getSerializable(KEY_CLASS);
            String action = ev.requestBundle.getString(KEY_ACTION);
            if (serializable != null) {
                intent.setClass(ev.reference.get(), (Class<?>) serializable);
                ev.requestBundle.remove(KEY_CLASS);
            } else if (!TextUtils.isEmpty(action)) {
                intent.setAction(action);
                ev.requestBundle.remove(KEY_ACTION);
            } else {
                throw new ContextNoActionException("start Context failed,there is no action or class to go!");
            }
            String data = ev.requestBundle.getString(KEY_DATA);
            if (!TextUtils.isEmpty(data)) {
                intent.setData(Uri.parse(data));
                ev.requestBundle.remove(KEY_DATA);
            }
            String category = ev.requestBundle.getString(KEY_CATEGORY);
            if (!TextUtils.isEmpty(category)) {
                intent.addCategory(category);
                ev.requestBundle.remove(KEY_CATEGORY);
            }
        } /*else {
            ev.requestBundle.remove(KEY_INTENT);
        }*/
        return intent;
    }

    @Override
    public void onReceive(EventBuilder.Event event) {
        switch (event.requestId) {
            case TYPE_GO_ACTIVITY : {
                goActivity(event);
                break;
            }
            case TYPE_SEND_BROADCAST : {
                sendBroadcast(event);
                break;
            }
            case TYPE_START_SERVICE : {
                startService(event);
                break;
            }
        }
    }

    @Override
    public EventReceiver getReceiver(String key) {
        return this;
    }

    protected static class ContextNoActionException extends RuntimeException{

        public ContextNoActionException(String str) {
            super(str);
        }

    }


}
