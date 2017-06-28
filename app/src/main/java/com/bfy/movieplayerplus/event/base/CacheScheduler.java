package com.bfy.movieplayerplus.event.base;

import com.bfy.movieplayerplus.event.base.EventBuilder;
import com.bfy.movieplayerplus.event.base.EventFactory;
import com.bfy.movieplayerplus.event.base.EventReceiver;
import com.bfy.movieplayerplus.event.base.EventRegister;
import com.bfy.movieplayerplus.event.base.Platform;
import com.bfy.movieplayerplus.event.base.Scheduler;
import com.bfy.movieplayerplus.event.base.Subscription;
import com.bfy.movieplayerplus.event.base.Unsubscribed;
import com.bfy.movieplayerplus.utils.LogUtils;

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
        public Subscription schedule() {
            if (mUnsubscribe) {
                return new Unsubscribed();
            }

            ScheduledAction action = new ScheduledAction(mEvent, mPlatform, mWork);
            mPlatform.execute(action);
            return action;
        }

        @Override
        public Subscription schedule(long delayTime, TimeUnit unit) {
            //TODO unimpliment 延时调度方法
            return null;
        }
    }

    static class ScheduledAction implements Runnable, Subscription{

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
            mPlatform.cancel(mEvent.sessionId);
        }

        @Override
        public boolean isUnsubscribed() {
            return mUnsubscribe;
        }

        @Override
        public void run() {
            if (mWork != null) {
                mWork.run();
            }
        }
    }


}
