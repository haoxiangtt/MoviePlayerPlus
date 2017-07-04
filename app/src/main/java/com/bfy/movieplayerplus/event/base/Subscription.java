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
 * @desc       : 订阅接口，用于查看订阅情况
 * </pre>
 */

public interface Subscription {

    void unsubscribe();

    boolean isUnsubscribed();

    <V, T> EventBuilder.Event<V, T> getEvent();

}
