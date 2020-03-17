package com.hulk.byod.ccb;

import android.text.TextUtils;
import android.util.Log;

import com.hulk.util.file.PrintLog;

/**
 * Created by zhanghao on 18-1-17.
 */

public class RuntimeLog {

    private final static String TAG = "CCBPrintLog";

    private static boolean WRITE_LOG_ENABLED = true;
    static PrintLog sPrinttHeartbeatLog = new PrintLog("/logs/runtime/heartbeat_logs/");
    static PrintLog sPrintJobLog = new PrintLog("/logs/runtime/job_logs/");

    public static void printHeartbeatLog(String text) {
        printHeartbeatLog(TAG, text);
    }

    public static void printHeartbeatLog(String tag, String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        if (TAG.equals(tag)) {
            Log.w(tag, "" + text);
        } else {
            Log.w(tag, TAG + ".printLog >> " + text);
        }
        if (WRITE_LOG_ENABLED) {
            sPrinttHeartbeatLog.printLog(tag, text + "\n");
        }
    }

    public static void printJobLog(String text) {
        printJobLog(TAG, text);
    }

    public static void printJobLog(String tag, String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        if (TAG.equals(tag)) {
            Log.w(tag, "" + text);
        } else {
            Log.w(tag, TAG + ".printLog >> " + text);
        }
        if (WRITE_LOG_ENABLED) {
            sPrintJobLog.printLog(tag, text + "\n");
        }
    }
}
