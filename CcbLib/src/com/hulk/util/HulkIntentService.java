package com.hulk.byod.parser;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.hulk.byod.parser.entity.HulkHttpResponse;

/**
 * 5.0一下的系统时用 HulkIntentService
 * Created by zhanghao on 18-1-12.
 */

public class HulkIntentService extends IntentService {

    private static final String TAG = "HulkIntentService";

    public HulkIntentService() {
        super(TAG);
        setIntentRedelivery(true);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            Log.w(TAG, "onHandleIntent: intent is null !! ");
            return;
        }
        String action = intent.getAction();
        printLog("onHandleIntent: " + action);
        if (HeartbeatManager.ACTION_HEARTBEAT.equals(action)) {
            startHeartbeatRequest();
        }
    }

    public void startHeartbeatRequest() {
        String username = "zhanghao";
        HeartbeatManager.HeartbeatCallback callback = new HeartbeatManager.HeartbeatCallback() {
            @Override
            public void onFinish(HulkHttpResponse response) {
                if (response != null && response.isSuccess()) {
                    Log.i(TAG, "Heartbeat: " + response);
                } else {
                    printLog("Heartbeat FAILED: " + response);
                }

            }
        };
        HeartbeatManager.sendHeartbeatRequest(getApplicationContext(), username, 2, TAG, callback);
    }

    private void printLog(String text) {
        Log.w(TAG, text);
        RuntimeLog.printHeartbeatLog(TAG, text);
    }
}
