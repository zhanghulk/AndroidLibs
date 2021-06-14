package com.hulk.byod.parser.net;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.hulk.byod.parser.xml.httpbody.IHttpBody;
import com.hulk.byod.parser.entity.HulkHttpResponse;

/**
 *认证服务器请求异步任务统一入口
 * Created by zhanghao on 2017/11/20.
 */

public class HulkHttpTask extends AsyncTask<IHttpBody, Integer, HulkHttpResponse> {

    protected final static String TAG = "HulkHttpTask";
    protected final static boolean DEBUG = true;

    Context mContext;
    HulkHttpCallback mCallBack;

    public HulkHttpTask(Context context, HulkHttpCallback callBack) {
        this.mContext = context;
        this.mCallBack = callBack;
    }

    @Override
    protected HulkHttpResponse doInBackground(IHttpBody... params) {
        IHttpBody body = params[0];
        String tranCode = body.getTradeCode();
        String postText = body.formatAsXml(mContext, false);
        Log.i(TAG, "doInBackground start to request tranCode: " + tranCode + ", postText: " + postText);
        HulkHttpResponse response;
        if (DEBUG) {
            response = HulkHttpUtils.testHttpRequest(mContext, tranCode, postText);
        } else {
            response = HulkHttpUtils.startHttpRequest(mContext, tranCode, postText);
        }
        return response;
    }

    @Override
    protected void onPostExecute(HulkHttpResponse response) {
        Log.w(TAG, "onPostExecute HulkResponse: " + response);
        if (mCallBack != null) {
            mCallBack.onHttpResult(response, null);
        }
    }

    public void execute(IHttpBody body) {
        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, body);
    }
}
