package com.bfy.movieplayerplus.event.base;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXX
 * @company    : XXXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * @e-mail     : jinfu123.-@163.com
 * @createDate : 2017/6/28 0028
 * @modifyDate : 2017/6/28 0028
 * @version    : 1.0
 * @desc       : 对订阅接口做空实现的被取消订阅的类。
 * </pre>
 */

public class Unsubscribed implements Subscription {

    private EventBuilder.Event mEvent;

    public Unsubscribed(){}

    public Unsubscribed(EventBuilder.Event event) {
        mEvent = event;
    }

    @Override
    public void unsubscribe() {

    }

    @Override
    public boolean isUnsubscribed() {
        return true;
    }

    @Override
    public <V, T> EventBuilder.Event<V, T> getEvent() {
        return mEvent;
    }
}
