package com.bfy.movieplayerplus.http.base;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　彩讯科技股份有限公司
 * @company    : 彩讯科技股份有限公司
 * @author     : OuyangJinfu
 * @e-mail     : ouyangjinfu@richinfo.cn
 * @createDate : 2017/4/13 0023
 * @modifyDate : 2017/4/13 0023
 * @version    : 1.0
 * @desc       : MyHostnameVerifier.java
 * </pre>
 */
public class MyHostnameVerifier implements HostnameVerifier {

    @Override
    public boolean verify(String hostname, SSLSession session) {
        boolean result = false;
//        if (Constant.URL_BASE.contains("www.cmpassport.com")) {
            result = hostname.contains("www.cmpassport.com") || hostname.contains("open.mmarket.com");
//        }
        return result;
    }
}
