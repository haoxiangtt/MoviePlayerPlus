package com.bfy.movieplayerplus.utils;

import java.security.NoSuchAlgorithmException;

/**
 * Title: XXXX (类或者接口名称)
 * Description: XXXX (简单对此类或接口的名字进行描述)
 * Copyright: Copyright (c) 2012
 * Company:深圳彩讯科技有限公司
 *
 * @author duminghui
 * @version 1.0
 */
public class Md5Coder {

	public final static String md5(String src) {
		java.security.MessageDigest md;
		try {
			md = java.security.MessageDigest.getInstance("MD5");
			byte[] bytes = src.getBytes();
			byte[] bytes_md5 = md.digest(bytes);
			StringBuffer md5StrBuff = new StringBuffer();

			for (int i = 0; i < bytes_md5.length; i++) {
				if (Integer.toHexString(0xFF & bytes_md5[i]).length() == 1)
					md5StrBuff.append("0").append(
							Integer.toHexString(0xFF & bytes_md5[i]));
				else
					md5StrBuff.append(Integer.toHexString(0xFF & bytes_md5[i]));
			}
			return md5StrBuff.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	public final static String md5Upper(String src) {
		return md5(src).toUpperCase();
	}

	public final static String md5Lower(String src) {
		return md5(src).toLowerCase();
	}
}
