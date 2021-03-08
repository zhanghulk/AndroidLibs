package com.hulk.android.http.download;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * 下载API
 * @author: zhanghao
 * @Time: 2021-02-25 21:29
 */
public interface DownloadApiService {
    /**
     * 下载通用函数
     * @param url
     * @return
     */
    @Streaming
    @GET
    Observable<ResponseBody> download(@Url String url);
}
