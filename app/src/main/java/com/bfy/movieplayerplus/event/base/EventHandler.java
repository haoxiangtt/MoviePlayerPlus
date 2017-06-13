package com.bfy.movieplayerplus.event.base;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　彩讯科技股份有限公司
 * @company    : 彩讯科技股份有限公司
 * @author     : OuyangJinfu
 * @e-mail     : ouyangjinfu@richinfo.cn
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

    public void send(EventBuilder.Event event){
        event.target = this;
        event.send();
    }

    public void handleEvent(EventBuilder.Event event){
        EventDispatcher dispatcher = EventFactory.getEventDispatcherFactory()
            .getEventDispatcher(event);
        if (dispatcher != null) {
            dispatcher.dispatch(event);
        }
    }

}
