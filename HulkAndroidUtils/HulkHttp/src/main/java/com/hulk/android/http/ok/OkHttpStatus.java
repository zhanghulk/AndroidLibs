package com.hulk.android.http.ok;

import java.net.HttpURLConnection;

/**
 * Ok http 状态码
 * @author: zhanghao
 * @Time: 2021-02-24 18:21
 */
public interface OkHttpStatus {

    int SC_OK = HttpURLConnection.HTTP_OK;

    /**
     * 服务器返回未授权(user token已过期),需要重新刷新token
     */
    int SC_UNAUTHORIZED = HttpURLConnection.HTTP_UNAUTHORIZED;

    int SC_PARTIAL_CONTENT = HttpURLConnection.HTTP_PARTIAL;

    int SC_NOT_FOUND = HttpURLConnection.HTTP_NOT_FOUND;

    int SC_FORBIDDEN = HttpURLConnection.HTTP_FORBIDDEN;

    int SC_INTERNAL_ERROR = HttpURLConnection.HTTP_INTERNAL_ERROR;
}
