package com.hulk.android.http.conn;

import android.content.Context;
import android.util.Log;

import com.hulk.android.http.ssl.NoneHostnameVerifier;
import com.hulk.android.http.ssl.SSLUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

/**
 * Http 管理器
 * @author: zhanghao
 * @Time: 2021-02-10 12:36
 */
public class HttpManager {

    private static final String TAG = "HttpManager";
    private static final String KEY_STORE_TYPE = "JKS";

    public static final int REQUEST_TIMEOUT = 15 * 1000;

    static SSLSocketFactory sCustomSSLSocketFactory = null;
    static HostnameVerifier sCustomHostnameVerifier = null;

    public static SSLSocketFactory getCustomSSLSocketFactory() {
        if (sCustomSSLSocketFactory == null) {
            sCustomSSLSocketFactory = SSLUtils.getSSLSocketFactory();
        }
        return sCustomSSLSocketFactory;
    }

    public static HostnameVerifier getCustomHostnameVerifier() {
        if (sCustomHostnameVerifier == null) {
            sCustomHostnameVerifier = new NoneHostnameVerifier();
        }
        return sCustomHostnameVerifier;
    }

    public static HttpsURLConnection getHttpsConnection(String url,
                                                        int connectTimeout, int readTimeout,
                                                        boolean doInput, boolean doOutput) throws IOException {
        URL uRL = new URL(url);
        HttpsURLConnection conn = (HttpsURLConnection) uRL.openConnection();
        SSLSocketFactory sf = getCustomSSLSocketFactory();
        if (sf != null) {
            conn.setSSLSocketFactory(sf);
        }
        HostnameVerifier hv = getCustomHostnameVerifier();
        if (hv != null) {
            conn.setHostnameVerifier(hv);
        }
        conn.setConnectTimeout(connectTimeout);
        conn.setReadTimeout(readTimeout);
        conn.setDoInput(doInput);
        conn.setDoOutput(doOutput);
        return conn;
    }

    public static HttpsURLConnection getHttpsConnection(String url, boolean doInput, boolean doOutput) throws IOException {
        return getHttpsConnection(url, REQUEST_TIMEOUT, REQUEST_TIMEOUT, doInput, doOutput);
    }

    public static HttpsURLConnection getHttpsConnection(Context context, String url) throws IOException {
        return getHttpsConnection(url, true, true);
    }

    public static HttpURLConnection getHttpConnection(String url, int connectTimeout, int readTimeout,
                                                      boolean doInput, boolean doOutput) throws IOException {
        URL uRL = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) uRL.openConnection();
        conn.setConnectTimeout(connectTimeout);
        conn.setReadTimeout(readTimeout);
        conn.setDoInput(doInput);
        conn.setDoOutput(doOutput);
        return conn;
    }

    public static HttpURLConnection getHttpConnection(String url, boolean doInput, boolean doOutput) throws IOException {
        return getHttpConnection(url, REQUEST_TIMEOUT, REQUEST_TIMEOUT, doInput, doOutput);
    }

    public static HttpURLConnection getHttpGetConnection(String url) throws IOException {
        HttpURLConnection conn = getHttpsConnection(url, false, true);
        conn.setRequestMethod("GET");
        return conn;
    }

    public static HttpURLConnection getHttpPostConnection(String url) throws IOException {
        HttpURLConnection conn = getHttpsConnection(url, true, true);
        conn.setRequestMethod("POST");
        return conn;
    }

    public static byte[] sendGetRequest(String url) throws IOException {
        HttpURLConnection conn = null;
        InputStream inputStream = null;
        try {
            conn = getHttpGetConnection(url);
            conn.connect();
            inputStream = conn.getInputStream();
            byte[] resData = read(inputStream);
            return resData;
        } catch (IOException e) {
            Log.e(TAG, "sendGetRequest failed: " + url, e);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
    }

    public static byte[] sendPostRequest(String url, byte[] postData) throws IOException {
        HttpURLConnection conn = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            conn = getHttpPostConnection(url);
            conn.setRequestProperty("Content-Length", String.valueOf(postData.length));
            conn.setUseCaches(false);
            conn.connect();
            outputStream = conn.getOutputStream();
            outputStream.write(postData);
            outputStream.flush();
            inputStream = conn.getInputStream();
            byte[] resData = read(inputStream);
            return resData;
        } catch (IOException e) {
            Log.e(TAG, "sendGetRequest failed: " + url, e);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
    }

    /**
     * 从流中读取数据
     *
     * @param inputStream
     * @return
     * @throws Exception
     */
    public static byte[] read(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return null;
        }
        ByteArrayOutputStream outStream = null;
        try {
            outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            outStream.flush();
            byte[] data = outStream.toByteArray();
            return data;
        } finally {
            inputStream.close();
            if (outStream != null) {
                outStream.close();
            }
        }

    }

    /**
     * 测试方法.
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // 密码
        String password = "123456";
        // 密钥库
        String keyStorePath = "tomcat.keystore";
        // 信任库
        String trustStorePath = "tomcat.keystore";
        // 本地起的https服务
        String httpsUrl = "https://localhost:8443/service/httpsPost";
        // 传输文本
        String xmlStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><fruitShop><fruits><fruit><kind>萝卜</kind></fruit><fruit><kind>菠萝</kind></fruit></fruits></fruitShop>";
        SSLUtils.initHttpsURLConnection(password, keyStorePath, trustStorePath);
        // 发起请求
        sendPostRequest(httpsUrl, xmlStr.getBytes());
    }
}
