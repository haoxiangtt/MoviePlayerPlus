package com.bfy.movieplayerplus.event.base;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXX
 * @company    : XXXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * @e-mail     : jinfu123.-@163.com
 * @createDate : 2017/6/1 0001
 * @modifyDate : 2017/6/1 0001
 * @version    : 1.0
 * @desc       : 事件分发工厂接口
 * </pre>
 */

public interface EventDispatcherFactory {

    /**
     * 将event分发至相应的分发器(Dispatcher)
     * @param event
     */
    EventDispatcher getEventDispatcher(EventBuilder.Event event);
}
