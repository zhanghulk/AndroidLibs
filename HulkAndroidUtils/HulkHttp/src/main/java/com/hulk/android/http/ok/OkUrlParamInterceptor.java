package com.hulk.android.http.ok;

import android.content.Context;

import com.hulk.android.log.Log;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 自定义 OK http url  参数拦截器
 * @author: zhanghao
 * @Time: 2021-02-19 15:45
 */
public class OkUrlParamInterceptor implements Interceptor {
    private static final String TAG = "OkUrlParamInterceptor";

    protected Context context;

    public OkUrlParamInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (isRequiredUrlDeviceKeyParam(request.url())) {
            //添加url尾部deviceKey参数
            String deviceKey = getDeviceKey();
            request = OkRequestUtils.appendUrlDeviceKeyParam(request, deviceKey);
        } else {
            Log.i(TAG, "intercept: Disabled UrlDeviceKeyApi(: " + request.url());
        }
        return chain.proceed(request);
    }

    protected boolean isRequiredUrlDeviceKeyParam(HttpUrl url) {
        return false;
    }

    protected String getDeviceKey() {
        return "";
    }
}
