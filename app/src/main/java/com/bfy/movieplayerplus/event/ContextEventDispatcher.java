package com.bfy.movieplayerplus.event;

import com.bfy.movieplayerplus.event.base.BaseEventDispatcher;
import com.bfy.movieplayerplus.event.base.EventBuilder;
import com.bfy.movieplayerplus.event.base.Scheduler;
import com.bfy.movieplayerplus.event.base.Subscription;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXX
 * @company    : XXXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * @e-mail     : jinfu123.-@163.com
 * @createDate : 2017/4/19
 * @modifyDate : 2017/4/19
 * @version    : 1.0
 * @desc       : Context事件分发器
 * </pre>
 */

public class ContextEventDispatcher extends BaseEventDispatcher {

    public ContextEventDispatcher(){

    }

    @Override
    protected Subscription onSchedule(EventBuilder.Event event) {
        Scheduler subscriber = event.getSubscriber();
        WrapEventCallback wrapCallback = new WrapEventCallback(event);
        event.callback = wrapCallback;
        Scheduler.Worker worker = subscriber.createWorker(event, new ContextWorkRunnable(event));
        return worker.schedule();
    }

    protected static class ContextWorkRunnable implements Runnable {

        private EventBuilder.Event mEvent;

        public ContextWorkRunnable(EventBuilder.Event event) {
            mEvent = event;
        }

        @Override
        public void run() {
            ContextReceiver.getReceiverInstance().onReceive(mEvent);
        }
    }

    /*@Override
    public void dispatch(final EventBuilder.Event event) {
       Platform.getInstance(Platform.TYPE_UI_THREAD_POOL).execute(new Runnable() {
            @Override
            public void run() {
                ContextReceiver.getReceiverInstance().onReceive(event);
            }
        });
    }*/

}
