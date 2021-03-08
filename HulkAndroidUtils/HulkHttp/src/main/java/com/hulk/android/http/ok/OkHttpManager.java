package com.hulk.android.http.ok;

import android.content.Context;

import com.hulk.android.http.ssl.SSLUtils;
import com.hulk.android.log.Log;
import com.hulk.android.log.LogUtil;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Authenticator;
import okhttp3.Call;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Ok http 管理器
 * @author: zhanghao
 * @Time: 2021-02-18 19:34
 */
public class OkHttpManager {

    public static final String TAG = "OkHttpManager";
    private static int CONNECTED_TIMEOUT = 30;
    private static int READ_TIMEOUT = 30;
    private static int WRITE_TIMEOUT = 30;

    public static final String DEVICE_KEY = "deviceKey";
    public static final String USER_TOKEN = "user_token";

    public static final String CONTENT_TYPE_PROTOBUF = "application/x-protobuf";
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_FORM_DATA = "multipart/form-data";

    public static final MediaType MEDIA_TYPE_PROTOBUF = MediaType.parse("application/x-protobuf;charset=utf-8");
    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json;charset=utf-8");
    public static final MediaType MEDIA_TYPE_FORM_DATA = MediaType.parse("multipart/form-data;charset=utf-8");

    private static Context sContext;

    private static volatile OkHttpClient.Builder sOkHttpBuilder;
    private static volatile OkHttpClient sOkHttpClient;
    private static volatile OkHttpClient.Builder sPlainHttpBuilder;

    protected static Authenticator authenticator;
    protected static Interceptor headerInterceptor;
    protected static Interceptor urlParamInterceptor;

