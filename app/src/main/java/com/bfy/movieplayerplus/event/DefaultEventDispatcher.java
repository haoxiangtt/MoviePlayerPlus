package com.bfy.movieplayerplus.event;


import org.json.JSONException;

import com.bfy.movieplayerplus.event.base.BaseEventDispatcher;
import com.bfy.movieplayerplus.event.base.EventBuilder;
import com.bfy.movieplayerplus.event.base.EventFactory;
import com.bfy.movieplayerplus.event.base.EventReceiver;
import com.bfy.movieplayerplus.event.base.EventRegister;
import com.bfy.movieplayerplus.model.base.BaseModel;
import com.bfy.movieplayerplus.utils.LogUtils;
import com.bfy.movieplayerplus.utils.Platform;
import com.bfy.movieplayerplus.utils.Constant;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXX
 * @company    : XXXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * @e-mail     : jinfu123.-@163.com
 * @createDate : 2017/4/18
 * @modifyDate : 2017/4/18
 * @version    : 1.0
 * @desc       : 默认的事件分发器
 * </pre>
 */

public class DefaultEventDispatcher extends BaseEventDispatcher {


    public DefaultEventDispatcher(){

    }

    @Override
    public void onError(EventBuilder.Event event, int errorType) {
        if (errorType == DISPATCHER_ERROR_NULL_REGISTER) {
            if (event.callback != null) {
                EventJsonObject result = new EventJsonObject();
                try {
                    result.put(BaseModel.KEY_RESULT_CODE, Constant.ResponseCode.CODE_MODEL_UNREGIST);
                    result.put(BaseModel.KEY_DESC, "业务工厂(" + event.type + ")未注册，" +
                            "无法正常使用!");
                    event.callback.call(event);
                } catch (JSONException e) {
                    LogUtils.e(TAG, e.getMessage());
                }
            }
        } else if (errorType == DISPATCHER_ERROR_NULL_RECEIVER) {
            if (event.callback != null) {
                //to do by ouyangjinfu 已经实现
                EventJsonObject result = new EventJsonObject();
                try {
                    result.put(BaseModel.KEY_RESULT_CODE, Constant.ResponseCode.CODE_MODEL_UNREGIST);
                    result.put(BaseModel.KEY_DESC, "业务接收者(" + event.key + ")未注册，" +
                            "无法正常使用!");
                    event.callback.call(event);
                } catch (JSONException e) {
                    LogUtils.e(TAG, e.getMessage());
                }
            }
        }
    }

    @Override
    public void dispatch(final EventBuilder.Event event) {
        EventRegister register = EventFactory.getEventRegisterFactory()
            .getRegister(event.type);
        if (register == null) {
            //to do by ouyangjinfu 已经实现

            return;
        }
        final EventReceiver receiver = register.getReceiver(event.key);
        if ( receiver == null) {

            return;
        }
        Platform.getInstance(Platform.TYPE_UI_THREAD_POOL).execute(new Runnable() {
            @Override
            public void run() {
                receiver.onReceive(event);
            }
        });

    }

}
