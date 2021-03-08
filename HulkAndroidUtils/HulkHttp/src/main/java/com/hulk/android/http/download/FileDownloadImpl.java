package com.hulk.android.http.download;

import android.util.Log;

import com.hulk.android.http.content.InputWriteCallback;

/**
 * 文件保存实现
 * @author: zhanghao
 * @Time: 2021-03-05 16:46
 */
public class FileDownloadImpl implements InputWriteCallback {
    private static final String TAG = "FileSaveImpl";

    DownloadListener listener;
    String url;
    String filePath;
    long contentLength = 0;

    ProgressDispatcher dispatcher;

    boolean callbackFinished = false;

    public void setCallbackFinished(boolean callbackFinished) {
        this.callbackFinished = callbackFinished;
    }

    public FileDownloadImpl(String url, String filePath, long contentLength, DownloadListener listener) {
        this.url = url;
        this.filePath = filePath;
        this.contentLength = contentLength;
        this.listener = listener;
        dispatcher = new ProgressDispatcher(listener);
    }

    @Override
    public void onInputWriteProgress(long writtenLength) {
        dispatcher.updateProgress(writtenLength, contentLength);
    }

    @Override
    public void onInputWriteFinished(int code, String msg, Throwable error) {
        Log.w(TAG, "onInputWriteFinished: code=" + code + ", msg=" + msg + ", error=" + error);
        if (callbackFinished) {
            if (code == 0) {
                dispatcher.onFinished(url, filePath);
            } else {
                if (error == null) {
                    error = new Throwable(msg + "[" + code + "]");
                }
                dispatcher.doFailure(error);
            }
        }
    }

}
