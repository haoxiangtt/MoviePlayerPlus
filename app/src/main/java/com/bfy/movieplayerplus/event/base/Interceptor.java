package com.bfy.movieplayerplus.event.base;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXX
 * @company    : XXXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * @e-mail     : jinfu123.-@163.com
 * @createDate : 2017/6/29 0029
 * @modifyDate : 2017/6/29 0029
 * @version    : 1.0
 * @desc       : 事件拦截器
 * </pre>
 */
public interface Interceptor<T> {

    enum EventState {SEND, DISPATCH, SCHEDULE, BEGIN_WORKING, WORKING, END_WORKING, CALLBACK}

    /**
     * 拦截事件处理
     * @param state 拦截的阶段
     * @param event 拦截的事件源
     * @return 返回true表示拦截，直接中断事件传递，后续步骤不在执行；返回false表示不拦截，事件将继续传递
     */
     boolean intercept(EventState state, EventBuilder.Event<T> event);

}
