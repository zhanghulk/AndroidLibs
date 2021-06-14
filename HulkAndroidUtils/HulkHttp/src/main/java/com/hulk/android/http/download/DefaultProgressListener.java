package com.hulk.android.http.download;

import android.util.Log;

import com.hulk.android.http.ok.ProgressListener;

import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 默认下载进度下载调度器
 * @author: zhanghao
 * @Time: 2021-02-26 16:10
 */
public class DefaultProgressListener implements ProgressListener {
    private static final String TAG = "DefaultProgressListener";

    public long contentLength;
    public ResponseBody responseBody;
    public long totalBytesRead;
    public boolean done;
    public Throwable throwable;

    DownloadListener downloadListener;
    ProgressDispatcher dispatcher;
    int progress = 0;
    String url;
    String filePath;

    boolean downloadObserverMode = false;

    public DefaultProgressListener(String url, String filePath) {
        this.url = url;
        this.filePath = filePath;
        init();
    }

    public DefaultProgressListener(String url, String filePath, DownloadListener downloadListener) {
        this.url = url;
        this.filePath = filePath;
        this.downloadListener = downloadListener;
        init();
    }

    public void init() {
        dispatcher = new ProgressDispatcher(downloadListener);
    }

    public boolean isDownloadObserverMode() {
        return downloadObserverMode;
    }

    public void setDownloadObserverMode(boolean downloadObserverMode) {
        this.downloadObserverMode = downloadObserverMode;
    }

    @Override
    public void onPreExecute(long contentLength , Response response) {
        Log.v(TAG, "onPreExecute: contentLength= " + contentLength + ", responseBody=" + responseBody);
        this.contentLength = contentLength;
        this.responseBody = responseBody;
        if (!downloadObserverMode) {
            //非observer模式,使用 progress 回调
            if (dispatcher != null) {
                dispatcher.onStart(TAG + ".onPreExecute");
            }
        }
    }

    @Override
    public void update(long totalBytesRead, boolean done, Response response) {
        this.totalBytesRead = totalBytesRead;
        this.done = done;
        if (dispatcher != null) {
            if (done) {
                Log.w(TAG, "update: done");
                dispatcher.onFinished(url, filePath);
            } else {
                progress = DownloadUtil.computeProgress(totalBytesRead, contentLength);
                boolean updated = dispatcher.onProgress(progress);
                if (updated) {
                    Log.i(TAG, "update: totalBytesRead= " + totalBytesRead + ", progress=" + progress);
                }
            }
        }
    }

    @Override
    public void onError(Throwable throwable, Response response) {
        Log.e(TAG, "onError: " + throwable);
        this.throwable = throwable;
        if (!downloadObserverMode) {
            //非observer模式,使用 progress 回调
            if (dispatcher != null) {
                dispatcher.doFailure(throwable);
            }
        }
    }
}
