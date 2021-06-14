package com.hulk.android.http.upload;

import com.hulk.android.http.conn.HttpResult;

import java.util.Map;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Url;

/**
 * 上传文件接口
 * @author: zhanghao
 * @Time: 2021-02-26 10:41
 */
public interface UploadApiService {

    /**
     * 上传单个文本和单个文件
     * @param textBody 文本数据: text 字段名称, 实际开发中在确定.
     * @param fileBody  文件数据: file 为字段名称, test.png 为服务器上保存的文件名, 实际开发中在确定,
     * @return
     */
    @Multipart
    @POST("v1/uploadImg")
    Observable<HttpResult> uploadImg(@Part("text") RequestBody textBody, @Part("file\"; filename=\"test.png") RequestBody fileBody);

    /**
     * 上传多个文本和多个文件(传递map)
     * @param textBodyMap 文本map
     * @param fileBodyMap  文件map
     * @return
     */
    @Multipart
    @POST("v1/uploadFiles")
    Observable<HttpResult> upLoad(@PartMap Map<String, RequestBody> textBodyMap, @PartMap Map<String, RequestBody> fileBodyMap);

    /**
     * 上传多个文本和多个文件, 传递 MultipartBody:
     * MultipartBody.Builder builder = new MultipartBody.Builder();
     * //文本部分
     * builder.addFormDataPart("title", "User comments");
     * builder.addFormDataPart("content", "The retrofit+Rxjava is perfect");
     *
     * //文件部分
     * File file1 = new File("hulk.jpg");
     * RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpg"), file1);
     * // “image”为文件参数的参数名
     * builder.addFormDataPart("image", file1.getName(), requestBody);
     *
     * File file2 = new File("hulk.mp4");
     * RequestBody requestBody2 = RequestBody.create(MediaType.parse("video/mp4"), file2);
     * // “video”为文件参数的参数名
     * builder.addFormDataPart("video", file2.getName(), requestBody2);
     *
     * builder.setType(MultipartBody.FORM);
     * MultipartBody multipartBody = builder.build();
     *
     * @param url  直接使用完整的 url
     * @param multipartBody
     * @return
     */
    @POST
    Observable<HttpResult> upLoad(@Url String url, @Body MultipartBody multipartBody);
}
