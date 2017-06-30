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
 * @version    : 2.0
 * @desc       : 分发器基类，做了一些分发器必要的工作。
 * </pre>
 */

public class BaseEventDispatcher implements EventDispatcher {

    /*protected void onError(EventBuilder.Event event, int errorType) {
        LogUtils.e(TAG, "event dispatch error, errorType(" + errorType + ").");
    }*/

    protected Subscription onSchedule(final EventBuilder.Event event) {
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
            final EventReceiver receiver = register.getReceiver(mEvent.receiverKey);
            if ( receiver == null) {
                LogUtils.e(Scheduler.TAG, "event scheduler error : receiver is null, receiver receiverKey = '" + mEvent.receiverKey + "'.");
                return;
            }
            if (mEvent.getInterceptor() != null
                    && mEvent.getInterceptor().intercept(Interceptor.EventState.BEGIN_WORKING, mEvent)) {
                return;
            }

            receiver.onReceive(mEvent);

            if (mEvent.getInterceptor() != null
                    && mEvent.getInterceptor().intercept(Interceptor.EventState.END_WORKING, mEvent)) {
                return;
            }
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
                        if (mEvent.getInterceptor() != null
                                && mEvent.getInterceptor().intercept(Interceptor.EventState.CALLBACK, mEvent)) {
                            return;
                        }
                        mCallback.call(mEvent);
                    }
                }
            });
            worker.schedule();
        }
    }



}
