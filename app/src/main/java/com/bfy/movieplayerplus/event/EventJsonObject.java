package com.bfy.movieplayerplus.event;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

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
 * @desc       : 可序列化的json
 * </pre>
 */

public class EventJsonObject extends JSONObject implements Parcelable{

    protected EventJsonObject(Parcel in) throws JSONException {
        super(in.readString());

    }

    public EventJsonObject(){
     super();
    }

    public EventJsonObject(String json) throws JSONException {
        super(json);
    }

    public EventJsonObject(Map copyFrom) {
        super(copyFrom);
    }

    public EventJsonObject(JSONTokener readFrom) throws JSONException {
        super(readFrom);
    }

    public EventJsonObject(JSONObject copyFrom, String[] names) throws JSONException {
        super(copyFrom, names);
    }

    public static final Creator<EventJsonObject> CREATOR = new Creator<EventJsonObject>() {
        @Override
        public EventJsonObject createFromParcel(Parcel in) {
            try {
                return new EventJsonObject(in);
            } catch (JSONException e) {
                return null;
            }
        }

        @Override
        public EventJsonObject[] newArray(int size) {
            return new EventJsonObject[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(toString());
    }
}
