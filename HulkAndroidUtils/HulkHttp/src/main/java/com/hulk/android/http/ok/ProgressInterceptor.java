package com.hulk.android.http.ok;

import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * 下载进度拦截器
 * @author: zhanghao
 * @Time: 2021-02-24 22:04
 */
public class ProgressInterceptor implements Interceptor {

    private static final String TAG = "ProgressInterceptor";
    ProgressListener listener;

    public ProgressInterceptor(ProgressListener listener) {
        this.listener = listener;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        if (listener == null) {
            Log.i(TAG, "intercept: listener is null");
            return response;
        }
        Log.i(TAG, "intercept: listener is " + listener);
        //这里将ResponseBody包装成我们的ProgressResponseBody
        return response.newBuilder()
                .body(new ProgressResponseBody(response, listener))
                .build();
    }
}
