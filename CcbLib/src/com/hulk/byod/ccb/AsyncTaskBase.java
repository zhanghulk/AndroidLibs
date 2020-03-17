package com.hulk.byod.ccb;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * 网络请求异步任务基类
 * Created by zhanghao on 17-12-18.
 */

public abstract class AsyncTaskBase<Result> extends AsyncTask<String, Integer, Result> {
    protected String TAG = "AsyncTaskBase";
    protected Context mContext;
    protected String serverUrl;//请求的天机服务器地址
    protected String params;//执行异步任务的参数
    protected String remark;
    protected boolean executing = false;


    public AsyncTaskBase(Context context, String serverUrl, String remark, String tag) {
        this.mContext = context;
        this.serverUrl = serverUrl;
        this.remark = remark;
        this.TAG = tag;
    }

    public boolean isExecuting() {
        return executing;
    }

    public void execute() {
        Log.i(TAG, remark + " >> execute params: " + params);
        executing = true;
        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (params != null ? params : remark));
    }

    @Override
    protected void onPostExecute(Result result) {
        executing = false;
    }
}
