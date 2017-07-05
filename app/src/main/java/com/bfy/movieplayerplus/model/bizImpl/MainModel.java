package com.bfy.movieplayerplus.model.bizImpl;

import android.os.Bundle;
import android.text.TextUtils;

import com.bfy.movieplayerplus.event.EventJsonObject;
import com.bfy.movieplayerplus.event.base.EventBuilder;
import com.bfy.movieplayerplus.event.base.EventReceiver;
import com.bfy.movieplayerplus.http.RequestManager;
import com.bfy.movieplayerplus.model.base.BaseModel;
import com.bfy.movieplayerplus.model.base.ModelFactory;
import com.bfy.movieplayerplus.model.biz.MainBiz;
import com.bfy.movieplayerplus.utils.Constant;
import com.bfy.movieplayerplus.utils.LogUtils;
import com.bfy.movieplayerplus.utils.Md5Coder;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXX
 * @company    : XXXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * @e-mail     : jinfu123.-@163.com
 * @createDate : 2017/4/20
 * @modifyDate : 2017/4/20
 * version     : 1.0
 * @desc       : 核心业务模型
 * </pre>
 */

public final class MainModel extends BaseModel implements MainBiz, EventReceiver<Bundle, Object> {

    public static final String MODEL_KEY = "main_model";

    //设计具体业务模型时建议私有化构造方法
    private MainModel(){
        super();
    }

