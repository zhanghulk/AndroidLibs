package com.hulk.android.http.retrofit;

import android.text.TextUtils;
import android.util.Log;

import com.hulk.android.http.ok.OkHttpManager;
import com.hulk.android.http.ok.ProgressListener;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit 管理器
 * @author: zhanghao
 * @Time: 2021-03-04 16:00
 */
public class RetrofitManager {
    private static final String TAG = "RetrofitManager";

    Retrofit mRetrofit;
    String mBaseUrl;

    /**
     * 浏览器模式,此模式 Ok http client 下不执行SSL证书等等验证,直接下载
     */
    boolean browserMode = true;

    /**
     * 全局精度监听器, 与 OK http 绑定
     */
    ProgressListener mProgressListener;

    static RetrofitManager instance = new RetrofitManager();

    public static RetrofitManager getInstance() {
        return instance;
    }

    private RetrofitManager() {
    }

    public void setProgressListener(ProgressListener progressListener) {
        this.mProgressListener = progressListener;
    }

    public void setBrowserMode(boolean browserMode) {
        this.browserMode = browserMode;
    }

    public void setBaseUrl(String baseUrl) {
        this.mBaseUrl = baseUrl;
    }

    public String getBaseUrl() {
        return mBaseUrl;
    }

    public OkHttpClient getOkHttpClient() {
        return getOkHttpClient(mProgressListener);
    }

    public OkHttpClient getOkHttpClient(ProgressListener listener) {
        if (listener == null) {
            Log.w(TAG, "getOkHttpClient: progress listener is null");
        }
        if (browserMode) {
            return OkHttpManager.getBrowserDownloadClient(listener);
        }
        return OkHttpManager.getDownloadSslClient(listener);
    }

    public Retrofit getRetrofit() {
        ensureRetrofit();
        return mRetrofit;
    }

    public static Retrofit createRetrofit(String baseUrl, OkHttpClient okHttpClient) {
        //可以增加其他转换器
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(ToByteConvertFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        return retrofit;
    }

    public Retrofit createRetrofit(OkHttpClient okHttpClient) {
        String baseUrl = getAvailableBaseUrl();
        return createRetrofit(baseUrl, okHttpClient);
    }

    public String getAvailableBaseUrl() {
        if (TextUtils.isEmpty(mBaseUrl)) {
            mBaseUrl = "https://www.baidu.com";
        }
        return mBaseUrl;
    }

    private void initRetrofit() {
        initRetrofit(getOkHttpClient());
    }

    public void initRetrofit(OkHttpClient okHttpClient) {
        mRetrofit = createRetrofit(okHttpClient);
    }

    private void initRetrofit(ProgressListener progressListener) {
        initRetrofit(getOkHttpClient(progressListener));
    }

    private void ensureRetrofit() {
        if (mRetrofit == null) {
            initRetrofit();
        }
    }
}
