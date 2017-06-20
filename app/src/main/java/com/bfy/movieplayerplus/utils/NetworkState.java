package com.bfy.movieplayerplus.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 监测网络状态的单例类
 * 
 * @author ouyangjinfu@giiso.com
 * change by ouyangjinfu
 * @version 创建时间：2014年9月20日 下午5:31:53
 */
public class NetworkState {

	public enum NetWorkType { UNKNOWN,WIFI,MOBILE_2G,MOBILE_3G,MOBILE_4G }

	//网络类型
	public static final int NETWORK_TYPE_NONE = 0;//无网络
	public static final int NETWORK_TYPE_MOBILE = 1;//数据流量
	public static final int NETWORK_TYPE_WIFI = 2;//wifi网络

	public static final String TAG="NetworkState";
	
	public static final boolean DEBUG = LogUtils.isDebug;

	private static NetworkState state;

	private boolean connected = false;
	

	public static NetworkState getInstance(Context context) {

		if (state == null) {
			state = new NetworkState();
		}
		return state;
	}

	public NetworkState() {
		//initNetWorkStatus(context);
	}

	private int initNetWorkStatus(Context context) {
		if (context != null) {
			ConnectivityManager cm = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			
			State wifiState = null;
	        State mobileState = null;
	        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	        if(wifiInfo != null){
	        	wifiState = wifiInfo.getState();  
	        }
	        NetworkInfo mobileInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
	        if(mobileInfo != null){
	        	mobileState = mobileInfo.getState();  
	        }

			NetworkInfo info = cm.getActiveNetworkInfo();

			if (info != null && info.getType() == ConnectivityManager.TYPE_WIFI) {
				wifiState = info.getState();
				if (wifiState != null && State.CONNECTED == wifiState) {
					// 无线网络连接成功
					this.connected = true;
					return NETWORK_TYPE_WIFI;
				} else {
					// 手机没有任何的网络
					connected = false;
					return NETWORK_TYPE_NONE;
				}
			} else if (info != null && info.getType() == ConnectivityManager.TYPE_MOBILE) {
				mobileState = info.getState();
				if (mobileState != null && State.CONNECTED == mobileState) {
					// 手机网络连接成功
					connected = true;
					return NETWORK_TYPE_MOBILE;
				} else {
					// 手机没有任何的网络
					connected = false;
					return NETWORK_TYPE_NONE;
				}
			} else {
				// 手机没有任何的网络
				connected = false;
				return NETWORK_TYPE_NONE;
			}
	        
		}
		LogUtils.d(TAG, "=====初始网络状态==>>"+connected);
		return NETWORK_TYPE_NONE;

	}

	public boolean isConneted(Context context) {
		initNetWorkStatus(context);
		return this.connected;
	}

	synchronized public void stateChanged(boolean connected) {
		this.connected = connected;
		LogUtils.d(TAG, "=====网络状态==>>"+connected);
	}
	

	public int getConnectedType(Context context){

		return initNetWorkStatus(context);
	}
	
	/**
	 * 这个可以判断是否联网
	 * @return
	 */
	public boolean ping(){
		Runtime rt = Runtime.getRuntime();
		InputStream in = null;
		try {
			Process p = rt.exec("ping -c 3 -w 100 www.cmpassport.com");
			
			in = p.getInputStream();  
	        BufferedReader read = new BufferedReader(new InputStreamReader(in));
	        String result = "";
	        while((result = read.readLine()) != null){
	          if(DEBUG){ LogUtils.e(TAG, ">>>>>>>"+result); }
	        }
	        int status = p.waitFor();
	        if(DEBUG){ LogUtils.e(TAG,">>>>>>status="+status); }
	        if(status == 0){
	        	return true;
	        }else{
	        	return false;
	        }
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}finally{
			if(in != null){
				try {
					in.close();
				} catch (IOException e) {}
			}
		}
		
		return false;
	}

