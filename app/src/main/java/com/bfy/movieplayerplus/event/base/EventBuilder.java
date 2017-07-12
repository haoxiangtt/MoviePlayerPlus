package com.bfy.movieplayerplus.event.base;

import java.lang.ref.Reference;
import java.util.concurrent.TimeUnit;

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

public class EventBuilder<V, T> {

    private Event<V, T> mEvent;

    public EventBuilder() {
        mEvent = obtain();
    }

    final Event<V, T> obtain(){
        return Event.obtain();
    }

    public static final void release(Event ev){
        ev.release();
    }

    public EventBuilder<V, T> type(int type) {
        mEvent.registerType = type;
        return this;
    }

    public EventBuilder<V, T> key(String key) {
        mEvent.receiverKey = key;
        return this;
    }

    public EventBuilder<V, T> requestId(int requestId) {
        mEvent.requestId = requestId;
        return this;
    }

    public EventBuilder<V, T> sessionId(String sessionId) {
        mEvent.sessionId = sessionId;
        return this;
    }

    public EventBuilder<V, T> requestData(V bundle) {
        mEvent.requestData = bundle;
        return this;
    }

    public EventBuilder<V, T> reference(Reference reference) {
        mEvent.reference = reference;
        return this;
    }

    public EventBuilder<V, T> target(EventHandler handler) {
        mEvent.target = handler;
        return this;
    }

    public EventBuilder<V, T> callback(EventCallback<V, T> callback) {
        mEvent.callback = callback;
        return this;
    }

    public EventBuilder<V, T> startTime(long time) {
        mEvent.startTime = time;
        return this;
    }

    public EventBuilder<V, T> subscribeOn(Scheduler scheduler) {
        mEvent.subscriber = scheduler;
        return this;
    };

    public EventBuilder<V, T> observeOn(Scheduler scheduler) {
        mEvent.observer = scheduler;
        return this;
    }

    public EventBuilder<V, T> register(EventRegister register) {
        mEvent.register = register;
        return this;
    }

    public EventBuilder<V, T> receiver(EventReceiver<V, T> receiver) {
        mEvent.receiver = receiver;
        return this;
    }

    public EventBuilder<V, T> dispatcher(EventDispatcher dispatcher) {
        mEvent.dispatcher = dispatcher;
        return this;
    }

    public EventBuilder<V, T> interceptor(Interceptor<V, T> interceptor) {
        mEvent.interceptor = interceptor;
        return this;
    }

    public EventBuilder<V, T> delay(long delay, TimeUnit unit){
        mEvent.delay = delay;
        mEvent.unit = unit;
        return this;
    }

    public Event<V, T> build() {
        return mEvent;
    }


    /**
     * 事件源。
     * 为了规范开发，请不要将构造方法曝光
     */
    public static final class Event<V, T>{

        /*********************event对象池，后续用于内存优化(对象池功能暂时无法使用了)****************/
        /*private static Event mPool;
        private static Object lock = new Object();
        private static int curSize = 0;
        private static final int maxSize = 5;
        private Event next;*/

        static <V, T> Event<V, T> obtain(){
            /*synchronized (lock) {
                if (curSize <= 0) {
                    curSize = 0;
                    return new Event<>();
                } else {
                    Event ev = mPool;
                    mPool = ev.next;
                    ev.next = null;
                    curSize--;
                    return ev;
                }
            }*/
            return new Event<>();
        }

        void release(){
            /*synchronized (lock) {
                clear();
                if (curSize < maxSize) {
                    next = mPool;
                    mPool = this;
                    curSize++;
                }
            }*/
        }

        void clear(){
            registerType = 0;
            requestId = 0;
            receiverKey = "";
            sessionId = "";
            requestData = null;
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
        public V requestData;//请求参数
        public T responseData;//请求结果集
        public Reference reference;//存放android 上下文(任何生命周期比较长的资源消耗大的实例都可以存放在此)
        public EventHandler target;
        EventCallback<V, T> callback;//回调

        long delay;
        TimeUnit unit;
        boolean isSent;
        boolean unsubscribe;
        Scheduler subscriber;//事件处理时所在的调度器
        Scheduler observer;//执行回调函数所在的调度器
        EventRegister register;//业务工厂(注册器Register)实例，此变量如果不为null，则registerType失效
        EventReceiver<V, T> receiver;//接收器(Receiver)实例，此变量如果不为null，则receiverKey、registerType、register均失效，
        EventDispatcher dispatcher;//分发器，如果不配置，则会使用EventFactory中注册的分发器或使用默认的分发器。
        Interceptor<V, T> interceptor;//拦截器，在对应阶段对事件流程进行拦截处理，

        Event(){
            isSent = false;
            unsubscribe = false;
        }

        public Subscription send(){
            if (target == null) {
                throw new NullPointerException("event target is null!");
            }
            return target.send(this);
        }

        public Event<V, T> copy(){
            Event<V, T> ev = new Event<>();
            ev.registerType = registerType;
            ev.receiverKey = receiverKey;
            ev.requestId = requestId;
            ev.sessionId = sessionId;
            ev.requestData = requestData;
            ev.callback = callback;
            ev.reference = reference;
            ev.target = target;

            ev.isSent = false;
            ev.unsubscribe = unsubscribe;
            ev.startTime = System.currentTimeMillis();
            ev.observer = observer;
            ev.subscriber = subscriber;
            ev.register = register;
            ev.receiver = receiver;
            ev.dispatcher = dispatcher;
            ev.interceptor = interceptor;
            return ev;
        }

        public void performCallback(Event<V, T> ev){
            if (callback != null) {
                callback.call(ev);
            }
        }

        public EventCallback<V, T> getCallback(){
            return callback;
        }

        void setDelay(long delay, TimeUnit unit) {
            this.delay = delay;
            this.unit = unit;
        }

        public long getDelay(){
            return delay;
        }

        public TimeUnit getUnit(){
            return unit;
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

        void setObserver(Scheduler scheduler){
            observer = scheduler;
        }

        public Scheduler getSubscriber() {
            return subscriber;
        }

        void setSubscriber(Scheduler scheduler) {
            subscriber = scheduler;
        }

        public EventRegister getRegister() {
            return register;
        }

        void setRegister(EventRegister register) {
            this.register = register;
        }

        public EventReceiver<V, T> getReceiver() {
            return receiver;
        }

        void setReceiver(EventReceiver<V, T> receiver) {
            this.receiver = receiver;
        }

        public EventDispatcher getDispatcher() {
            return dispatcher;
        }

        void setDispatcher(EventDispatcher dispatcher) {
            this.dispatcher = dispatcher;
        }

        public Interceptor<V, T> getInterceptor() {
            return interceptor;
        }

        void setInterceptor(Interceptor<V, T> interceptor) {
            this.interceptor = interceptor;
        }

        @Override
        public String toString() {
            return "Event{" +
//                    "next=" + next +
                    ", registerType=" + registerType +
                    ", requestId=" + requestId +
                    ", receiverKey='" + receiverKey + '\'' +
                    ", sessionId='" + sessionId + '\'' +
                    ", requestData=" + requestData.toString() +
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
