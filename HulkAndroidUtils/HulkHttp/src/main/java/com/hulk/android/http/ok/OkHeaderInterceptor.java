package com.hulk.android.http.ok;

import android.content.Context;
import android.text.TextUtils;

import com.hulk.android.log.Log;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 自定义 OK http header
 * @author: zhanghao
 * @Time: 2021-02-19 15:45
 */
public class OkHeaderInterceptor implements Interceptor {
    private static final String TAG = "OkHeaderInterceptor";

    Context context;

    public OkHeaderInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (isRequiredUserTokenHeader(request.url())) {
            //添加UserToken头部
            String token = getUserToken();
            request = OkRequestUtils.addUserTokenHeader(request, token);
        } else {
            Log.i(TAG, "intercept: Disabled UserTokenVerifyApi: " + request.url());
        }
        return chain.proceed(request);
    }

    protected boolean isRequiredUserTokenHeader(HttpUrl url) {
        String token = getUserToken();
        if (TextUtils.isEmpty(token)) {
            return false;
        }
        return true;
    }

    protected String getUserToken() {
        return "";
    }
}
