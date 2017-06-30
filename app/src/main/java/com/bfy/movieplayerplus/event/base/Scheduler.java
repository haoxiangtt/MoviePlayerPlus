package com.bfy.movieplayerplus.event.base;

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
 * @desc       : Event调度器
 * </pre>
 */
public abstract class Scheduler {

    public static final String TAG = "Event";

    public abstract Worker createWorker(Object... args);

    public abstract static class Worker{

        public abstract Subscription schedule();

        public abstract Subscription schedule(long delayTime, TimeUnit unit);

    }

}
