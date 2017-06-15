package com.bfy.movieplayerplus.http;

import android.content.Context;

import java.net.HttpURLConnection;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

import com.bfy.movieplayerplus.http.base.LoadControler;
import com.bfy.movieplayerplus.http.base.LoadListener;
import com.bfy.movieplayerplus.http.base.MyHostnameVerifier;
import com.bfy.movieplayerplus.http.base.MyX509TrustManager;
import com.bfy.movieplayerplus.http.base.RequestMap;
import com.bfy.movieplayerplus.volley.AuthFailureError;
import com.bfy.movieplayerplus.volley.DefaultRetryPolicy;
import com.bfy.movieplayerplus.volley.Request;
import com.bfy.movieplayerplus.volley.Request.Method;
import com.bfy.movieplayerplus.volley.RequestQueue;
import com.bfy.movieplayerplus.volley.RetryPolicy;
import com.bfy.movieplayerplus.volley.toolbox.Volley;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXX
 * @company    : XXXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * @e-mail     : jinfu123.-@163.com
 * @createDate : 2017/4/13 0023
 * @modifyDate : 2017/4/13 0023
 * @version    : 1.0
 * @desc       :
 * </pre>
 */
public class RequestManager {

	private static final String CHARSET_UTF_8 = "UTF-8";

	private static final int TIMEOUT_COUNT = 5 * 1000;

	private static final int RETRY_TIMES = 1;

	private volatile static RequestManager instance = null;

	private RequestQueue mRequestQueue = null;

	//add by ouyangjinfu
//	private ImageLoader mImageLoader = null;

	public interface RequestListener {

		void onRequest();

		void onSuccess(byte[] data, Map<String, String> headers, String url, String actionId);

		void onError(String errorCode, String errorMsg, String url, String actionId);
	}

	private RequestManager() {
//		initHttpHostnameVerifier();
//		initHttpsHostnameVerifier();
	}

	private static void initHttpsHostnameVerifier() {
		SSLContext sslContext = null;
		try {
			sslContext = SSLContext.getInstance("SSL");
			X509TrustManager[] x509TrustManagers = new X509TrustManager[]{new MyX509TrustManager()};
			sslContext.init(null, x509TrustManagers,
					new java.security.SecureRandom());
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (sslContext != null) {
			HttpsURLConnection.setDefaultSSLSocketFactory(sslContext
					.getSocketFactory());
		}
		HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier());
	}

	private static void initHttpHostnameVerifier() {
		SSLContext sslContext = null;
		try {
			sslContext = SSLContext.getInstance("SSL");
			X509TrustManager[] x509TrustManagers = new X509TrustManager[]{new MyX509TrustManager()};
			sslContext.init(null, x509TrustManagers,
					new java.security.SecureRandom());
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (sslContext != null) {
			HttpsURLConnection.setDefaultSSLSocketFactory(sslContext
					.getSocketFactory());
		}
		HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier());
	}
	
	public void init(Context context) {
		if (mRequestQueue != null) {
			mRequestQueue.stop();
		}
		this.mRequestQueue = Volley.newRequestQueue(context);
	}

	public void init(RequestQueue queue/*,ImageLoader imageLoader*/) {
		if (mRequestQueue != null) {
			mRequestQueue.stop();
		}
		this.mRequestQueue = queue;
	}

	public static RequestManager getInstance() {
		if (null == instance) {
			synchronized (RequestManager.class) {
				if (null == instance) {
					instance = new RequestManager();
				}
			}
		}
		return instance;
	}

	public RequestQueue getRequestQueue() {
		return this.mRequestQueue;
	}
	
	/*public ImageLoader getImageLoader() {
		return this.mImageLoader;
	}*/

	/**
	 * default get method
	 * 
	 * @param url
	 * @param requestListener
	 * @param actionId
	 * @return
	 */
	public LoadControler get(String url, RequestListener requestListener, String actionId) {
		return get(url, requestListener, true, actionId);
	}

	public LoadControler get(String url, RequestListener requestListener, boolean shouldCache, String actionId) {
		return get(url, null, requestListener, shouldCache, actionId);
	}

