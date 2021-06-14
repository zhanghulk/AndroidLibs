package com.hulk.byod.parser;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.hulk.byod.parser.xml.httpbody.HttpBodyBase;
import com.hulk.byod.parser.xml.httpbody.IHttpBody;
import com.hulk.byod.parser.net.HulkHttpCallback;
import com.hulk.util.common.NetUtils;
import com.hulk.byod.parser.entity.HulkHttpResponse;
import com.hulk.byod.parser.net.HulkHttpTask;
import com.hulk.byod.parser.xml.httpbody.HeartbeatResponseHttpBody;

/**
 * Created by zhanghao on 2017/11/24.
 */

public class HeartbeatManager {
    public static final String TAG = "HeartbeatManager";

    public static final String ACTION_HEARTBEAT = "com.hulk.byod.action.HEARTBEAT";
    public final static int HEARTBEAT_DEAULT_TIME = 300 * 1000; //心跳间隔时间300秒

    public interface HeartbeatCallback {
        void onFinish(HulkHttpResponse response);
    }

    static PendingIntent mPendingService = null;
    static HulkHttpTask sHeartbeatTask;

    static void resetHeartbeatTask() {
        if(sHeartbeatTask != null) {
            sHeartbeatTask.cancel(true);
        }
    }

    public static void sendHeartbeatRequest(Context context, String remark) {
        String username = "zhanghao";
        sendHeartbeatRequest(context, username, 2, remark, null);
    }

    /**
     * 心跳请求
     * @param context
     * @param username
     * @param onlineStatus 终端在线状态 1：终端网络在线（但用户未登录）2：用户在线（用户已登录）,通常为2
     * @param remark
     */
    public static void sendHeartbeatRequest(Context context, String username, int onlineStatus, String remark) {
        sendHeartbeatRequest(context, username, onlineStatus, remark, null);
    }

    /**
     * 请求服务器，发起心跳请求, 并处理返回结果
     * @param context
     * @param username
     * @param onlineStatus 终端在线状态 1：终端网络在线（但用户未登录）2：用户在线（用户已登录）,通常为2
     * @param remark
     * @param callback
     */
    public static void sendHeartbeatRequest(final Context context, String username, int onlineStatus, final String remark, final HeartbeatCallback callback) {
        resetHeartbeatTask();
        printLog(context, remark + " >> sendHeartbeatRequest... ");
        sHeartbeatTask = new HulkHttpTask(context, new HulkHttpCallback() {
            @Override
            public void onHttpResult(HulkHttpResponse response, Object obj) {
                if (response != null && response.isSuccess()) {
                    Log.i(TAG, "Heartbeat: " + response);
                    HeartbeatResponseHttpBody body = new HeartbeatResponseHttpBody();
                    HttpBodyBase.parseFrom(body, response.xmlText);
                    if (body != null && body.isSuccess()) {
                        String tag = remark + " >> Heartbeat SUCCESS";
                        long newInterval = body.getIntervalMillis();
                        long interval = newInterval != 0 ? newInterval : HEARTBEAT_DEAULT_TIME;
                        processHeartbeatInerval(context, interval, tag);
                        //TODO 上传成功之后清除日志，避免重复
                        HulkLogManager.getInstance().clear(tag);
                    } else {
                        printLog(context, "Heartbeat FAILED: " + body);
                        String tag = remark + " >> Heartbeat FAILED";
                        resetNextAsync(context, getFixedIntervalMillis(context), tag);
                    }
                } else {
                    printLog(context, "Heartbeat failed: " + response);
                }
                if (callback != null) {
                    callback.onFinish(response);
                }
                sHeartbeatTask = null;
            }
        });
        IHttpBody body = HulkManager.getHeartbeatRequestHttpBody(context, username, onlineStatus);
        sHeartbeatTask.execute(body);
    }

    private static void processHeartbeatInerval(Context context, long intervalMillis, String remark) {
        long old = getIntervalMillis(context);
        printLog(context, remark + " >> process new intervalMillis= " + intervalMillis + ", the old interval= " + old);
        setIntervalMillis(context, intervalMillis);
        if (intervalMillis != old) {
            //发生变化,重置定时任务,设置下一次的定是请求任务
            resetNextAsync(context, getFixedIntervalMillis(context), remark);
        } else {
            printLog(context, "The HeartbeatInerval is not changed: " + intervalMillis);
        }
    }

    public static void initHeartbeatAsync(Context context, String remark) {
        if (NetUtils.isConnected(context)) {
            sendHeartbeatRequest(context, remark);
        } else {
            resetNextAsync(context, getFixedIntervalMillis(context), remark);
        }
    }

    public static void resetNextAsync(final Context context, final long intervalMillis, final String remark) {
    	new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isJobSchedulerEnabled = HulkJobScheduler.isJobSchedulerEnabled();
                printLog(context, "reset next intervalMillis= " + intervalMillis
                        + ", JobScheduler Enabled= " + isJobSchedulerEnabled);
                if (isJobSchedulerEnabled) {
                    HulkJobScheduler.getInstance(context).resetHeartbeatJob(intervalMillis, remark);
                } else {
                    resetRepeatingAlarm(context, intervalMillis);
                }
            }
        }).start();
    }

    public static void resetRepeatingAlarm(Context context, long intervalMillis) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (mPendingService != null) {
            am.cancel(mPendingService);
        }
        //首次不需要触发，直接设置延迟时间为下一个时间
        long triggerAtMillis = System.currentTimeMillis() + intervalMillis;
        printLog(context, "Alarm setRepeating intervalMillis= " + intervalMillis);
        am.setRepeating(AlarmManager.RTC, triggerAtMillis, intervalMillis, getPendingIntent(context));
    }

    private static PendingIntent getPendingIntent(Context context) {
        if (mPendingService == null) {
            Intent intent = new Intent(context, HulkIntentService.class);
            mPendingService = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return mPendingService;
    }

    public static void printLog(Context context, String text) {
        RuntimeLog.printHeartbeatLog(TAG, text);
    }

    public static long getFixedIntervalMillis(Context context) {
        long local = getIntervalMillis(context);
        return local == 0 ? HEARTBEAT_DEAULT_TIME : local;
    }

    public static long getIntervalMillis(Context context) {
        return HulkDataHelper.getInstance(context).getHeartbeatIntervalMillis();
    }

    public static void setIntervalMillis(Context context, long intervalMillis) {
        HulkDataHelper.getInstance(context).setHeartbeatIntervalMillis(intervalMillis);
    }
}
