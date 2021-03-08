package com.hulk.android.report;

import android.content.Context;

import com.hulk.android.log.LogUtil;
import com.hulk.android.log.RuntimeLog;

import java.io.File;

public class SysLogPrinter {
    public static final String TAG = "SysLogPrinter";
    public static final boolean PRINT_ENABLED = true;
    /**
     * //sys目录下文件数量限制
     */
    public static final int SYS_LOGS_FILE_MAX_COUNT = 50;

    private static class LogManagerHolder {
        private static SysLogPrinter INSTANCE = new SysLogPrinter();
    }

    private LogRecorder mLogRecorder;

    LogRecorder.Callback mDefaultCallback = new LogRecorder.Callback() {
        @Override
        public void onChangeRecordingState(boolean isRecording) {
            LogUtil.w(TAG, "onChangeRecordingState: isRecording= " + isRecording);
            if(isRecording){
                //日志记录中，只显示停止按键
                //handler.sendEmptyMessage(ACTION_START_LOG_RECORD);
            }else {
                //未记录日志，只显示开始按键
                //handler.sendEmptyMessage(ACTION_STOP_LOG_RECORD);
            }
        }

        @Override
        public void onLogRecordStopped(Context context, File file) {
            if (context != null) {
                LogUtil.w(TAG, "onLogRecordStopped: " + file);
                //打印完毕立即压缩日志,便于用户手动导出
                ReportLogUtils.exportLogFiles(context, false, TAG);
            }
        }
    };

    public static SysLogPrinter getInstance() {
        return LogManagerHolder.INSTANCE;
    }

    private SysLogPrinter() {
    }

    public static LogRecorder createLogRecorder(Context context, String logFolderPath, LogRecorder.Callback callback) {
        try {
            //文件路径需要仔细考虑考虑,过滤（tag、关键字、pid、级别），指定（文件路径、前缀、单个文件大小、时长）
            return new LogRecorder.Builder(context)
                    .setLogFolderName(RuntimeLog.SYS_LOGS_PREFIX)
                    .setLogFolderPath(logFolderPath)
                    .setLogFileNamePrefix(RuntimeLog.SYS_LOGS_PREFIX)
                    .setLogFileSizeLimitation(LogRecorder.SYS_LOGS_FILE_LENGTH_LIMIT)
                    .setLogFileMaxCount(SYS_LOGS_FILE_MAX_COUNT)
                    .setLogLevel(2)
                    .addCallback(callback)
                    .build();
        } catch (Throwable e) {
            LogUtil.e(TAG, "createLogRecorder failed: " +e, e);
        }
        return null;
    }

    public void initLogRecorder(Context context) {
        if (!PRINT_ENABLED) {
            return;
        }
        try {
            if(mLogRecorder == null){
                String dir = RuntimeLog.getSysLogDir(context);
                LogUtil.w(TAG, "initLogRecorder: dir= " + dir);
                mLogRecorder = createLogRecorder(context,dir , mDefaultCallback);
            } else {
                LogUtil.w(TAG, "initLogRecorder: available recorder: " + mLogRecorder);
            }
        } catch (Throwable e) {
            LogUtil.e(TAG, "initLogRecorder failed: " +e, e);
        }
    }

    public void startRecordLog(Context context, String reamrk) {
        if (!PRINT_ENABLED) {
            return;
        }
        try {
            RuntimeLog.setPrintAllLog(true);
            initLogRecorder(context);
            if (mLogRecorder != null) {
                LogUtil.w(TAG, "startRecordLog: " + mLogRecorder + ", reamrk= " + reamrk);
                mLogRecorder.start();
            } else {
                LogUtil.w(TAG, "startRecordLog: log recorder is null");
            }
        } catch (Throwable e) {
            LogUtil.e(TAG, "startRecordLog failed: " +e, e);
        }
    }

    /**
     * 停止打印系统日志
     * @param lockFile 是否锁定 通常出现问题是锁定该日志文件
     * @param remark
     */
    public void stopRecordLog(boolean lockFile, String remark) {
        stopRecordLog(lockFile, false, remark);
    }

    /**
     * 停止打印系统日志
     * @param lockFile 是否锁定 通常出现问题是锁定该日志文件
     * @param deleteLog 是否删除此日志文件
     * @param remark
     */
    public void stopRecordLog(boolean lockFile, boolean deleteLog, String remark) {
        if (!PRINT_ENABLED) {
            return;
        }
        LogUtil.w(TAG, "stopRecordLog: lockFile=" + lockFile + ", deleteLog=" + deleteLog + ", remark=" + remark);
        try {
            RuntimeLog.flushAsync(remark);
            if (lockFile) {
                RuntimeLog.getInstance().lockLogFile();
            }
            if (mLogRecorder != null) {
                mLogRecorder.setDeleteLogFileEnabled(deleteLog);
                mLogRecorder.setLockFileEnabled(lockFile);
                mLogRecorder.stop();
                LogUtil.w(TAG, "stopRecordLog: " + mLogRecorder);
            } else {
                LogUtil.w(TAG, "stopRecordLog: mLogRecorder is null");
            }
        } catch (Throwable e) {
            LogUtil.e(TAG, "stopRecordLog failed: " +e, e);
        }
    }
}