	/**
	 * 网络类型判断
	 * 	--------------------Added in API level 1---------------------
	 *	public static final int NETWORK_TYPE_UNKNOWN
	 *	Network type is unknown
	 *	Constant Value: 0 (0x00000000)
	 *	(不知道网络类型)
	 *
	 *	public static final int NETWORK_TYPE_GPRS
	 *	Current network is GPRS
	 *	Constant Value: 1 (0x00000001)
	 *	(2.5G）移动和联通
	 *
	 *	public static final int NETWORK_TYPE_EDGE
	 *	Current network is EDGE
	 *	Constant Value: 2 (0x00000002)
	 *	(2.75G)2.5G到3G的过渡    移动和联通
	 *
	 *	public static final int NETWORK_TYPE_UMTS
	 *	Current network is UMTS
	 *	Constant Value: 3 (0x00000003)
	 *	(3G)联通
	 * 	-----------------Added in API level 4---------------------
	 *	public static final int NETWORK_TYPE_CDMA
	 *	Current network is CDMA: Either IS95A or IS95B
	 *	Constant Value: 4 (0x00000004)
	 *	(2G 电信)
	 *
	 *	public static final int NETWORK_TYPE_EVDO_0
	 *	Current network is EVDO revision 0
	 *	Constant Value: 5 (0x00000005)
	 *	( 3G )电信
	 *
	 *	public static final int NETWORK_TYPE_EVDO_A
	 *	Current network is EVDO revision A
	 *	Constant Value: 6 (0x00000006)
	 *	(3.5G) 属于3G过渡
	 *
	 *	public static final int NETWORK_TYPE_1xRTT
	 *	Current network is 1xRTT
	 *	Constant Value: 7 (0x00000007)
	 *	( 2G )
	 *
	 *	---------------------Added in API level 5--------------------
	 *	public static final int NETWORK_TYPE_HSDPA
	 *	Current network is HSDPA
	 *	Constant Value: 8 (0x00000008)
	 *	(3.5G )
	 *
	 *	public static final int NETWORK_TYPE_HSUPA
	 *	Current network is HSUPA
	 *	Constant Value: 9 (0x00000009)
	 *	( 3.5G )
	 *
	 *	public static final int NETWORK_TYPE_HSPA
	 *	Current network is HSPA
	 *	Constant Value: 10 (0x0000000a)
	 *	( 3G )联通
	 *
	 *	--------------------------Added in API level 8-------------------------
	 *	public static final int NETWORK_TYPE_IDEN
	 *	Current network is iDen
	 *	Constant Value: 11 (0x0000000b)
	 *	(2G )
	 *
	 *	--------------------------Added in API level 9-------------------------
	 *	public static final int NETWORK_TYPE_EVDO_B
	 *	Current network is EVDO revision B
	 *	Constant Value: 12 (0x0000000c)
	 *	3G-3.5G
	 *
	 *	--------------------------Added in API level 11------------------------
	 *	public static final int NETWORK_TYPE_LTE
	 *	Current network is LTE
	 *	Constant Value: 13 (0x0000000d)
	 *	(4G)
	 *
	 *	public static final int NETWORK_TYPE_EHRPD
	 *	Current network is eHRPD
	 *	Constant Value: 14 (0x0000000e)
	 *	3G(3G到4G的升级产物)
	 *
	 *
	 *	--------------------------Added in API level 13---------------------------
	 *	public static final int NETWORK_TYPE_HSPAP
	 *	Current network is HSPA+
	 *	Constant Value: 15 (0x0000000f)
	 *	( 3G )
	 * @param type 网络连接类型(mobile网络)
	 * @return 返网络枚举类型(UNKONWN/WIFI/MOBILE_2G/MOBILE_3G/MOBILE_4G)
	 */
	public NetWorkType getNetWorkType(int type){
		switch (type) {
			case 0:{
				return NetWorkType.UNKNOWN;
			}
			case 1:
			case 2:
			case 4:
			case 7:
			case 11:{
				return NetWorkType.MOBILE_2G;
			}
			case 3:
			case 5:
			case 6:
			case 8:
			case 9:
			case 10:
			case 12:
			case 14:
			case 15:{
				return NetWorkType.MOBILE_3G;
			}
			default:{
				return NetWorkType.MOBILE_4G;
			}

		}
	}


}
