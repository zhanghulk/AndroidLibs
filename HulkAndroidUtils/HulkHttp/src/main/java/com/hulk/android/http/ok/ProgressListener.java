package com.hulk.android.http.ok;

import java.io.IOException;

import okhttp3.Response;

/**
 * 下载进度监听器
 * @author: zhanghao
 * @Time: 2021-02-24 22:10
 */
public interface ProgressListener {
    /**
     * 开始下载之前的回调函数
     * @param contentLength 响应内容总长度 (未下载的长度)
     *                      断点续传下载事次值小于或等于文件大小, 在使用时要加上已经下载的数据长度
     * @param response
     */
    void onPreExecute(long contentLength, Response response);

    /**
     * 进度更新
     * @param totalBytesRead 已经读取的字节数
     * @param done 是否读取完毕
     * @param totalBytesRead
     * @param done
     * @throws IOException
     */
    void update(long totalBytesRead, boolean done, Response response);

    /**
     * 错误
     * @param throwable
     */
    void onError(Throwable throwable, Response response);
}
