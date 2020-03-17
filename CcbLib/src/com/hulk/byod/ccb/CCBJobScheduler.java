package com.hulk.byod.ccb;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

/**
 * CCB定时任务管理器
 * refer to: http://blog.csdn.net/bboyfeiyu/article/details/44809395
 *
 * Created by zhanghao on 2017/11/22.
 */

public class CCBJobScheduler {

    private static final String TAG = "CCBJobScheduler";
    private static final int CCB_HEARTBEAT_JOB_ID = 100;// 心跳任务ID

    Context mContext;
    //5.x以下不支持JobScheduler
    private boolean mEnabled = false;

    ComponentName mServieComponent = null;
    JobScheduler mScheduler;
    JobInfo.Builder mHeartbeatJobBuilder;

    private static CCBJobScheduler instance;

    public static CCBJobScheduler getInstance(Context context) {
        if (instance == null) {
            synchronized (CCBJobScheduler.class) {
                if (instance == null) {
                    instance = new CCBJobScheduler(context);
                }
            }
        }
        return instance;
    }

    private CCBJobScheduler(Context context) {
        mContext = context;
        //5.x以下不支持JobScheduler
        mEnabled = isJobSchedulerEnabled();
        if (mEnabled) {
            mServieComponent = new ComponentName(context.getPackageName(), CCBJobService.class.getName());
            mScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            initJobBuilder();
        }
    }

    private void initJobBuilder() {
        if (mEnabled) {
            mHeartbeatJobBuilder = new JobInfo.Builder(CCB_HEARTBEAT_JOB_ID, mServieComponent);
            mHeartbeatJobBuilder.setPersisted(true);//reboot device will be continue task
            mHeartbeatJobBuilder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);//有网络时执行
        }
    }

    /**
     * 5.x以下不支持JobScheduler
     * @return
     */
    public static boolean isJobSchedulerEnabled() {
        return Build.VERSION.SDK_INT > 21;
    }

    /**
     * 小曲小调之前的定时任务,重新设置任务周期
     * @param intervalMillis 任务周期
     * @param remark
     * @return
     */
    public int resetHeartbeatJob(long intervalMillis, String remark) {
        if (mEnabled) {
            printLog(remark + " >> resetHeartbeatJob= " + intervalMillis);
            mScheduler.cancel(CCB_HEARTBEAT_JOB_ID);
            return setHeartbeatPeriodic(intervalMillis, remark);
        }
        return 0;
    }

    /**
     * 启动定时，设置定时周期, eg.
     * <p>1. 建行心跳连接服务器任务
     * @param intervalMillis the intervalMillis to repeat time
     * @param remark
     * @return
     */
    private int setHeartbeatPeriodic(long intervalMillis, String remark) {
        if (mEnabled) {
            mHeartbeatJobBuilder.setPeriodic(intervalMillis);
            int resultCode = mScheduler.schedule(mHeartbeatJobBuilder.build());
            boolean success = resultCode == JobScheduler.RESULT_SUCCESS;
            printLog(remark + " >> setHeartbeat periodic= " + intervalMillis
                    + (success? " SUCCESS" : " FAILURE code=" + resultCode));
            return resultCode;
        }
        return 0;
    }

    public void printLog(String text) {
        printLog(TAG, text);
    }

    public void printLog(String tag, String text) {
        RuntimeLog.printJobLog(tag, text);
    }
}
