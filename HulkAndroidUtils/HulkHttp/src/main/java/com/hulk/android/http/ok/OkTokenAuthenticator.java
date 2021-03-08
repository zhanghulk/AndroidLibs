package com.hulk.android.http.ok;

import android.content.Context;

import com.hulk.android.log.Log;


import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

/**
 * token认证拦截器
 * @author: zhanghao
 * @Time: 2021-02-19 17:32
 */
public class OkTokenAuthenticator implements Authenticator {

    private static final String TAG = "OkTokenAuthenticator";

    Context context;
    boolean userTokenRefreshing = false;

    public OkTokenAuthenticator(Context context) {
        this.context = context;
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        int code = response.code();
        Request request = response.request();
        if (code == OkHttpStatus.SC_UNAUTHORIZED) {
            if (userTokenRefreshing) {
                Log.w(TAG, "authenticate: userToken is refreshing.");
                return request;
            }
            try {
                String requestUrlPath = request.url().url().getPath();
                refreshUserToken(requestUrlPath);
                Log.w(TAG, "authenticate: Finished.");
            } catch (Exception e) {
                Log.w(TAG, "authenticate: Failed: " + e, e);
            }
        }
        return request;
    }

    public void refreshUserToken(String requestUrlPath) {
        //TODO Token过期 重新执行登录认证接口，刷新token(获取并保存)
    }
}
