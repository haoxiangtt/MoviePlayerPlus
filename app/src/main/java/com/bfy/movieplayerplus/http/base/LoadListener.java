package com.bfy.movieplayerplus.http.base;

import java.util.Map;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXX
 * @company    : XXXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * @e-mail     : jinfu123.-@163.com
 * @createDate : 2017/4/13 0023
 * @modifyDate : 2017/4/13 0023
 * @version    : 1.0
 * @desc       : LoadListener special for ByteArrayLoadControler
 * </pre>
 */
public interface LoadListener {
	
	void onStart();

	void onSuccess(byte[] data, Map<String, String> headers, String url, String actionId);

	void onError(String errorCode, String errorMsg, String url, String actionId);
}
