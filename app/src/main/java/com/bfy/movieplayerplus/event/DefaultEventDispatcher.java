package com.bfy.movieplayerplus.event;


import com.bfy.movieplayerplus.event.base.BaseEventDispatcher;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXX
 * @company    : XXXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * @e-mail     : jinfu123.-@163.com
 * @createDate : 2017/4/18
 * @modifyDate : 2017/4/18
 * @version    : 2.0
 * @desc       : 默认的事件分发器
 * </pre>
 */

public class DefaultEventDispatcher extends BaseEventDispatcher {

    /*@Override
    public void onError(EventBuilder.Event event, int errorType) {
        if (errorType == DISPATCHER_ERROR_NULL_REGISTER) {
            if (event.callback != null) {
                EventJsonObject result = new EventJsonObject();
                try {
                    result.put(BaseModel.KEY_RESULT_CODE, Constant.ResponseCode.CODE_MODEL_UNREGIST);
                    result.put(BaseModel.KEY_DESC, "业务工厂(" + event.registerType + ")未注册，" +
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
                    result.put(BaseModel.KEY_DESC, "业务接收者(" + event.receiverKey + ")未注册，" +
                            "无法正常使用!");
                    event.callback.call(event);
                } catch (JSONException e) {
                    LogUtils.e(TAG, e.getMessage());
                }
            }
        }
    }*/


}
