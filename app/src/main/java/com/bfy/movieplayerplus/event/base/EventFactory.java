package com.bfy.movieplayerplus.event.base;

import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXX
 * @company    : XXXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * @e-mail     : jinfu123.-@163.com
 * @createDate : 2017/6/1 0001
 * @modifyDate : 2017/6/1 0001
 * @version    : 1.0
 * @desc       : 事件处理工厂，处理相应的事件，比如分发事件处理。
 *               事件工厂运用工厂方法设计模式，在工厂中可以扩展更多的接口来处理不同的事件。
 *               此处只做了事件分发的处理和业务类型注册，待以后根据需要扩展功能
 *
 *****************************事件机制原理详细讲解********************************
 * 此事件机制模块形象地可认为是一家快递公司，每一件快递被打包成Event，我们事件机制模块主要做的事情有三件，
 * 1、登记注册物品供应商公司（就是EventRegister，一般为xxxFactory：生成receiver的工厂）;
 * 2、分配派送车俩运送快递(也就是xxxDispatcher)，每一个供应商公司对应一辆派送车，但派送车也可以不同公
 * 司一起共享;
 * 3、让收货人收到快递(也就是EventReceiver)
 * 主要成员类说明：
 * Event : 被打包成快递的物件
 * EventHandler : 快递公司的指挥部
 * EventFactory : 快递公司
 * EventRegister : 供应商公司(具体实例不属于Event模块)，每一家供应商公司都要向快递公司注册登记，所以必须实现此接口，供应商公司会告诉
 * 快递公司货物(Event)要发给哪个收件人。
 * EventReceiver : 收件人(具体实例不属于Event模块)，作为收件人必须实现此接口，在这里可以收到快递（Event）,之后根据实际需求处理快递
 * (Event)
 * </pre>
 */

public class EventFactory implements EventDispatcherFactory,
        EventRegisterFactory{

    /*public static final int TYPE_CALL_MODEL = 0;

    public static final int TYPE_EXECUTE_CONTEXT = 1;

    public static final int TYPE_CONTENT_RESOLVER = 2;*/

    private static EventFactory mInstance;

    private Map<Integer, EventRegister> registerMap;

    private Map<Integer, EventDispatcher> dispatcherMap;

    private BaseEventDispatcher mDispatcher;

    private EventFactory(){
        registerMap = new HashMap<>();
        dispatcherMap = new HashMap<>();
        mDispatcher = new BaseEventDispatcher();
    }

    public static EventDispatcherFactory getEventDispatcherFactory(){
        if (mInstance == null) {
            mInstance = new EventFactory();
        }
        return mInstance;
    }

    public static EventRegisterFactory getEventRegisterFactory(){
        if (mInstance == null) {
            mInstance = new EventFactory();
        }
        return mInstance;
    }

    @Override
    public EventDispatcher getEventDispatcher(EventBuilder.Event event) {
        EventDispatcher dispatcher = dispatcherMap.get(event.registerType);
        return dispatcher != null ? dispatcher : mDispatcher;
    }

    @Override
    public EventRegisterFactory registRegister(int type, EventRegister register) {
        if (register != null) {
            registerMap.put(type, register);
        }
        return this;
    }

    @Override
    public EventRegister getRegister(int type) {
        return registerMap.get(type);
    }

    @Override
    public EventRegisterFactory registDispatcher(int type, EventDispatcher dispatcher) {
        if (dispatcher != null) {
            dispatcherMap.put(type, dispatcher);
        }
        return this;
    }

    @Override
    public EventDispatcher getDispatcher(int type) {
        return dispatcherMap.get(type);
    }
}
