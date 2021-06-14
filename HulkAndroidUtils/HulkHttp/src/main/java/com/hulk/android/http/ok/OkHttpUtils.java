package com.hulk.android.http.ok;

import android.text.TextUtils;
import android.util.Log;

import com.hulk.android.http.conn.HttpException;
import com.hulk.android.http.conn.HttpResult;
import com.hulk.android.http.content.StreamTool;
import com.hulk.android.http.utils.UrlParser;
import com.hulk.android.log.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Ok http 请求工具类
 * @author hulk
 */
public class OkHttpUtils {

    private static final String TAG = "OkHttpUtils";
    public static final int REQUEST_TIMEOUT = 30 * 1000;
    public static final String CONTENT_TYPE_PROTOBUF = OkHttpManager.CONTENT_TYPE_PROTOBUF;

    /**
     * 发起 http het 请求
     * <p>可用于文下载和普通接口请求
     * <p>使用完成一定要记得关闭 ResponseBody(close)
     * @param url
     * @return
     * @throws IOException
     * @throws HttpException
     */
    public static Response sendOkHttpGetRequest(String url) throws IOException {
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .get();
        logi("sendOkHttpGetRequest url: " + url);
        Response response = OkHttpManager.executeRequest(requestBuilder.build());
        return response;
    }

    /**
     * 发起 http het 请求
     * <p>可用于文下载和普通接口请求
     * <p>使用完成一定要记得关闭 ResponseBody(close)
     * @param url
     * @return
     * @throws IOException
     * @throws HttpException
     */
    public static ResponseBody sendHttpGetRequest(String url)
            throws IOException, HttpException {
        Response response = sendOkHttpGetRequest(url);
        int code = response.code();
        if (code != OkHttpStatus.SC_OK) {
            LogUtil.e(TAG, "sendHttpGetRequest: Failed HttpException code= " + code);
            throw new HttpException(code, url);
        } else {
            LogUtil.i(TAG, "sendHttpGetRequest: SC_OK");
        }
        ResponseBody responseBody = response.body();
        return responseBody;
    }

    /**
     * 发起 http het 请求
     * <p>可用于文下载和普通接口请求
     * <p>使用完成一定要记得关闭输入流(close)
     * @param url
     * @return
     * @throws IOException
     * @throws HttpException
     */
    public static InputStream sendInputGetRequest(String url)
            throws IOException, HttpException {
        ResponseBody responseBody = sendHttpGetRequest(url);
        if (responseBody != null) {
            InputStream inputStream = responseBody.byteStream();
            return inputStream;
        } else {
            LogUtil.e(TAG, "sendInputGetRequest responseBody is null ");
        }
        return null;
    }

