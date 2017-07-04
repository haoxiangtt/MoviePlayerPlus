package com.bfy.movieplayerplus.event.base;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXX
 * @company    : XXXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * @e-mail     : jinfu123.-@163.com
 * @createDate : 2017/4/14 0014
 * @modifyDate : 2017/4/14 0014
 * @version    : 1.0
 * @desc       : 接收者(接收器)
 * </pre>
 */

public interface EventReceiver<V, T> {
    void onReceive(EventBuilder.Event<V, T> event);
}
