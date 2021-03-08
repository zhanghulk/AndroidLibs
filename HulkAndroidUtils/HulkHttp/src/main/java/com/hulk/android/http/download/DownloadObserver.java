package com.hulk.android.http.download;

import android.util.Log;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;

/**
 * 现在观察者
 * @author: zhanghao
 * @Time: 2021-02-26 15:20
 */
public class DownloadObserver implements Observer<ResponseBody> {
    private static final String TAG = "";

    String url;
    String filePath;
    DownloadListener downloadListener;

    public DownloadObserver(String url, String filePath) {
        this.url = url;
        this.filePath = filePath;
    }

    public DownloadObserver(String url, String filePath, DownloadListener downloadListener) {
        this.url = url;
        this.filePath = filePath;
        this.downloadListener = downloadListener;
    }

    @Override
    public void onSubscribe(@NonNull Disposable d) {
        Log.i(TAG, "onSubscribe: " + d);
        if (downloadListener != null) {
            downloadListener.onStart("onSubscribe");
        }
    }

    /**
     * 下一步,通常为请求结果, 运行在UI线程
     * @param body
     */
    @Override
    public void onNext(@NonNull ResponseBody body) {
        Log.i(TAG, "onNext: " + body);
        //可在在主线程次处理 ResponseBody
        //进度在doOnNext中更新
    }

    @Override
    public void onError(@NonNull Throwable e) {
        Log.e(TAG, "onError: " + e, e);
        if (downloadListener != null) {
            Log.w(TAG, "onComplete: downloadListener.onFailure");
            downloadListener.onFailure(e);
        }
    }

    @Override
    public void onComplete() {
        Log.w(TAG, "onComplete");
        if (downloadListener != null) {
            Log.w(TAG, "onComplete: downloadListener.onFinished");
            downloadListener.onFinished(url, filePath);
        }
    }
}
