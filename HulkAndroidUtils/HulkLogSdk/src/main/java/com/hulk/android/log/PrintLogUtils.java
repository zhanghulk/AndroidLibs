package com.hulk.android.log;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.hulk.util.file.PrintLog;
import com.hulk.android.report.LogFileManager;

/**
 * debug控制工具类:debug状态控制，日志打印控制
 */
public class PrintLogUtils {

    public static final String DEBUG_PREF_NAME = "debug_prefs";
    public static final String SHARED_LAUNCH_SWITCHER_STATUS = "debug_switcher_status";
    public static final String DEFAULT_DEBUG_PASSWORD = "*#HulkLog#*";
    private static final String TAG = "DebugUtils";

    /**
     * debug标记:-1表示关闭,1表示打开 (缓存状态,避免每次都从文件获取,频繁调用效率太低)
     */
    private static int sDebugFlag = 0;
    private static Context sContext;

    public static void setDebugMode(Context context, boolean debugMode) {
        //更新缓存标记
        sDebugFlag = encodeFlag(debugMode);
        SharedPreferences pref = context.getSharedPreferences(DEBUG_PREF_NAME, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor edit = pref.edit();
        edit.putBoolean(SHARED_LAUNCH_SWITCHER_STATUS, debugMode);
        edit.commit();
    }

    public static boolean isDebugMode(Context context) {
        SharedPreferences pref = context.getSharedPreferences(DEBUG_PREF_NAME, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
        return pref.getBoolean(SHARED_LAUNCH_SWITCHER_STATUS,false);
    }

    public static String getDeviceModel(){
        return android.os.Build.MODEL;
    }

    /**
     * debug标记:-1表示关闭,1表示打开
     * @param status
     * @return
     */
    private static int encodeFlag(boolean status) {
        return status ? 1 : -1;
    }

    /**
     * Debug是否打开(某些关键日志打印开关)
     * @return
     */
    public static boolean isDebugMode() {
        if (sContext == null) {
            return false;
        }
        if (sDebugFlag == 0) {
            //首次调用时初始化,默认为0
            boolean status = isDebugMode(sContext);
            sDebugFlag = encodeFlag(status);
        }
        return sDebugFlag == 1;
    }



    /**
     * 初始化debug日志工具
     * @param context
     * @param bufferMode 缓冲区模式(10kb)
     * @param isTianjiWrapped 是否沙箱封装
     */
    public static void init(Context context, String processName, boolean bufferMode, boolean isTianjiWrapped) {
        sContext = context;
        RuntimeLog.getInstance().init(context, processName, bufferMode, isTianjiWrapped);
        PrintLog log = RuntimeLog.getInstance().getLog();
        if (log != null) {
            String outputDir = log.getDirPath();
            //精简当前进程日志目录下的文件树龄,避免越积越多
            Log.i(TAG, "init: simplify log files async in output dir: " + outputDir);
            LogFileManager.simplifyDirFilesAsync(outputDir);
        } else {
            Log.w(TAG, "init: PrintLog is null.");
        }
    }

    /**
     * 初始化当前进程日志工具
     * @param context
     */
    public static void initProcessLog(Context context) {
        sContext = context;
        String rootDir = RuntimeLog.LOGS_ROOT_DIR;
        initProcessLog(context, rootDir);
    }

    /**
     * 初始化当前进程日志工具
     * @param context
     */
    public static void initProcessLog(Context context, String rootDir) {
        PrintLog log = RuntimeLog.initProcessLog(context, rootDir);
        if (log != null) {
            String outputDir = log.getDirPath();
            //精简当前进程日志目录下的文件树龄,避免越积越多
            Log.i(TAG, "initProcessLog: simplify log files async in output dir: " + outputDir);
            LogFileManager.simplifyDirFilesAsync(outputDir);
        } else {
            Log.w(TAG, "initProcessLog: PrintLog is null.");
        }
    }
}