    /**
     * 日志拦截器
     * 注意事项:
     * 通用接口请求需要打印response body 日志,
     * 下载不建议添加 HttpLoggingInterceptor.Level.BODY, 否则导致下载进度被卡住,
     * 直到文件下载完成后, execute()才返回,或者异步回调onResponse()或者,
     * 导致事件非常耗时,无法实现下载精度和断点续传
     */
    static HttpLoggingInterceptor sLoggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
        @Override
        public void log(String message) {
            try {
                String text = URLDecoder.decode(message, "utf-8");
                Log.i(TAG, "OKHttp.log: " + text);
            } catch (Throwable e) {
                e.printStackTrace();
                android.util.Log.e(TAG, "OKHttp.log(NotUtf-8) failed: " + message, e);
            }
        }
    });

    static CookieJar sMyCookieJar = new CookieJar() {
        @Override
        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
            OkCookieManager.saveFromResponse(url, cookies);
        }

        @Override
        public List<Cookie> loadForRequest(HttpUrl url) {
            return OkCookieManager.loadForRequest(url);
        }
    };

    static {
    }

    public static void init(Context context) {
        sContext = context;
        SSLUtils.setContext(context);
    }

    public static void setContext(Context context) {
        OkHttpManager.sContext = context;
    }

    private static Context getContext() {
        return sContext;
    }

    /**
     * 创键天机的服务器请求的 OK http client builder
     * <p>其中包含: 天机服务器的证书验证, token和参数验证等等.
     * @return
     */
    public static OkHttpClient.Builder createSslOkHttpBuilder() {
        return new OkHttpClient.Builder()
                .cookieJar(sMyCookieJar)
                .connectTimeout(CONNECTED_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .sslSocketFactory(getSSLSocketFactory(), getTrustManager())
                .hostnameVerifier(getCustomHostnameVerifier())
                .authenticator(getAuthenticator())
                .addInterceptor(getHeaderInterceptor())
                .addInterceptor(getUrlParamInterceptor());
    }

    /**
     * 获取天机的服务器请求的 OK http client builder(优先使用缓存)
     * <p>其中包含: 天机服务器的证书验证, token和参数验证等等.
     * @return
     */
    public static OkHttpClient.Builder getOkHttpBuilder() {
        if (sOkHttpBuilder == null) {
            sOkHttpBuilder = createSslOkHttpBuilder();
        }
        return sOkHttpBuilder;
    }

    /**
     * 获取天机的服务器请求的 OK http client (金莲使用缓存)
     * <p>其中包含: 天机服务器的证书验证, token和参数验证等等.
     * @return
     */
    public static OkHttpClient getOkHttpClient() {
        if (sOkHttpClient == null) {
            synchronized (OkHttpManager.class) {
                if (sOkHttpClient == null) {
                    sOkHttpClient = getOkHttpBuilder()
                            //通用接口请求需要打印response body 日志,
                            // 但是在文件下载响应中不能使用Level.BODY,否则会导致下载请求被卡主,
                            // 直到文件下载完成后, execute()才返回,或者异步回调onResponse()或者,
                            // 导致事件非常耗时,无法实现下载精度和断点续传
                            .addInterceptor(sLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY))
                            .build();
                }
            }
        }
        return sOkHttpClient;
    }

    /**
     * 获取下载 OK http client
     * <p>下载需要使用进度监听器,不能使用缓存单利的 ok client, 不许创建一个新的 client
     * @param listener
     * @return
     */
    public static OkHttpClient getDownloadSslClient(ProgressListener listener) {
        //下载因为需要使用进度监听器,不能使用单利的 client he builder
        // 必须每次创键一个新的client,传入回调接口,
        // 避免进度拦截器越加越多,后面的的下载进度篡改前面的下载进度
        OkHttpClient.Builder clientBuilder = createSslOkHttpBuilder();
        if (listener != null) {
            clientBuilder.addNetworkInterceptor(new ProgressInterceptor(listener));
        }
        return clientBuilder.build();
    }

    public static OkHttpClient getDownloadSslClient() {
        return getDownloadSslClient(null);
    }

    /**
     * 浏览器下载OK http client, 不含有证书和域名等等验证
     * 浏览器下载可能其他服务器,不是天机服务器,不能验证生疏等等
     * @return
     */
    public static OkHttpClient getBrowserDownloadClient(ProgressListener listener) {
        OkHttpClient.Builder clientBuilder = getPlainHttpBuilder(listener);
        return clientBuilder.build();
    }

    public static OkHttpClient getBrowserDownloadClient() {
        return getBrowserDownloadClient(null);
    }

    /**
     * 创键极简素版本 OK http client builder.
     * @return
     */
    public static OkHttpClient.Builder getPlainHttpBuilder() {
        if (sPlainHttpBuilder == null) {
            sPlainHttpBuilder = createPlainHttpBuilder();
        }
        return sPlainHttpBuilder;
    }

    /**
     * 创键极简素版本 OK http client builder.
     * @return
     */
    public static OkHttpClient.Builder getPlainHttpBuilder(ProgressListener listener) {
        if (listener != null) {
            return createPlainHttpBuilder(listener);
        } else {
            return getPlainHttpBuilder();
        }
    }

    /**
     * 创键极简素版本 OK http client builder.
     * @return
     */
    public static OkHttpClient.Builder createPlainHttpBuilder() {
        return createPlainHttpBuilder(null);
    }

    /**
     * 创键极简素版本 OK http client builder.
     * @param listener
     * @return
     */
    public static OkHttpClient.Builder createPlainHttpBuilder(ProgressListener listener) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .cookieJar(sMyCookieJar)
                .connectTimeout(CONNECTED_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS);
        if (listener != null) {
            builder.addNetworkInterceptor(new ProgressInterceptor(listener));
        }
        return builder;
    }

    public static SSLSocketFactory getSSLSocketFactory() {
        return SSLUtils.getSSLSocketFactory(getContext());
    }

    public static X509TrustManager getTrustManager() {
        return SSLUtils.getLocalCustomManager(getContext());
    }

    public static HostnameVerifier getCustomHostnameVerifier() {
        return SSLUtils.getCustomHostnameVerifier();
    }

    public static Authenticator getAuthenticator() {
        if (authenticator == null) {
            authenticator = new OkTokenAuthenticator(getContext());
        }
        return authenticator;
    }

    public static void setAuthenticator(Authenticator authenticator) {
        OkHttpManager.authenticator = authenticator;
    }

    public static Interceptor getHeaderInterceptor() {
        if (headerInterceptor == null) {
            headerInterceptor = new OkHeaderInterceptor(getContext());
        }
        return headerInterceptor;
    }

    public static void setHeaderInterceptor(Interceptor headerInterceptor) {
        OkHttpManager.headerInterceptor = headerInterceptor;
    }

    public static Interceptor getUrlParamInterceptor() {
        if (urlParamInterceptor == null) {
            urlParamInterceptor = new OkUrlParamInterceptor(getContext());
        }
        return urlParamInterceptor;
    }

    public static void setUrlParamInterceptor(Interceptor urlParamInterceptor) {
        OkHttpManager.urlParamInterceptor = urlParamInterceptor;
    }

    public static Call createCall(Request request) {
        OkHttpClient okHttpClient = getOkHttpClient();
        Call call = okHttpClient.newCall(request);
        return call;
    }

    public static Response executeRequest(Request request) throws IOException {
        Call call = createCall(request);
        return call.execute();
    }

    /**
     * 请求服务器, eg: 上传文件
     * @param url
     * @param requestBody
     * @return
     * @throws IOException
     */
    public static Response executeRequest(String url, RequestBody requestBody) throws IOException {
        if (requestBody == null) {
            logw("executeRequest: requestBody is null");
            return null;
        }
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        Response response = OkHttpManager.executeRequest(request);
        boolean isSuccessful = response != null ? response.isSuccessful() : false;
        if (isSuccessful) {
            logi("executeRequest response: " + response);
        } else {
            logw("executeRequest Failed response: " + response);
        }
        return response;
    }

    /**
     * 请求服务器, eg: 上传文件
     * @param url
     * @param multipartBody 表格提交数据 eg: 文件上传
     * @return
     * @throws IOException
     */
    public static Response uploadMultipartData(String url, MultipartBody multipartBody) throws IOException {
        if (multipartBody == null) {
            logw("uploadMultipartData: multipartBody is null");
            return null;
        }
        return executeRequest(url, multipartBody);
    }

    private static void logw(String msg) {
        LogUtil.w(TAG, msg + ", date: " + new Date().toLocaleString());
    }

    private static void logi(String msg) {
        LogUtil.i(TAG, msg + ", date: " + new Date().toLocaleString());
    }
}
