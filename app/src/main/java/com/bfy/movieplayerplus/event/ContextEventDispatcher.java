package com.bfy.movieplayerplus.event;

import com.bfy.movieplayerplus.event.base.BaseEventDispatcher;
import com.bfy.movieplayerplus.event.base.EventBuilder;
import com.bfy.movieplayerplus.event.base.Interceptor;
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
 * @desc       : Context事件分发器是一个特殊的分发器，它会忽略Event中携带的接收者和注册者信息，转而直接调用ContextReceiver
 *              类中的单例，这样省去了去查询接收器和注册器的步骤，使得程序运行更快。所以Context分发器是适用于ContextReceiver
 *              这种类型的接收器和注册器。如果开发者使用自己定义的接收器，建议不要使用此分发器来做分发工作。
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
            if (mEvent.getInterceptor() != null
                    && mEvent.getInterceptor().intercept(Interceptor.EventState.BEGIN_WORKING, mEvent)) {
                return;
            }
            ContextReceiver.getReceiverInstance().onReceive(mEvent);
            if (mEvent.getInterceptor() != null
                    && mEvent.getInterceptor().intercept(Interceptor.EventState.END_WORKING, mEvent)) {
                return;
            }
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
