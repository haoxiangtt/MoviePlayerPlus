package com.bfy.movieplayerplus.event.base;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.lang.ref.Reference;
import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXX
 * @company    : XXXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * @e-mail     : jinfu123.-@163.com
 * @createDate : 2017/4/18
 * @modifyDate : 2017/4/18
 * @version     : 1.0
 * @desc       : event构造器，用于构造event事件源，
 *                  为了规范开发，请不要直接构造event
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
        mEvent.type = type;
        return this;
    }

    public EventBuilder key(String key) {
        mEvent.key = key;
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

    public Event build() {
        return mEvent;
    }


    /**
     * 事件源
     * 为了规范开发，请不要将构造方法曝光
     */
    public static final class Event implements Parcelable{

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
            type = 0;
            requestId = 0;
            key = "";
            sessionId = "";
            requestBundle = null;
            responseData = null;
            callback = null;
            reference = null;
            target = null;
            isSent = false;
        }
        /****************************************************************/

        private static final Map<String,Object> parcelMap = new HashMap<>();
        public int type;//业务工厂(Register)类型标识
        public String key = "";//接收器标识
        public int requestId;//接收器处理请求id
        public String sessionId = "";//回话ID
        public long startTime = 0;
        public long endTime = 0;
        public Bundle requestBundle;//请求参数
        public Parcelable responseData;//请求结果集
        public EventCallback callback;//回调

        public Reference<Context> reference;
        public EventHandler target;

        protected boolean isSent;

        protected Event(){
            isSent = false;
        }

        protected Event(Parcel in) {
            type = in.readInt();
            key = in.readString();
            requestId = in.readInt();
            sessionId = in.readString();
            startTime = in.readLong();
            endTime = in.readLong();
            requestBundle = in.readBundle();
            responseData = in.readParcelable(responseData.getClass().getClassLoader());
            isSent = Boolean.valueOf(in.readString()).booleanValue();

            Object obj = null;
            if (!TextUtils.isEmpty(sessionId)) {
                obj = parcelMap.get(sessionId);
            }
            if (obj != null) {
                callback = (EventCallback)
                parcelMap.remove(sessionId);
            }
        }

        public static final Creator<Event> CREATOR = new Creator<Event>() {
            @Override
            public Event createFromParcel(Parcel in) {
                return new Event(in);
            }

            @Override
            public Event[] newArray(int size) {
                return new Event[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {

            parcel.writeInt(type);
            parcel.writeString(key);
            parcel.writeInt(requestId);
            parcel.writeString(sessionId);
            parcel.writeLong(startTime);
            parcel.writeLong(endTime);
            parcel.writeBundle(requestBundle);
            parcel.writeParcelable(responseData, i);
            parcel.writeString(Boolean.valueOf(isSent).toString());
            if (!TextUtils.isEmpty(sessionId)) {
                parcelMap.put(sessionId, callback);
            }
        }

        public void send(){
            if (target == null) {
                throw new NullPointerException("target is null!");
            }
            if (isSent) {
                //一次事件只能发送一次，发送多次抛出异常，如需要重复发送同样事件，可以调用
                //event.copy()复制一个新的event对象再重新发送
                throw new EventAlreadySentException("event repeat send,the event object connot send repeatedly!");
            }
            target.handleEvent(this);
            isSent = true;
        }

        public Event copy(){
            Event ev = new Event();
            ev.type = type;
            ev.key = key;
            ev.requestId = requestId;
            ev.sessionId = sessionId;
            ev.requestBundle = requestBundle;
//            ev.responseData = responseData;
            ev.callback = callback;
            ev.reference = reference;
            ev.target = target;
            ev.isSent = false;
            ev.startTime = startTime;
            ev.endTime = endTime;
            return ev;
        }

        @Override
        public String toString() {
            return "Event{" +
                    "next=" + next +
                    ", type=" + type +
                    ", requestId=" + requestId +
                    ", key='" + key + '\'' +
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
