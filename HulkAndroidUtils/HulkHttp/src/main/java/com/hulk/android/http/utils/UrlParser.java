package com.hulk.android.http.utils;

import android.text.TextUtils;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * URL解析器
 */
public class UrlParser {

	public static final String TAG = "UrlParser";
	public static final String HTTPS = "https://";
	public static final String HTTP = "http://";

	/**
	 * 解析出url请求的路径，包括页面
	 * 
	 * @param strURL
	 *            url地址
	 * @return url路径
	 */
	public static String getURLPage(String strURL) {
		String strPage = null;
		String[] arrSplit = null;

		strURL = strURL.trim().toLowerCase();

		arrSplit = strURL.split("[?]");
		if (strURL.length() > 0) {
			if (arrSplit.length > 1) {
				if (arrSplit[0] != null) {
					strPage = arrSplit[0];
				}
			}
		}

		return strPage;
	}

	/**
	 * 去掉url中的路径，留下请求参数部分
	 * 
	 * @param strURL
	 *            url地址
	 * @return url请求参数部分
	 */
	public static String getURLParamStr(String strURL) {
		String strAllParam = null;
		String[] arrSplit = null;
		arrSplit = strURL.split("[?]");
		if (strURL.length() > 1) {
			if (arrSplit.length > 1) {
				if (arrSplit[1] != null) {
					strAllParam = arrSplit[1];
				}
			}
		}

		return strAllParam;
	}

