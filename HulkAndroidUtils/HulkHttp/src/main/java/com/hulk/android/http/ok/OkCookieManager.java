package com.hulk.android.http.ok;

import com.hulk.android.log.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.HttpUrl;

/**
 * cookie管理器
 * @author: zhanghao
 * @Time: 2021-02-19 15:24
 */
public class OkCookieManager {

    private static final String TAG = "OkCookieManager";
    private static HashMap<String, List<Cookie>> sCookieMap = new HashMap<>();

    public static void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        Log.i(TAG, "saveFromResponse: " + url + ", cookies: " + cookies);
        sCookieMap.put(url.host(), cookies);
    }

    public static List<Cookie> loadForRequest(HttpUrl url) {
        Log.i(TAG, "loadForRequest: " + url);
        List<Cookie> cookies = sCookieMap.get(url.host());
        return cookies == null ? new ArrayList<Cookie>() : cookies;
    }

    public static HashMap<String, List<Cookie>> getCookieMap() {
        return sCookieMap;
    }

    public static List<Cookie> getCookieList(String host) {
        return sCookieMap.get(host);
    }

    public static List<Cookie> getUrlCookieList(String url) {
        HttpUrl httpUrl = HttpUrl.get(url);
        return sCookieMap.get(httpUrl.host());
    }

    public static String getCookieStr(String host) {
        List<Cookie> cookieList = getCookieList(host);
        return parseCookieStr(cookieList);
    }

    public static String getUrlCookieStr(String url) {
        HttpUrl httpUrl = HttpUrl.get(url);
        return getCookieStr(httpUrl.host());
    }

    /**
     * 解析cookie为等号字符串.
     * eg: name=zhangsan;time=15676768989
     * @param cookieList
     * @return
     */
    public static String parseCookieStr(List<Cookie> cookieList) {
        if (cookieList == null || cookieList.isEmpty()) {
            return null;
        }
        StringBuffer buff = new StringBuffer();
        if (cookieList != null && cookieList.size() > 0) {
            int count = 0;
            for (Cookie cookie: cookieList) {
                buff.append(cookie.name()).append("=").append(cookie.value());
                count++;
                if (count < cookieList.size()) {
                    buff.append(";");
                }
            }
        }
        return buff.toString();
    }
}
