package com.bfy.movieplayerplus.event.base;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXX
 * @company    : XXXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * @e-mail     : jinfu123.-@163.com
 * @createDate : 2017/4/18
 * @modifyDate : 2017/4/18
 * version     : 1.0
 * @desc       : 事件回调接口
 * </pre>
 */

public interface EventCallback<V, T> {
     void call(EventBuilder.Event<V, T> event);
}
