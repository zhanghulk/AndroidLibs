package com.hulk.android.http.ok;

import android.text.TextUtils;

import com.hulk.android.http.content.UploadFile;
import com.hulk.android.http.utils.HttpFileUtils;
import com.hulk.android.log.Log;
import com.hulk.android.log.LogUtil;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * ok http 工具类
 * @author: zhanghao
 * @Time: 2021-02-19 15:50
 */
public class OkRequestUtils {

    private static final String TAG = "OkRequestUtils";

    /**
     * 添加请求header user token 参数
     * @param request
     * @param userToken
     * @return
     */
    public static Request addUserTokenHeader(Request request, String userToken) {
        if (TextUtils.isEmpty(userToken)) {
            Log.w(TAG, "addUserTokenHeader: The userToken is empty.");
            return request;
        }
        Request request2 = request.newBuilder()
                .header(OkHttpManager.USER_TOKEN, userToken)
                .build();
        return request2;
    }

    /**
     * 添加请求url尾部 device key 参数
     * @param request
     * @param deviceKey
     * @return
     */
    public static Request appendUrlDeviceKeyParam(Request request, String deviceKey) {
        if (TextUtils.isEmpty(deviceKey)) {
            Log.w(TAG, "appendDeviceKeyParam: The device key is empty.");
            return request;
        }
        HttpUrl url1 = request.url();
        //检查device key 参数事是否尊在, parameter is value
        String parameter = url1.queryParameter(OkHttpManager.DEVICE_KEY);
        if (!TextUtils.isEmpty(parameter)) {
            //Existed device key param, not nned add it duplicated
            Log.i(TAG, "appendDeviceKeyParam: Ignored existed device key in: " + url1);
            return request;
        }
        //copy one
        HttpUrl url2 = url1.newBuilder()
                .addQueryParameter(OkHttpManager.DEVICE_KEY, deviceKey)
                .build();
        Log.i(TAG, "appendDeviceKeyParam: Updated url: " + url2);
        Request request2 = request.newBuilder()
                .url(url2)
                .build();
        return request2;
    }

    public static RequestBody createFileBody(MediaType mediaType, File file) {
        RequestBody fileBody = RequestBody.create(mediaType, file);
        return fileBody;
    }

    public static RequestBody createFileBody(File file) {
        return createFileBody(OkHttpManager.MEDIA_TYPE_FORM_DATA, file);
    }

    public static RequestBody createFileBody(UploadFile file) {
        File file1 = file.getFile();
        MediaType.parse(file.contentType);
        return createFileBody(OkHttpManager.MEDIA_TYPE_FORM_DATA, file1);
    }

    public static UploadFile[] getUploadFiles(String... filePaths) {
        if (filePaths == null) {
            logw("getUploadFiles: filePaths is null");
            return null;
        }
        UploadFile[] files = new UploadFile[filePaths.length];
        for (int i = 0; i < filePaths.length; i++) {
            String filePath = filePaths[i];
            String filename = HttpFileUtils.getFileName(filePath);
            UploadFile uploadFile = new UploadFile(filename, filePath, OkHttpManager.CONTENT_TYPE_FORM_DATA);
            files[i] = uploadFile;
        }
        return files;
    }

    /**
     * 创键上传文件(可多个)的 MultipartBody/.Builder
     * @param formDataName 表数据自丢按名称,自定义, 与服务端协商 eg: file, data ...
     * @param formDataName
     * @param files
     * @return
     * @throws IOException
     */
    public static MultipartBody.Builder createMultipartBody(String formDataName, UploadFile... files) {
        if (files == null || files.length == 0) {
            logw("createMultipartBody: Invalid files " + files);
            return null;
        }
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        for (UploadFile uploadFile: files) {
            String filePath = uploadFile.path;
            if (TextUtils.isEmpty(filePath)) {
                logw("createMultipartBody: filePath is empty");
                continue;
            }
            File file = new File(filePath);
            if (file.exists()) {
                logw("createMultipartBody: filePath= " + filePath);
                String fileName = uploadFile.fileName;
                RequestBody fileBody = createFileBody(file);
                String name = formDataName;
                if (TextUtils.isEmpty(formDataName)) {
                    name = fileName;
                    logw("createMultipartBody: formDataName is fileName " + fileName);
                }
                builder.addFormDataPart(name, fileName, fileBody);
            } else {
                logw("createMultipartBody: Not exists filePath " + filePath);
            }
        }
        return builder;
    }

    public static MultipartBody.Builder createMultipartBody(String formDataName, String... filePaths) {
        return createMultipartBody(formDataName, getUploadFiles(filePaths));
    }

    private static void logw(String msg) {
        LogUtil.w(TAG, msg + ", date: " + new Date().toLocaleString());
    }

    private static void logi(String msg) {
        LogUtil.i(TAG, msg + ", date: " + new Date().toLocaleString());
    }
}
