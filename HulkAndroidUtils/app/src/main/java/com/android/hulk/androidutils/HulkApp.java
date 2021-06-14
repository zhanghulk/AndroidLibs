package com.android.hulk.androidutils;

import android.app.Application;
import android.content.Context;

import com.hulk.android.http.ok.OkHttpManager;
import com.hulk.android.log.PrintLogUtils;
import com.hulk.android.log.Log;

/**
 * @author: zhanghao
 * @Time: 2021-03-03 18:54
 */
public class HulkApp extends Application {

    private static final String TAG = "HulkApp";

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        PrintLogUtils.initProcessLog(context);
        OkHttpManager.init(context);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.w(TAG, "onCreate: ");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.w(TAG, "onTerminate: ");
    }
}
