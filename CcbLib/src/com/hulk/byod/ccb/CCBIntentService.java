package com.hulk.byod.ccb;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.hulk.byod.ccb.entity.CCBHttpResponse;

/**
 * 5.0一下的系统时用 CCBIntentService
 * Created by zhanghao on 18-1-12.
 */

public class CCBIntentService extends IntentService {

    private static final String TAG = "CCBIntentService";

    public CCBIntentService() {
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
            public void onFinish(CCBHttpResponse response) {
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
