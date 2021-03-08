package com.hulk.android.http.upload;

import com.hulk.android.http.conn.HttpResult;
import com.hulk.android.http.retrofit.RetrofitManager;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * 文件上传管理器
 * @author: zhanghao
 * @Time: 2021-03-04 18:02
 */
public class UploadManager {
    private static final String TAG = "UploadManager";

    public static void uploadImg(String text, String filePath, UploadCallback callback) {
        UploadApiService service = RetrofitManager.getInstance().getRetrofit().create(UploadApiService.class);
        RequestBody textBody = RequestBody.create(MediaType.parse("text/plain"), text);
        File file = new File(filePath);
        RequestBody fileBody = RequestBody.create(MediaType.parse("image/png"), file);

        Observable<HttpResult> observable = service.uploadImg(textBody, fileBody);
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new UploadObserver(callback));
    }

    public static void uploadI(String url, MultipartBody multipartBody, UploadCallback callback) {
        UploadApiService service = RetrofitManager.getInstance().getRetrofit().create(UploadApiService.class);
        Observable<HttpResult> observable = service.upLoad(url, multipartBody);
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new UploadObserver(callback));
    }
}
