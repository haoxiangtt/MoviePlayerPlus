package com.bfy.movieplayerplus.event.base;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXX
 * @company    : XXXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * @e-mail     : ouyangjinfu@richinfo.cn
 * @createDate : 2017/6/28 0028
 * @modifyDate : 2017/6/28 0028
 * @version    : 1.0
 * @desc       : 调度器获取类
 * </pre>
 */

public class Schedulers {

    private static final Scheduler CACHE_SCHEDELER = new CacheScheduler();

    private static final Scheduler UI_SCHEDELER = new AndroidScheduler();

    public static Scheduler cache() {
        return CACHE_SCHEDELER;
    }

    public static Scheduler ui() {
        return UI_SCHEDELER;
    }

    public static Scheduler defaultScheduler() {
        return CACHE_SCHEDELER;
    }

}
