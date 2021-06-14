package com.hulk.android.http.download;

/**
 * 下载监听器
 * @author: zhanghao
 * @Time: 2021-02-25 21:36
 */
public interface DownloadListener {
    /**
     * 开始下载
     * @param remark
     */
    void onStart(String remark);

    /**
     * 下载进度
     * @param progress
     */
    void onProgress(int progress);

    /**
     * 下载结束
     * @param url 下载服务器地址
     * @param filePath 下载后的文件保存地址
     */
    void onFinished(String url, String filePath);

    /**
     * 下载失败
     * @param throwable
     */
    void onFailure(Throwable throwable);
}
