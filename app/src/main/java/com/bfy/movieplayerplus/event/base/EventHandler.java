package com.bfy.movieplayerplus.event.base;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXX
 * @company    : XXXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * @e-mail     : jinfu123.-@163.com
 * @createDate : 2017/4/18
 * @modifyDate : 2017/4/18
 * @version    : 1.1
 * @desc       : 事件句柄，处理事件传递操作，部分功能需根据后续业务扩展
 * </pre>
 */

public class EventHandler {

    private static EventHandler mInstance;

    public static EventHandler getInstance(){
        if(mInstance == null){
            mInstance = new EventHandler();
        }
        return mInstance;
    }

    @SuppressWarnings("unchecked")
    public Subscription send(EventBuilder.Event event){
        if (event.getInterceptor() != null
                && event.getInterceptor().intercept(Interceptor.EventState.SEND, event)) {
            return new Unsubscribed(event);
        }
        event.target = this;
        if (event.isSent) {
            //一次事件只能发送一次，发送多次抛出异常，如需要重复发送同样事件，可以调用
            //event.copy()复制一个新的event对象再重新发送
            throw new EventAlreadySentException("event repeat send,the event object connot send repeatedly!");
        }

        Subscription subscription = handleEvent(event);
        event.isSent = true;
        return subscription;
    }

    @SuppressWarnings("unchecked")
    protected Subscription handleEvent(EventBuilder.Event event){

        if (event == null) {
            throw new NullPointerException("the event is null, cannot send.");
        }

        EventDispatcher dispatcher;
        if (event.getDispatcher() != null) {
            dispatcher = event.getDispatcher();
        } else {
            dispatcher = EventFactory.getEventDispatcherFactory()
                    .getEventDispatcher(event);
        }
        if (event.getInterceptor() != null
                && event.getInterceptor().intercept(Interceptor.EventState.DISPATCH, event)) {
            return new Unsubscribed(event);
        }
        if (dispatcher != null) {
            return dispatcher.dispatch(event);
        } else {
            return new BaseEventDispatcher().dispatch(event);
        }
    }

}
