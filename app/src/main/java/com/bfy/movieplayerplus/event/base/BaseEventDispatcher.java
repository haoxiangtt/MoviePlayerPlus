package com.bfy.movieplayerplus.event.base;

import android.text.TextUtils;

import com.bfy.movieplayerplus.utils.LogUtils;

import java.util.UUID;

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

//    public static final int DISPATCHER_ERROR_NULL_REGISTER = 0;
//    public static final int DISPATCHER_ERROR_NULL_RECEIVER = 1;

    /*protected void onError(EventBuilder.Event event, int errorType) {
        LogUtils.e(TAG, "event dispatch error, errorType(" + errorType + ").");
    }*/

    protected Subscription onSchedule(final EventBuilder.Event event) {
        /*Platform.getInstance(Platform.TYPE_UI_THREAD_POOL).execute(new Runnable() {
            @Override
            public void run() {
                receiver.onReceive(event);
            }
        });*/
        Scheduler subscriber = event.getSubscriber();
        if (event.callback != null) {
            WrapEventCallback wrapCallback = new WrapEventCallback(event);
            event.callback = wrapCallback;
        }
        Scheduler.Worker worker = subscriber.createWorker(event, new DefaultWorkRunnable(event));
        return worker.schedule();
    }

    @Override
    public Subscription dispatch(EventBuilder.Event event) {
        /*EventRegister register = EventFactory.getEventRegisterFactory()
                .getRegister(event.registerType);
        if (register == null) {
            onError(event, DISPATCHER_ERROR_NULL_REGISTER);
            return;
        }
        final EventReceiver receiver = register.getReceiver(event.ReceiverKey);
        if ( receiver == null) {
            onError(event, DISPATCHER_ERROR_NULL_RECEIVER);
            return;
        }*/
        if (event.getSubscriber() == null) {
            event.setSubscriber(Schedulers.cache());
        }
        if (event.getObserver() == null) {
            event.setObserver(Schedulers.ui());
        }
        if (TextUtils.isEmpty(event.sessionId)) {
            String uuid = UUID.randomUUID().toString();
            event.sessionId = uuid.replaceAll("-", "");
        }
        return onSchedule(event);
    }

    protected static class DefaultWorkRunnable implements Runnable {

        private EventBuilder.Event mEvent;

        public DefaultWorkRunnable(EventBuilder.Event event) {
            mEvent = event;
        }

        @Override
        public void run() {
            if (mEvent.getReceiver() != null) {
                mEvent.getReceiver().onReceive(mEvent);
                return;
            }
            EventRegister register;
            if (mEvent.getRegister() != null) {
                register = mEvent.getRegister();
            } else {
                register = EventFactory.getEventRegisterFactory()
                        .getRegister(mEvent.registerType);
            }
            if (register == null) {
                LogUtils.e(Scheduler.TAG, "event scheduler error : register is null, register registerType = " + mEvent.registerType + ".");
                return;
            }
            final EventReceiver receiver = register.getReceiver(mEvent.ReceiverKey);
            if ( receiver == null) {
                LogUtils.e(Scheduler.TAG, "event scheduler error : receiver is null, receiver ReceiverKey = '" + mEvent.ReceiverKey + "'.");
                return;
            }
            receiver.onReceive(mEvent);
        }
    }

    protected  static class WrapEventCallback implements EventCallback{

        private EventCallback mCallback;
        private EventBuilder.Event mEvent;

        public WrapEventCallback (EventBuilder.Event event) {
            mEvent = event;
            mCallback = event.callback;
        }

        @Override
        public void call(EventBuilder.Event event) {
            Scheduler observer = event.getObserver();
            Scheduler.Worker worker = observer.createWorker(event, new Runnable() {
                @Override
                public void run() {
                    mEvent.callback = mCallback;
                    if (mCallback != null) {
                        mCallback.call(mEvent);
                    }
                }
            });
            worker.schedule();
        }
    }



}