    @Override
    public void getMV(EventBuilder.Event event) {
        if (TextUtils.isEmpty(event.sessionId)) {
            event.sessionId = Constant.generateNonce32();
        }
        final EventBuilder.Event<Bundle, Object> ev = (EventBuilder.Event<Bundle, Object>)event;
        String url = Constant.KUGOU_MV_SEARCH_URL;
        StringBuilder sb = new StringBuilder(url);
        sb.append("?callback=");
        final String callback = "jQuery19108035724928824395_" + System.currentTimeMillis();
        sb.append(callback);
        sb.append("&keyword=");
        try {
            sb.append(URLEncoder.encode(ev.requestBundle.getString("keyword"), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            sb.append(URLEncoder.encode(ev.requestBundle.getString("keyword")));
        }
        sb.append("&page=");
        sb.append(ev.requestBundle.getString("page"));
        sb.append("&pagesize=");
        sb.append(ev.requestBundle.getString("pagesize"));
        sb.append("&userid=");
        sb.append(ev.requestBundle.getString("userid"));
        sb.append("&clientver=");
        sb.append(ev.requestBundle.getString("clientver"));
        sb.append("&platform=");
        sb.append(ev.requestBundle.getString("platform"));
        sb.append("&tag=");
        sb.append(ev.requestBundle.getString("tag"));
        sb.append("&filter=");
        sb.append(ev.requestBundle.getString("filter"));
        sb.append("&iscorrection=");
        sb.append(ev.requestBundle.getString("iscorrection"));
        sb.append("&privilege_filter=");
        sb.append(ev.requestBundle.getString("privilege_filter"));
        sb.append("&_=");
        sb.append(String.valueOf(System.currentTimeMillis()));
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.155 Safari/537.36");
        url = sb.toString();
        printURL(url, null);
        RequestManager.getInstance().get(url, headers, new RequestManager.RequestListener() {
            @Override
            public void onRequest() {}

            @Override
            public void onSuccess(byte[] data, Map<String, String> headers, String url, String actionId) {
                try {
                    String result = new String(data, "UTF-8");
                    LogUtils.d(TAG, "result = " + result);
                    String reg = "^" + callback + "\\((.*)\\)$";
                    Pattern pattern = Pattern.compile(reg);
                    Matcher matcher = pattern.matcher(result);
                    if (matcher.find()) {
                        String jsonStr = matcher.group(1);
                        EventJsonObject response = new EventJsonObject();
                        EventJsonObject jobj = new EventJsonObject(jsonStr);
                        response.put("json", jobj);
                        response.put(KEY_RESULT_CODE, Constant.ResponseCode.CODE_SUCCESSFULLY);
                        response.put(KEY_DESC, "请求成功");
                        response.put(KEY_URL, url);
                        ev.responseData = response;
                        ev.endTime = System.currentTimeMillis();
                        ev.performCallback(ev);
                    } else {
                        EventJsonObject response = new EventJsonObject();
                        response.put(KEY_RESULT_CODE, Constant.ResponseCode.CODE_DATA_ERROR);
                        response.put(KEY_DESC, "服务器连接错误");
                        response.put(KEY_URL, url);
                    }

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    onError(Constant.ResponseCode.CODE_DATA_ERROR, "响应结果解析错误", url, actionId);
                } catch (JSONException e) {
                    e.printStackTrace();
                    onError(Constant.ResponseCode.CODE_DATA_ERROR, "json对象转换错误", url, actionId);
                }
            }

            @Override
            public void onError(String errorCode, String errorMsg, String url, String actionId) {
                showError(errorCode, errorMsg, url, ev);
            }
        }, true, ev.sessionId);
    }

    @Override
    public void getMVUrl(EventBuilder.Event event) {
        final EventBuilder.Event<Bundle, Object> ev = (EventBuilder.Event<Bundle, Object>)event;
        StringBuilder sb = new StringBuilder(Constant.KUGOU_MV_REAL_URL);
        String md5 = Md5Coder.md5Lower(ev.requestBundle.getString("url") + "kugoumvcloud");
        sb.append("cmd=100");
        sb.append("&hash=");
        sb.append(ev.requestBundle.getString("url"));
        sb.append("&key=");
        sb.append(md5);
        sb.append("&pid=6");
        sb.append("&ext=mp4");
        sb.append("&ismp3=0");
        String url = sb.toString();
        printURL(url, null);
        RequestManager.getInstance().get(url, new RequestManager.RequestListener() {
            @Override
            public void onRequest() {}

            @Override
            public void onSuccess(byte[] data, Map<String, String> headers, String url, String actionId) {
                try {
                    String result = new String(data, "UTF-8");
                    LogUtils.d(TAG, "result = " + result);
                    EventJsonObject response = new EventJsonObject();
                    EventJsonObject json = new EventJsonObject(result);
                    response.put("json", json);
                    response.put(KEY_RESULT_CODE, Constant.ResponseCode.CODE_SUCCESSFULLY);
                    response.put(KEY_DESC, "请求成功");
                    response.put(KEY_URL, url);
                    ev.responseData = response;
                    ev.endTime = System.currentTimeMillis();
                    ev.performCallback(ev);

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    onError(Constant.ResponseCode.CODE_DATA_ERROR, "响应结果解析错误", url, actionId);
                } catch (JSONException e) {
                    e.printStackTrace();
                    onError(Constant.ResponseCode.CODE_DATA_ERROR, "json对象转换错误", url, actionId);
                }
            }

            @Override
            public void onError(String errorCode, String errorMsg, String url, String actionId) {
                showError(errorCode, errorMsg, url, ev);
            }
        }, true, ev.sessionId);
    }

    private void showError(String errorCode, String errorMsg, String url, EventBuilder.Event ev) {
        EventJsonObject obj = new EventJsonObject();
        try {
            obj.put(KEY_RESULT_CODE, errorCode);
            obj.put(KEY_DESC, errorMsg);
            obj.put(KEY_URL, url);
        } catch (JSONException e) {}
        LogUtils.e(TAG, "request failed , url : " + url
                + ">>>>>errorMsg : " + obj.toString());
        ev.responseData = obj;
        ev.endTime = System.currentTimeMillis();
        ev.performCallback(ev);
    }

    @Override
    public void onReceive(EventBuilder.Event<Bundle, Object> event) {
        MainBiz model = ModelFactory.getInstance().getModelProxy(event.receiverKey);
        if (event.requestId == 0) {
            if (model != null) {
                model.getMV(event);
            } else {
                getMV(event);
            }
        } else if (event.requestId == 1) {
            if (model != null) {
                model.getMVUrl(event);
            } else {
                getMVUrl(event);
            }
        }
    }

}
