package com.bfy.movieplayerplus.model.base;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Map.Entry;

import com.bfy.movieplayerplus.event.EventJsonObject;
import com.bfy.movieplayerplus.http.RequestManager;
import com.bfy.movieplayerplus.http.base.LoadControler;
import com.bfy.movieplayerplus.parameter.RequestParameter;
import com.bfy.movieplayerplus.utils.LogUtils;
import com.bfy.movieplayerplus.utils.Constant;


/**
 * 业务模型基础类
 * @author ouyangjinfu
 * @data 2015年9月8日
 *
 */
public abstract class BaseModel {

	protected static final boolean DEBUG = LogUtils.isDebug;
	protected static final String TAG = "BaseModel";

	public static final String KEY_RESULT_CODE = "resultcode";
	public static final String KEY_DESC = "desc";
	public static final String KEY_URL = "url";

	public interface ErrorCallback{
		/**
		 * 回调出错
		 * @param result 出错信息
		 */
		public void onError(JSONObject result, String actionId);
	}
	
	public interface SuccessCallback{
		/**
		 * 成功回调
		 * @param result 结果集
		 * @param actionId 请求行为
		 */
		public void onSuccess(JSONObject result, String actionId);
	}

	public interface BaseCallback extends ErrorCallback, SuccessCallback{}



	protected BaseModel(){}
	
	/**
	 * 打印url地址和参数
	 * @param url url地址
	 * @param params 参数集合
	 */
	protected void printURL(String url, Map<String,String> params){
		LogUtils.i(TAG, "url:"+url);
		if(params !=  null){
			StringBuilder sb = new StringBuilder();
			for(Entry< String, String> entry : params.entrySet()){
				sb.append(entry.getKey()+":"+entry.getValue()+",\n");
			}
			LogUtils.i(TAG, "params:{"+sb.toString()+"}");
		}
	}

	protected  <T extends RequestParameter> LoadControler doCommonRequest(String sessionId, T params,
																		  final BaseCallback callback, String url) {
		LogUtils.e(TAG, "request https url : " + url
				+ ">>>>>>> PARAMS : " + params.toJsonString());
		return RequestManager.getInstance().post(url, params.toJsonString(), null, new RequestManager.RequestListener() {
			@Override
			public void onRequest() {}

			@Override
			public void onSuccess(byte[] data, Map<String, String> headers, String url, String actionId) {
				String resultStr;
				try {
					resultStr = new String(data,"UTF-8");
					JSONObject result = new EventJsonObject(resultStr);
					LogUtils.e(TAG, "request success , url : " + url
							+ ">>>>result : " + resultStr);
					if (callback != null) {
						callback.onSuccess(result, actionId);
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
				JSONObject obj = new EventJsonObject();
				try {
					obj.put(KEY_RESULT_CODE, errorCode);
					obj.put(KEY_DESC, errorMsg);
					obj.put(KEY_URL, url);
				} catch (JSONException e) {}
				LogUtils.e(TAG, "request failed , url : " + url
						+ ">>>>>errorMsg : " + obj.toString());
				if (callback != null) {
					callback.onError(obj, actionId);
				}
			}
		}, sessionId);
	}
	

}