	/**
	 * 解析出url参数中的键值对 如 "index.jsp?Action=del&id=123"，解析出Action:del,id:123存入map中
	 * 
	 * @param url
	 *            url地址
	 * @return url请求参数部分
	 */
	public static Map<String, String> getURLParamMap(String url) {
		Map<String, String> mapRequest = new HashMap<String, String>();
		String[] arrSplit = null;
		String paramStr = getURLParamStr(url.trim());
		if (paramStr == null) {
			return mapRequest;
		}
		// 每个键值为一组
		Log.i(TAG, "paramStr: " + paramStr);
		arrSplit = paramStr.split("[&]");
		for (String strSplit : arrSplit) {
			String[] arrSplitEqual = strSplit.split("[=]");
			// 解析出键值
			if (arrSplitEqual.length > 1) {
				Log.i(TAG, arrSplitEqual[0] + " : " + arrSplitEqual[1]);
				// 正确解析
				mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);
			} else {
				if (arrSplitEqual[0] != "") {
					// 只有参数没有值，不加入
					mapRequest.put(arrSplitEqual[0], "");
				}
			}
		}
		return mapRequest;
	}

	public static String getUrlHost(String url) {
		url = url.trim().toLowerCase();
		if (url.startsWith("https://") || url.startsWith("http://")) {
			try {
				URL uRL = new URL(url);
				return uRL.getHost();
			} catch (Exception e) {
				Log.e(TAG, "getUrlHost: " + e + ", url: " + url, e);
			}
		}
		return url;
	}

	public static int getUrlPort(String url) {
		try {
			URL uRL = new URL(url);
			return uRL.getPort();
		} catch (Exception e) {
			Log.e(TAG, "getUrlPort: " + e + ", url: " + url, e);
		}
		return 0;
	}

	public static String getUrlPath(String url) {
		try {
			URL uRL = new URL(url);
			return uRL.getPath();
		} catch (Exception e) {
			Log.e(TAG, "getUrlPath: " + e + ", url: " + url, e);
		}
		return "";
	}

	/**
	 * 修复 host url, 去掉前缀和后面的path
	 * <p> 修复用户输入的服务器地址，获取hostname url eg: https:/www.baidu.com/
	 * @param inputUrl
	 * @param appendDiagonalLine 是否天界尾部的斜线
	 * @return
	 * @throws MalformedURLException
	 */
	public static String fixHostUrl(String inputUrl, boolean appendDiagonalLine) throws MalformedURLException {
		if (TextUtils.isEmpty(inputUrl)) {
			Log.w(TAG, "fixUrl: inputUrl is empty.");
			return "";
		}
		String fixUrl = inputUrl.trim().toLowerCase();
		boolean isHttps = true;
		if (!(inputUrl.startsWith(HTTPS) || inputUrl.startsWith(HTTP))) {
			//用户没有输入前缀就把前缀补上
			Log.w(TAG, "fixHostUrl: Add prefix https auto for " + inputUrl);
			fixUrl = HTTPS + fixUrl;
			isHttps = true;
		} else {
			isHttps = fixUrl.startsWith(HTTPS);
		}
		//通过URL获取完整的hostname,去掉前缀和后面的path
		URL uRL = new URL(fixUrl);
		String host = uRL.getHost();
		int port = uRL.getPort();
		String prefix = uRL.getProtocol() + "://";
		//有端口号时保留端口号,没有端口号时uRL.getPort()为-1
		StringBuffer buff = new StringBuffer()
				.append(prefix)
				.append(host);
		if (port > 0) {
			buff.append(":").append(port);
		}
		if (appendDiagonalLine) {
			buff.append("/");
		}
		return buff.toString();
	}

	public static String fixUrl(String inputUrl) throws MalformedURLException {
		return fixHostUrl(inputUrl, true);
	}

	public static String appendUrlParams(String url, Map<String, String> params) {
		if (TextUtils.isEmpty(url)) {
			Log.w(TAG,"appendUrlParams: Invalid url: " + url);
			return url;
		}
		if (params == null || params.isEmpty()) {
			Log.w(TAG,"appendUrlParams: Invalid params: " + params);
			return url;
		}
		StringBuffer buffer = new StringBuffer(url);
		if (!url.contains("?")) {
			buffer.append("?");
		}
		for (Map.Entry<String, String> entry: params.entrySet()) {
			try {
				String key = entry.getKey();
				String value = entry.getValue();
				String enKey = URLEncoder.encode(key,"UTF-8");
				String enValue = URLEncoder.encode(value,"UTF-8");
				if (!TextUtils.isEmpty(key) && value != null) {
					buffer.append(enKey).append("=").append(enValue);
				}
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "appendUrlParams error: " + e, e);
			}
		}
		return buffer.toString();
	}

	/**
	 * url path 删除前面的斜杠(如果存在)
	 * @param urlPath
	 * @return
	 */
	public static String removeUrlPathFirstDiagonalLine(String urlPath) {
		if (urlPath != null && urlPath.startsWith("/")) {
			return urlPath.substring(1);
		}
		return urlPath;
	}

	/**
	 * url path 添加前面的斜杠(如果不存在)
	 * @param urlPath
	 * @return
	 */
	public static String addUrlPathFirstDiagonalLine(String urlPath) {
		if (urlPath != null && !urlPath.startsWith("/")) {
			return "/" + urlPath;
		}
		return urlPath;
	}

	/**
	 * 获取完整接口url
	 * @param serverHostUtl 服务器之地址
	 * @param apiPath  api path
	 * @param params
	 * @return
	 */
	public static String getCompleteUrl(String serverHostUtl , String apiPath, Map<String, String> params) {
		if (TextUtils.isEmpty(serverHostUtl)) {
			Log.w(TAG,"getCompleteUrl: serverHostUtl is empty");
			return "";
		}
		String fixHostUrl = "";
		try {
			fixHostUrl = fixHostUrl(serverHostUtl, true);
		} catch (MalformedURLException e) {
			Log.e(TAG,"getCompleteUrl: failed FIX host url: " + serverHostUtl, e);
			return "";
		}
		String url;
		if (!TextUtils.isEmpty(apiPath)) {
			//fixUrl 后面没有有斜杠, url path 添加前面的斜杠(如果不存在)
			String fixPath = removeUrlPathFirstDiagonalLine(apiPath);
			url = fixHostUrl + fixPath;
		} else {
			url = fixHostUrl;
		}
		if (params != null && !params.isEmpty()) {
			url = UrlParser.appendUrlParams(url, params);
		}
		return url;
	}

	/**
	 * 获取基础url, 只包含hostname
	 * @param url 服务器之地址
	 * @return  基础url eg: https://www.baidu.com
	 */
	public static String getBaseUrl(String url) {
		if (TextUtils.isEmpty(url)) {
			Log.w(TAG,"getBaseUrl: url is empty");
			return "";
		}
		String baseUrl = "";
		try {
			URL uRL = new URL(url);
			String protocol = uRL.getProtocol();
			String host = uRL.getHost();
			int port = uRL.getPort();
			baseUrl = protocol + "://" + host + ":" + port;
			if (port > 0) {
				baseUrl = protocol + "://" + host + ":" + port;
			} else {
				baseUrl = protocol + "://" + host;
			}
		} catch (MalformedURLException e) {
			Log.e(TAG,"getBaseUrl: failed: " + url, e);
			return "";
		}
		return baseUrl;
	}
}
