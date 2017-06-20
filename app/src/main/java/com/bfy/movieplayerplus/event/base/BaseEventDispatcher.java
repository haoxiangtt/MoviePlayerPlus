package com.bfy.movieplayerplus.event.base;

import com.bfy.movieplayerplus.utils.LogUtils;
import com.bfy.movieplayerplus.utils.Platform;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXX
 * @company    : XXXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * @e-mail     : jinfu123.-@163.com
 * @createDate : 2017/6/20 0020
 * @modifyDate : 2017/6/20 0020
 * @version    : 1.0
 * @desc       :
 * </pre>
 */

public class BaseEventDispatcher implements EventDispatcher {

    public static final int DISPATCHER_ERROR_NULL_REGISTER = 0;
    public static final int DISPATCHER_ERROR_NULL_RECEIVER = 1;

    protected void onError(EventBuilder.Event event, int errorType) {
        LogUtils.e(TAG, "event dispatch error, errorType(" + errorType + ").");
    }

    protected void onReceive(final EventReceiver receiver,final EventBuilder.Event event) {
        Platform.getInstance(Platform.TYPE_UI_THREAD_POOL).execute(new Runnable() {
            @Override
            public void run() {
                receiver.onReceive(event);
            }
        });
    }

    @Override
    public void dispatch(EventBuilder.Event event) {
        EventRegister register = EventFactory.getEventRegisterFactory()
                .getRegister(event.type);
        if (register == null) {
            onError(event, DISPATCHER_ERROR_NULL_REGISTER);
            return;
        }
        final EventReceiver receiver = register.getReceiver(event.key);
        if ( receiver == null) {
            onError(event, DISPATCHER_ERROR_NULL_RECEIVER);
            return;
        }
        onReceive(receiver, event);
    }
}