	public LoadControler get(String url, Map<String, String> headers, RequestListener requestListener, boolean shouldCache, String actionId) {
		return this.request(Method.GET, url, null, headers, requestListener, shouldCache, TIMEOUT_COUNT, RETRY_TIMES, actionId);
	}

	/**
	 * default post method
	 * 
	 * @param url
	 * @param data
	 *            String, Map<String, String> or RequestMap(with file)
	 * @param requestListener
	 * @param actionId
	 * @return
	 */
	public LoadControler post(final String url, Object data, Map<String,String> headers,final RequestListener requestListener, String actionId) {
		return this.post(url, data,headers, requestListener, false, TIMEOUT_COUNT, RETRY_TIMES, actionId);
	}

	/**
	 * 
	 * @param url
	 * @param data
	 *            String, Map<String, String> or RequestMap(with file)
	 * @param requestListener
	 * @param shouldCache
	 * @param timeoutCount
	 * @param retryTimes
	 * @param actionId
	 * @return
	 */
	public LoadControler post(final String url, Object data, Map<String,String> headers,final RequestListener requestListener, boolean shouldCache,
			int timeoutCount, int retryTimes, String actionId) {
		return request(Method.POST, url, data, headers, requestListener, shouldCache, timeoutCount, retryTimes, actionId);
	}

	/**
	 * request
	 * 
	 * @param method
	 *            mainly Method.POST and Method.GET
	 * @param url
	 *            target url
	 * @param data
	 *            request params
	 * @param headers
	 *            request headers
	 * @param requestListener
	 *            request callback
	 * @param shouldCache
	 *            useCache
	 * @param timeoutCount
	 *            reqeust timeout count
	 * @param retryTimes
	 *            reqeust retry times
	 * @param actionId
	 *            request id
	 * @return
	 */
	public LoadControler request(int method, final String url, Object data, final Map<String, String> headers,
			final RequestListener requestListener, boolean shouldCache, int timeoutCount, int retryTimes, String actionId) {
		return this.sendRequest(method, url, data, headers, new LoadListener() {
			@Override
			public void onStart() {
				requestListener.onRequest();
			}

			@Override
			public void onSuccess(byte[] data, Map<String, String> headers, String url, String actionId) {
//				String parsed = null;
//				try {
//					
//					parsed = new String(data, CHARSET_UTF_8);
//				} catch (UnsupportedEncodingException e) {
//					e.printStackTrace();
//				}
				requestListener.onSuccess(data, headers, url, actionId);
			}

			@Override
			public void onError(String errorCode, String errorMsg, String url, String actionId) {
				requestListener.onError(errorCode, errorMsg, url, actionId);
			}
		}, shouldCache, timeoutCount, retryTimes, actionId);
	}

	/**
	 * @param method
	 * @param url
	 * @param data
	 * @param headers
	 * @param requestListener
	 * @param shouldCache
	 * @param timeoutCount
	 * @param retryTimes
	 * @param actionId
	 * @return
	 */
	public LoadControler sendRequest(int method, final String url, Object data, final Map<String, String> headers,
			final LoadListener requestListener, boolean shouldCache, int timeoutCount, int retryTimes, String actionId) {
		if (requestListener == null)
			throw new NullPointerException();

		final ByteArrayLoadControler loadControler = new ByteArrayLoadControler(requestListener, actionId);

		Request<?> request = null;
		if (data != null && data instanceof RequestMap) {// force POST and No  Cache
			request = new ByteArrayRequest(Method.POST, url, data, loadControler, loadControler);
			request.setShouldCache(false);
		} else {
			request = new ByteArrayRequest(method, url, data, loadControler, loadControler);
			request.setShouldCache(shouldCache);
		}

		if (headers != null && !headers.isEmpty()) {// add headers if not empty
			try {
				request.getHeaders().putAll(headers);
			} catch (AuthFailureError e) {
				e.printStackTrace();
			}
		}

		RetryPolicy retryPolicy = new DefaultRetryPolicy(timeoutCount, retryTimes, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
		request.setRetryPolicy(retryPolicy);

		loadControler.bindRequest(request);

		if (this.mRequestQueue == null)
			throw new NullPointerException();
		requestListener.onStart();
		this.mRequestQueue.add(request);

		return loadControler;
	}

}
