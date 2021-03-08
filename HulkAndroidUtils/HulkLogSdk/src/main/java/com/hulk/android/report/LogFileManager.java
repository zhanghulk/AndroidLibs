package com.hulk.android.report;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.hulk.android.log.ListFileFilter;
import com.hulk.android.log.LogUtil;
import com.hulk.android.log.RuntimeLog;
import com.hulk.util.common.FileUtils;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 日志文件管理器
 * @author: zhanghao
 * @Time: 2021-02-01 18:58
 */
public class LogFileManager {

    private static final String TAG = "LogFileManager";

    public static final int FILE_LENGTH_M = 1024 * 1024;
    public static final long LARGE_FILE_MIN_LENGTH = FILE_LENGTH_M * 10;
    public static final int REPORT_DIR_FILE_MAX_COUNT = 10;

    static ExecutorService sExecutor = Executors.newCachedThreadPool();

    /**
     * 获取要上报的文件目录
     * @param context
     * @return
     */
    public static String[] getReportDirs(Context context) {
        String rootDir = RuntimeLog.getMainLogDir(context);
        String vpnDir = RuntimeLog.getVpnLogDir(context);
        String itsDir = RuntimeLog.getItsSdkLogDir(context);
        String sysDir = RuntimeLog.getSysLogDir(context);
        //上传任务中进行解码到临时目录中再上传
        String wrapDir = RuntimeLog.getWrapReportLogDir(context);
        String[] dirs = new String[]{rootDir, vpnDir, wrapDir, sysDir, itsDir};
        return dirs;
    }

    /**
     * 清空天机日志目录
     * @param context
     */
    public static void clearTianjiLogDir(Context context, String remark) {
        LogUtil.w(TAG, "clearTianjiLogDir: remark= " + remark);
        //String tianjiDir = RuntimeLog.getTianjiLogDir(context);
        //boolean cleared = clearDir(tianjiDir);
        String[] reportDirs = getReportDirs(context);
        if (reportDirs != null) {
            for (String dir: reportDirs) {
                boolean cleared = clearDir(dir);
                LogUtil.w(TAG, dir + " has been cleared= " + cleared);
            }
        }
    }

    public static boolean clearDir(String dir) {
        try {
            if (!TextUtils.isEmpty(dir)) {
                File file = new File(dir);
                if (file.exists()) {
                    FileUtils.deleteDirectory(file);
                    LogUtil.w(TAG, "cleared dir: " + dir);
                    return true;
                }
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "clearDir failed: " + e + ", dir: " + dir, e);
        }
        return false;
    }

    /**
     * 清空天机日志目录
     * @param context
     */
    public static void clearRuntimeLogsAsync(final Context context, final String remark) {
        sExecutor.execute(new Runnable() {
            @Override
            public void run() {
                clearTianjiLogDir(context, remark);
            }
        });
    }

    /**
     * 简化天机日志目录中老旧文件
     * @param context
     */
    public static void simplifyLogFilesAsync(final Context context, final String remark) {
        sExecutor.execute(new Runnable() {
            @Override
            public void run() {
                simplifyLogFiles(context, remark);
            }
        });
    }

    public static void simplifyLogFiles(final Context context, final String remark) {
        try {
            LogUtil.w(TAG, "simplifyLogFiles: remark= " + remark);
            String[] reportDirs = getReportDirs(context);
            if (reportDirs != null) {
                for (String dir: reportDirs) {
                    //先删除大文件，再删除老旧文件
                    simplifyDirFiles(dir);
                }
            }
        } catch (Throwable e) {
            LogUtil.e(TAG, "simplifyLogFiles failed: " + e);
        }
    }

    /**
     * 简化日志打印文件，防止大文件和过多文件占用空间
     * <p> 先删除大文件，再删除老旧文件
     * @param dir
     * @param largeFileMinLength  大文件的下限
     * @param retainOldFileMaxCount 需要保留的文件最大数量
     */
    public static int simplifyDirFiles(String dir, long largeFileMinLength, int retainOldFileMaxCount) {
        LogUtil.w(TAG, "simplifyDirFiles: dir=" + dir + ", largeFileMinLength=" + largeFileMinLength + ", retainOldFileMaxCount=" + retainOldFileMaxCount);
        //先删除大文件，再删除老旧文件
        int deleteLargeCount = 0;
        if (largeFileMinLength > 0) {
            deleteLargeCount = deleteLargeLogFiles(dir, largeFileMinLength);
        } else {
            LogUtil.w(TAG, "simplifyDirFiles: invalid largeFileMinLength is " + largeFileMinLength);
        }
        int deleteOldCount = 0;
        if (retainOldFileMaxCount > 0) {
            deleteOldCount = FileUtils.deleteOldFiles(dir, retainOldFileMaxCount, null);
        } else {
            LogUtil.w(TAG, "simplifyDirFiles: invalid oldFileMaxRetainCount is " + largeFileMinLength);
        }
        if (deleteLargeCount > 0 || deleteOldCount > 0) {
            LogUtil.w(TAG, "Deleted old file Count is " + deleteOldCount + " and large log file count is " + deleteLargeCount + " in dir " + dir);
        }
        return deleteLargeCount + deleteOldCount;
    }

    /**
     * 简化日志打印文件，防止大文件和过多文件占用空间
     * <p> 先删除大文件，再删除老旧文件
     * @param dir
     * @return
     */
    public static int simplifyDirFiles(String dir) {
        Log.w(TAG, "simplifyDirFiles: dir is " + dir);
        return simplifyDirFiles(dir, LARGE_FILE_MIN_LENGTH, REPORT_DIR_FILE_MAX_COUNT);
    }

    /**
     * 简化日志打印文件，防止大文件和过多文件占用空间
     * <p> 先删除大文件，再删除老旧文件
     * @param dir
     */
    public static void simplifyDirFilesAsync(final String dir) {
        sExecutor.execute(new Runnable() {
            @Override
            public void run() {
                simplifyDirFiles(dir);
            }
        });
    }

    public static int deleteLargeLogFiles(String dir, long minLength) {
        if (TextUtils.isEmpty(dir)) {
            return 0;
        }
        File[] files = null;
        try {
            File dirFIle = new File(dir);
            files = dirFIle.listFiles(ListFileFilter.getLargeFileFilter(minLength));
        } catch (Throwable e) {
            LogUtil.e(TAG, "deleteLargeLogFiles failed: " + e + ", dir= " + dir);
        }
        if (files == null || files.length == 0) {
            LogUtil.i(TAG, "deleteLargeLogFiles: files is empty.");
            return 0;
        }
        int count = 0;
        for (File file: files) {
            try {
                boolean deleted = FileUtils.delete(file);
                if (deleted) {
                    count++;
                }
            } catch (Throwable e) {
                LogUtil.e(TAG, "deleteLargeLogFiles failed: " + e + ", file: " + file);
            }
        }
        return count;
    }
}
