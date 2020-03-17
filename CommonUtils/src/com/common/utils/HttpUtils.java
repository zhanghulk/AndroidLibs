package com.common.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;

import android.util.Log;

public class HttpUtils {
	private static final String TAG = "HttpUtils";
	private static final boolean DEBUG = true;

	/**
	 * merge url and params for GET method.
	 * @param srcUrl
	 * @param paramMap the param map
	 * @param isAppendParams whether param are started by "&" or not. started by "?" id false
	 * @param encodeValueUTF8 whether param are encoded by utf-8 or not
	 * @return  merged url and params (encoding by utf-8) for GET method.
	 */
	public static String getUrlWithParams(String srcUrl, Map<String,String> paramMap,
			boolean isAppendParams, boolean encodeValueUTF8) {
		String destUrl = null;
		if (srcUrl == null || srcUrl.length() == 0) {
			new IllegalArgumentException("The src url must not NULL");
		}
		if(paramMap != null && !paramMap.isEmpty()){
			boolean hasParam = srcUrl.contains("&");
			StringBuilder buffer = new StringBuilder();
			buffer.append(srcUrl);
			if(isAppendParams && hasParam) {
				buffer.append("&");//and other params
			} else {
				buffer.append("?");//new params
			}
			
			for(Entry<String, String> en : paramMap.entrySet()){
			    String value = en.getValue();
			    if(value != null) {
			        buffer.append(en.getKey()).append("=");
			        if (encodeValueUTF8) {
			        	buffer.append(encodeUTF8(value));
					} else {
						buffer.append(value);
					}
			        buffer.append("&");
			    }
			}
			buffer.deleteCharAt(buffer.length()-1);
			destUrl = buffer.toString().trim();
		} else {
			destUrl = srcUrl;
		}
		if(DEBUG) Log.i(TAG, "srcUrl" + srcUrl + "\n AND paramMap:" + paramMap + "\n>>>>destUrl:" + destUrl);
		return destUrl;
	}

	public static String encodeUTF8(String value) {
		try {
			return URLEncoder.encode(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return "";
	}
}
