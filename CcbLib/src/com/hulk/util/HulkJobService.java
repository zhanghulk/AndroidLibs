package com.hulk.byod.parser;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.hulk.byod.parser.entity.HulkHttpResponse;


/**
 * 5.X以上的系统时用JobService
 * Created by zhanghao on 2017/11/21.
 * refer to: http://blog.csdn.net/bboyfeiyu/article/details/44809395
 */

public class HulkJobService extends JobService {

    private static final String TAG = "HulkJobService";

    private static final int MSG_HEARTBEAT = 0x1;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_HEARTBEAT:
                    JobParameters params = (JobParameters)msg.obj;
                    startHeartbeatRequest(params);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public boolean onStartJob(JobParameters params) {
        printLog("onStartJob params JonId= : " + params.getJobId());
        Message msg = mHandler.obtainMessage(MSG_HEARTBEAT);
        msg.obj = params;
        msg.sendToTarget();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        printLog("onStopJob params JobId= : " + params.getJobId());
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        printLog("onCreate...");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String intentUri = intent != null ? intent.toUri(0) : null;
        printLog("onStartCommand intentUri: " + intentUri + ", startId= " + startId);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        printLog("onDestroy...");
    }

    public void startHeartbeatRequest(final JobParameters params) {
        String username = "zhanghao";
        HeartbeatManager.HeartbeatCallback callback = new HeartbeatManager.HeartbeatCallback() {
            @Override
            public void onFinish(HulkHttpResponse response) {
                Log.i(TAG, "Heartbeat response: " + response);
                if (response != null && response.isSuccess()) {
                    jobFinished(params, false);
                } else {
                    printLog( "Heartbeat FAILED: " + response);
                    jobFinished(params, true);//失败需要重来
                }
            }
        };
        HeartbeatManager.sendHeartbeatRequest(getApplicationContext(), username, 2, TAG, callback);
    }

    private void printLog(String text) {
        Log.w(TAG, text);
        HulkJobScheduler.getInstance(this).printLog(TAG, text);
    }
}
