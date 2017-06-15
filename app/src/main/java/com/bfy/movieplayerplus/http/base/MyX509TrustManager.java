package com.bfy.movieplayerplus.http.base;

import javax.net.ssl.X509TrustManager;

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
public class MyX509TrustManager implements X509TrustManager {

    @Override
    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
        return null;
    }

    @Override
    public void checkClientTrusted(
            java.security.cert.X509Certificate[] chain, String authType)
            throws java.security.cert.CertificateException {
    }

    @Override
    public void checkServerTrusted(
            java.security.cert.X509Certificate[] chain, String authType)
            throws java.security.cert.CertificateException {
    }
}