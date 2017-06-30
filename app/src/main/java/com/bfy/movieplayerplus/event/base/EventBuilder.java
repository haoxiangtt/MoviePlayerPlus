package com.bfy.movieplayerplus.event.base;

import android.content.Context;
import android.os.Bundle;

import java.lang.ref.Reference;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXX
 * @company    : XXXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * @e-mail     : jinfu123.-@163.com
 * @createDate : 2017/4/18
 * @modifyDate : 2017/4/18
 * @version    : 1.0
 * @desc       : event构造器，用于构造event事件源，为了规范开发，请不要直接构造event。
 * Event机制可以说是一个精简版rxJava，
 * </pre>
 */

public class EventBuilder {

    private Event mEvent;

    public EventBuilder() {
        mEvent = obtain();
    }

    protected final Event obtain(){
        return Event.obtain();
    }

    public static final void release(Event ev){
        ev.release();
    }

    public EventBuilder type(int type) {
        mEvent.registerType = type;
        return this;
    }

    public EventBuilder key(String key) {
        mEvent.receiverKey = key;
        return this;
    }

    public EventBuilder requestId(int requestId) {
        mEvent.requestId = requestId;
        return this;
    }

    public EventBuilder sessionId(String sessionId) {
        mEvent.sessionId = sessionId;
        return this;
    }

    public EventBuilder requestBundle(Bundle bundle) {
        mEvent.requestBundle = bundle;
        return this;
    }

    public EventBuilder reference(Reference<Context> reference) {
        mEvent.reference = reference;
        return this;
    }

    public EventBuilder target(EventHandler handler) {
        mEvent.target = handler;
        return this;
    }

    public EventBuilder callback(EventCallback callback) {
        mEvent.callback = callback;
        return this;
    }

    public EventBuilder startTime(long time) {
        mEvent.startTime = time;
        return this;
    }

    public EventBuilder subscribeOn(Scheduler scheduler) {
        mEvent.subscriber = scheduler;
        return this;
    };

    public EventBuilder observeOn(Scheduler scheduler) {
        mEvent.observer = scheduler;
        return this;
    }

    public EventBuilder register(EventRegister register) {
        mEvent.register = register;
        return this;
    }

    public EventBuilder receiver(EventReceiver receiver) {
        mEvent.receiver = receiver;
        return this;
    }

    public EventBuilder dispatcher(EventDispatcher dispatcher) {
        mEvent.dispatcher = dispatcher;
        return this;
    }

    public EventBuilder interceptor(Interceptor interceptor) {
        mEvent.interceptor = interceptor;
        return this;
    }

    public <T> Event<T> build() {
        return mEvent;
    }


    /**
     * 事件源。
     * 为了规范开发，请不要将构造方法曝光
     */
    public static final class Event<T>{

        /*********************event对象池，后续用于内存优化****************/
        private static Event mPool;
        private static Object lock = new Object();
        private static int curSize = 0;
        private static final int maxSize = 5;
        private Event next;

        protected static Event obtain(){
            synchronized (lock) {
                if (curSize <= 0) {
                    curSize = 0;
                    return new Event();
                } else {
                    Event ev = mPool;
                    mPool = ev.next;
                    ev.next = null;
                    curSize--;
                    return ev;
                }
            }
        }

        protected void release(){
            synchronized (lock) {
                clear();
                if (curSize < maxSize) {
                    next = mPool;
                    mPool = this;
                    curSize++;
                }
            }
        }

        protected void clear(){
            registerType = 0;
            requestId = 0;
            receiverKey = "";
            sessionId = "";
            requestBundle = null;
            responseData = null;
            callback = null;
            reference = null;
            target = null;
            isSent = false;
            unsubscribe = false;
        }
        /****************************************************************/

        public int registerType;//业务工厂(注册器Register)标识
        public String receiverKey = "";//接收器(Receiver)标识
        public int requestId;//接收器处理请求id
        public String sessionId = "";//会话ID
        public long startTime = 0;
        public long endTime = 0;
        public Bundle requestBundle;//请求参数
        public T responseData;//请求结果集
        public EventCallback callback;//回调
        public Reference<Context> reference;//android 上下文
        public EventHandler target;

        protected boolean isSent;
        protected boolean unsubscribe;
        protected Scheduler subscriber;//事件处理时所在的调度器
        protected Scheduler observer;//执行回调函数所在的调度器
        protected EventRegister register;//业务工厂(注册器Register)实例，此变量如果不为null，则registerType失效
        protected EventReceiver receiver;//接收器(Receiver)实例，此变量如果不为null，则receiverKey、registerType、register均失效，
        protected EventDispatcher dispatcher;//分发器，如果不配置，则会使用EventFactory中注册的分发器或使用默认的分发器。
        protected Interceptor<T> interceptor;//拦截器，在对应阶段对事件流程进行拦截处理，

        protected Event(){
            isSent = false;
        }

        public Subscription send(){
            if (target == null) {
                throw new NullPointerException("event target is null!");
            }
            return target.send(this);
        }

        public Event copy(){
            Event ev = new Event();
            ev.registerType = registerType;
            ev.receiverKey = receiverKey;
            ev.requestId = requestId;
            ev.sessionId = sessionId;
            ev.requestBundle = requestBundle;
            ev.callback = callback;
            ev.reference = reference;
            ev.target = target;

            ev.isSent = false;
            ev.unsubscribe = false;
            ev.startTime = System.currentTimeMillis();
            ev.observer = observer;
            ev.subscriber = subscriber;
            ev.register = register;
            ev.receiver = receiver;
            ev.dispatcher = dispatcher;
            ev.interceptor = interceptor;
            return ev;
        }

        public boolean isUnsubscribe(){
            return unsubscribe;
        }

        public void setUnsubscribe(boolean flag) {
            unsubscribe = flag;
        }

        public Scheduler getObserver() {
            return observer;
        }

        public void setObserver(Scheduler scheduler){
            observer = scheduler;
        }

        public Scheduler getSubscriber() {
            return subscriber;
        }

        public void setSubscriber(Scheduler scheduler) {
            subscriber = scheduler;
        }

        public EventRegister getRegister() {
            return register;
        }

        public void setRegister(EventRegister register) {
            this.register = register;
        }

        public EventReceiver getReceiver() {
            return receiver;
        }

        public void setReceiver(EventReceiver receiver) {
            this.receiver = receiver;
        }

        public EventDispatcher getDispatcher() {
            return dispatcher;
        }

        public void setDispatcher(EventDispatcher dispatcher) {
            this.dispatcher = dispatcher;
        }

        public Interceptor<T> getInterceptor() {
            return interceptor;
        }

        public void setInterceptor(Interceptor<T> interceptor) {
            this.interceptor = interceptor;
        }

        @Override
        public String toString() {
            return "Event{" +
                    "next=" + next +
                    ", registerType=" + registerType +
                    ", requestId=" + requestId +
                    ", receiverKey='" + receiverKey + '\'' +
                    ", sessionId='" + sessionId + '\'' +
                    ", requestBundle=" + requestBundle.toString() +
                    ", responseData=" + responseData.toString() +
                    ", startTime=" + startTime +
                    ", endTime=" + endTime +
                    ", callback=" + callback +
                    ", reference=" + reference +
                    ", isSent=" + isSent +
                    '}';
        }
    }
}
