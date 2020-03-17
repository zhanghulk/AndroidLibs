package com.hulk.byod.ccb.net;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.hulk.byod.ccb.xml.httpbody.IHttpBody;
import com.hulk.byod.ccb.entity.CCBHttpResponse;

/**
 *建行认证服务器请求异步任务统一入口
 * Created by zhanghao on 2017/11/20.
 */

public class CCBHttpTask extends AsyncTask<IHttpBody, Integer, CCBHttpResponse> {

    protected final static String TAG = "CCBHttpTask";
    protected final static boolean DEBUG = true;

    Context mContext;
    CCBHttpCallback mCallBack;

    public CCBHttpTask(Context context, CCBHttpCallback callBack) {
        this.mContext = context;
        this.mCallBack = callBack;
    }

    @Override
    protected CCBHttpResponse doInBackground(IHttpBody... params) {
        IHttpBody body = params[0];
        String tranCode = body.getTradeCode();
        String postText = body.formatAsXml(mContext, false);
        Log.i(TAG, "doInBackground start to request tranCode: " + tranCode + ", postText: " + postText);
        CCBHttpResponse response;
        if (DEBUG) {
            response = CCBHttpUtils.testHttpRequest(mContext, tranCode, postText);
        } else {
            response = CCBHttpUtils.startHttpRequest(mContext, tranCode, postText);
        }
        return response;
    }

    @Override
    protected void onPostExecute(CCBHttpResponse response) {
        Log.w(TAG, "onPostExecute CCBResponse: " + response);
        if (mCallBack != null) {
            mCallBack.onHttpResult(response, null);
        }
    }

    public void execute(IHttpBody body) {
        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, body);
    }
}
