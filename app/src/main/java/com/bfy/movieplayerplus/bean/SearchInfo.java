package com.bfy.movieplayerplus.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXX
 * @company    : XXXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * @e-mail     : jinfu123.-@163.com
 * @createDate : 2017/6/12 0012
 * @modifyDate : 2017/6/12 0012
 * @version    : 1.0
 * @desc       :
 * </pre>
 */

public class SearchInfo implements Parcelable{
    /**
     *
     */
    private static final long serialVersionUID = 314395112714052640L;
    boolean urlFound = false;
    public String url="";
    public String name="";
    public String lrcURL="";
    public String singer="";
    public String album="";
    public String type = "mp3";
    public String extra = "";

    public static final Parcelable.Creator<SearchInfo> CREATOR = new Parcelable.Creator<SearchInfo>(){

        @Override
        public SearchInfo createFromParcel(Parcel source) {
            SearchInfo thiz = new SearchInfo();
            thiz.url = source.readString();
            thiz.name = source.readString();
            thiz.lrcURL = source.readString();
            thiz.singer = source.readString();
            thiz.album = source.readString();
            thiz.type = source.readString();
            thiz.extra = source.readString();
            return thiz;
        }

        @Override
        public SearchInfo[] newArray(int size) {

            return new SearchInfo[size];
        }

    };

    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(name);
        dest.writeString(lrcURL);
        dest.writeString(singer);
        dest.writeString(album);
        dest.writeString(type);
        dest.writeString(extra);

    }
}
