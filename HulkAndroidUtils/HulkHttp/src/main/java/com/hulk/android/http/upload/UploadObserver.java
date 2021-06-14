package com.hulk.android.http.upload;

import android.util.Log;

import com.hulk.android.http.conn.HttpResult;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

/**
 * 上传观察者
 * @author: zhanghao
 * @Time: 2021-03-04 18:32
 */
public class UploadObserver implements Observer<HttpResult> {

    private static final String TAG = "UploadObserver";
    UploadCallback callback;

    public UploadObserver() {
    }

    public UploadObserver(UploadCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onSubscribe(@NonNull Disposable d) {
        Log.w(TAG, "onSubscribe: " + d);
    }

    @Override
    public void onNext(@NonNull HttpResult result) {
        Log.w(TAG, "onNext: " + result);
        if (callback != null) {
            callback.onUpload(result);
        }
    }

    @Override
    public void onError(@NonNull Throwable e) {
        Log.e(TAG, "onError: " + e);
        if (callback != null) {
            HttpResult result = new HttpResult();
            result.error = e;
            callback.onUpload(result);
        }
    }

    @Override
    public void onComplete() {
        Log.w(TAG, "onComplete");
    }
}