    /**
     * Get方式请求网络接口，返回byte数组，可以使json字字符串，或者图片视频等等
     * @param url
     * @return
     * @throws IOException
     * @throws HttpException
     */
    public static byte[] sendDataGetRequest(String url) throws IOException, HttpException {
        InputStream inputStream = sendInputGetRequest(url);
        if (inputStream != null) {
            try {
                byte[] data = StreamTool.read(inputStream);
                if (data != null) {
                    logi("sendDataGetRequest success response data.length= " + data.length);
                    return data;
                } else {
                    logw("sendDataGetRequest failed to read inputStream ");
                }
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        } else {
            LogUtil.w(TAG, "sendGetDataRequest Content inputStream is null ");
        }
        return null;
    }

    /**
     * Get方式请求网络接口，返回 String
     * @param url
     * @return
     * @throws IOException
     * @throws HttpException
     */
    public static String sendStringGetRequest(String url) throws IOException, HttpException {
        byte[] data = sendDataGetRequest(url);
        if (data != null) {
            String str = new String(data);
            return str;
        } else {
            LogUtil.w(TAG, "sendStringGetRequest: data is null ");
        }
        return null;
    }

    /**
     * Get方式请求网络接口，返回 String
     * @param url
     * @return
     * @throws IOException
     * @throws HttpException
     */
    public static HttpResult<String> sendResultGetRequest(String url) throws IOException, HttpException {
        HttpResult<String> result = new HttpResult<>(-1, "");
        byte[] data = sendDataGetRequest(url);
        if (data != null) {
            try {
                result = parseJsonData(data);
            } catch (JSONException e) {
                Log.w(TAG, "sendResultGetRequest failed: " + e, e);
            }
        } else {
            result = new HttpResult<>(-1, "data is null");
            LogUtil.w(TAG, "sendGetResultRequest: data is null ");
        }
        return result;
    }

    /**
     * 解析服务器返回的通用json数据(存在错误码和错误信息)
     * @param resultData
     * @return
     * @throws JSONException
     */
    public static HttpResult<String> parseJsonData(byte[] resultData) throws JSONException {
        HttpResult<String> result = new HttpResult<>(-1, "");
        String resultStr = new String(resultData);
        result.data = resultStr;
        if (!TextUtils.isEmpty(resultStr)) {
            JSONObject resultJson = new JSONObject(resultStr);
            result.code = resultJson.optInt("code");
            result.msg = resultJson.optString("errorMessage");
            if (result.code != 0) {
                result.detail = resultStr;
                LogUtil.w(TAG, "parseJsonData failedresult: " + result);
            } else {
                LogUtil.i(TAG, "parseJsonData result: " + result);
            }
        } else {
            LogUtil.w(TAG, "parseJsonData: resultStr is empty: " + result);
        }
        return result;
    }

    /**
     * Post方式请求网络接口，返回byte数组，可以使json字字符串，或者图片视频等等
     * @param url 服务器接口地址
     * @param mediaType okhttp3.MediaType onject eg: "application/json" or CONTENT_TYPE_PROTOBUF...
     * @param postData
     * @return
     * @throws RuntimeException
     * @throws IOException
     * @throws HttpException
     */
    public static byte[] sendHttpPostRequest(String url, MediaType mediaType, byte[] postData)
            throws IOException, RuntimeException, HttpException {
        if (TextUtils.isEmpty(url)) {
            logw("sendHttpPostRequest: failed for invalid url: " + url);
            throw new IllegalArgumentException("url is null");
        }
        if (TextUtils.isEmpty(url)) {
            logw("sendHttpPostRequest: postData is null");
            throw new IllegalArgumentException("postData is null");
        }
        if (mediaType == null) {
            logw("sendHttpPostRequest: mediaType is null");
            throw new IllegalArgumentException("mediaType is null");
        }
        RequestBody requestBody = RequestBody.create(mediaType, postData);
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(requestBody);
        Response response = OkHttpManager.executeRequest(requestBuilder.build());
        ResponseBody responseBody = null;
        logw("sendHttpPostRequest url: " + url + ", postData length: " + postData.length);
        try {
            int code = response.code();
            logw("sendHttpPostRequest Http Status Code " + code);
            if (code != OkHttpStatus.SC_OK) {
                LogUtil.e(TAG, "Throw HttpException code= " + code + ", url path: " + UrlParser.getUrlPath(url));
                throw new HttpException(code, url);
            }
            responseBody = response.body();
            if (responseBody != null) {
                InputStream inputStream = responseBody.byteStream();
                if (inputStream != null) {
                    byte[] data = StreamTool.read(inputStream);
                    if (data != null) {
                        //logi("sendHttpGetRequest success response data: " + Arrays.toString(data));
                        logi("sendHttpPostRequest success response data.length= " + data.length);
                        return data;
                    } else {
                        logw("sendHttpPostRequest failed to read inputStream ");
                    }
                } else {
                    LogUtil.w(TAG, "sendHttpPostRequest Content inputStream is null ");
                }
            } else {
                LogUtil.e(TAG, "sendHttpPostRequest HttpEntity is null ");
            }
        } finally {
            if (responseBody != null) {
                responseBody.close();
            }
        }
        return null;
    }

    /**
     * Post方式请求网络接口，返回byte数组，可以使json字字符串，或者图片视频等等
     * @param url 服务器地址
     * @param contentType "application/json" or CONTENT_TYPE_PROTOBUF...
     * @param url
     * @return
     * @throws RuntimeException
     * @throws IOException
     * @throws HttpException
     */
    public static byte[] sendHttpPostRequest(String url, String contentType, byte[] postData)
            throws IOException, RuntimeException, HttpException {
        MediaType mediaType = MediaType.parse(contentType);
        return sendHttpPostRequest(url, mediaType, postData);
    }

    /**
     * Post方式请求网络接口，返回byte数组
     * <p>contentType = CONTENT_TYPE_PROTOBUF;
     * @param url 服务器地址
     * @param url
     * @return
     * @throws RuntimeException
     * @throws IOException
     * @throws HttpException
     */
    public static byte[] sendProtoPostRequest(String url, byte[] requestData)
            throws IOException, RuntimeException, HttpException {
        String contentType = CONTENT_TYPE_PROTOBUF;
        return sendHttpPostRequest(url, contentType, requestData);
    }

    /**
     * Post方式请求网络接口，返回byte数组
     * <p>contentType = "application/json";
     * @param url 服务器地址
     * @param url
     * @param requestData
     * @return
     * @throws IOException
     * @throws RuntimeException
     * @throws HttpException
     */
    public static byte[] sendJsonPostRequest(String url, byte[] requestData)
            throws IOException, RuntimeException, HttpException {
        String contentType = OkHttpManager.CONTENT_TYPE_JSON;
        return sendHttpPostRequest(url, contentType, requestData);
    }

    /**
     * Post方式请求网络接口，返回 String
     * <p>contentType = "application/json";
     * @param url 服务器地址
     * @param url
     * @param requestData
     * @return
     * @throws IOException
     * @throws RuntimeException
     * @throws HttpException
     */
    public static String sendStringJsonPostRequest(String url, byte[] requestData)
            throws IOException, RuntimeException, HttpException {
        byte[] res = sendJsonPostRequest(url, requestData);
        return new String(res, "UTF-8");
    }

    /**
     * Post发送json数据 (通用)
     * @param url
     * @param requestData
     * @return  HttpResult<String> 对象，其中data为json eg: {"code":0,"errorMessage":"Success"}
     */
    public static HttpResult<String> sendJsonPostRequestCommon(String url, byte[] requestData) {
        HttpResult<String> result = new HttpResult<>(-1, "");
        try {
            byte[] resultData = sendJsonPostRequest(url, requestData);
            if (resultData != null) {
                try {
                    result = parseJsonData(resultData);
                } catch (JSONException e) {
                    Log.w(TAG, "sendGetResultRequest failed: " + e, e);
                }
            } else {
                result = new HttpResult<>(-1, "data is null");
                LogUtil.w(TAG, "sendGetResultRequest: data is null ");
            }
        } catch (Exception e) {
            String detail = e + ", url= " + url;
            if (e instanceof HttpException) {
                result.code = ((HttpException)e).code();
            }
            result.msg = e.getMessage();
            result.detail = detail;
            result.error = e;
            LogUtil.e(TAG, "sendJsonPostRequestCommon failed: " + e + ", detail= " + detail, e);
        }
        return result;
    }

    /**
     * 请求服务器, eg: 上传文件
     * @param url
     * @param requestBody
     * @return
     * @throws IOException
     */
    public static Response executeRequest(String url, RequestBody requestBody) throws IOException {
        return OkHttpManager.executeRequest(url, requestBody);
    }

    /**
     * 请求服务器, eg: 上传文件
     * @param url
     * @param multipartBody 表格提交数据 eg: 文件上传
     * @return
     * @throws IOException
     */
    public static Response uploadMultipartData(String url, MultipartBody multipartBody) throws IOException {
        return OkHttpManager.uploadMultipartData(url, multipartBody);
    }

    /**
     * 上传文件(可多个)
     * @param url
     * @param formDataName 表数据自丢按名称,自定义, 与服务端协商 eg: file, data ...
     * @param filePaths
     * @param url
     * @param formDataName
     * @param filePaths
     * @return
     * @throws IOException
     */
    public static Response uploadFiles(String url, String formDataName, String... filePaths) throws IOException {
        MultipartBody.Builder multipartBuilder = OkRequestUtils.createMultipartBody(formDataName, filePaths);
        if (multipartBuilder == null) {
            logw("uploadFiles: multipartBuilder si null");
            return null;
        }
        MultipartBody requestBody = multipartBuilder.build();
        Response response = uploadMultipartData(url, requestBody);
        return response;
    }

    /**
     * 上传文件
     * @param url
     * @param formDataName
     * @param filePath
     * @return
     * @throws IOException
     */
    public static String uploadFile(String url, String formDataName, String filePath) throws IOException {
        logi("uploadFile: formDataName=" + formDataName + ", filePath= " + filePath);
        Response response = uploadFiles(url, formDataName, filePath);
        ResponseBody responseBody = null;
        try {
            if (response != null) {
                responseBody = response.body();
                String resStr = new String(responseBody.bytes());
                logw("uploadFile: " + resStr);
                return resStr;
            } else {
                logw("uploadFile: response is null");
            }
        } finally {
            if (responseBody != null) {
                responseBody.close();
            }
        }

        return "";
    }

    private static void logw(String msg) {
        LogUtil.w(TAG, msg + ", date: " + new Date().toLocaleString());
    }

    private static void logi(String msg) {
        LogUtil.i(TAG, msg + ", date: " + new Date().toLocaleString());
    }
}
