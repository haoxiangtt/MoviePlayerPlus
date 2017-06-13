package com.bfy.movieplayerplus.event.base;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　彩讯科技股份有限公司
 * @company    : 彩讯科技股份有限公司
 * @author     : OuyangJinfu
 * @e-mail     : ouyangjinfu@richinfo.cn
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
