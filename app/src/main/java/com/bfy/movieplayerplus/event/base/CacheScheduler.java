package com.bfy.movieplayerplus.event.base;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXX
 * @company    : XXXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * @e-mail     : jinfu123.-@163.com
 * @createDate : 2017/6/28 0028
 * @modifyDate : 2017/6/28 0028
 * @version    : 1.0
 * @desc       : 可缓存线程调度器，最终是用cacheExecutor进行任务调度。
 * </pre>
 */

public class CacheScheduler extends Scheduler {

    @Override
    public Worker createWorker(Object... args) {
        Platform platform = Platform.getInstance(Platform.TYPE_CACHE_THREAD_POOL);
        EventBuilder.Event event;
        Runnable work;
        if (args != null && args.length == 2) {
            if (args[0] instanceof EventBuilder.Event) {
                event = (EventBuilder.Event) args[0];
            }else {
                throw new ClassCastException("the args[0] cannot cast to Event object.");
            }
            if (args[1] instanceof Runnable) {
                work = (Runnable) args[1];
            }else {
                throw new ClassCastException("the args[1] cannot cast to Runnable object.");
            }
        }else {
            throw new NullPointerException("the args is null, or size is not 2.");
        }
        return new CacheWorker(platform, event, work);
    }

    static class CacheWorker extends Worker implements Subscription{

        private EventBuilder.Event mEvent = null;

        private boolean mUnsubscribe = false;

        private Platform mPlatform = null;

        private Runnable mWork = null;

        public CacheWorker(Platform platform, EventBuilder.Event event, Runnable work) {
            mPlatform = platform;
            mEvent = event;
            mWork = work;
        }

        @Override
        public void unsubscribe() {
            mUnsubscribe = true;
            mEvent.setUnsubscribe(true);
            mPlatform.cancel(mEvent.sessionId);
        }

        @Override
        public boolean isUnsubscribed() {
            return mUnsubscribe;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <V, T> EventBuilder.Event<V, T> getEvent() {
            return mEvent;
        }

        @Override
        public Subscription schedule() {
            return schedule(0, null);
        }

        @Override
        @SuppressWarnings("unchecked")
        public Subscription schedule(long delayTime, TimeUnit unit) {
            //impliment by ouyangjinfu 延时调度方法
            if (mUnsubscribe) {
                return new Unsubscribed(mEvent);
            }
            if (mEvent.getInterceptor() != null
                    && mEvent.getInterceptor().intercept(Interceptor.EventState.SCHEDULE, mEvent)) {
                return new Unsubscribed(mEvent);
            }
            ScheduledAction action = new ScheduledAction(mEvent, mPlatform, mWork);
            Timer timer = new Timer();
            if (delayTime <= 0) {
                timer.schedule(action, 0);
            } else {
                long millisDelay = delayTime;
                if (unit != null) {
                    millisDelay = unit.toMillis(delayTime);
                }
                timer.schedule(action, millisDelay);
            }
            return action;
        }
    }

    static class ScheduledAction extends TimerTask implements Subscription{

        private Platform mPlatform;
        private EventBuilder.Event mEvent;
        private boolean mUnsubscribe;
        private Runnable mWork;

        public ScheduledAction(EventBuilder.Event event, Platform platform, Runnable work) {
            mPlatform = platform;
            mEvent = event;
            mWork = work;
        }

        @Override
        public void unsubscribe() {
            mUnsubscribe = true;
            mEvent.setUnsubscribe(true);
            cancel();
            mPlatform.cancel(mEvent.sessionId);
        }

        @Override
        public boolean isUnsubscribed() {
            return mUnsubscribe;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <V, T> EventBuilder.Event<V, T> getEvent() {
            return mEvent;
        }

        @Override
        public void run() {
            if (mWork != null && !mUnsubscribe) {
                mPlatform.execute(mWork, mEvent.sessionId);
            }
        }
    }


}
