package com.hulk.android.log;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import com.hulk.util.common.FileUtils;
import com.hulk.util.file.PrintLog;
import com.hulk.util.file.PrintLog.LogLevel;
import com.hulk.util.file.PrintUtil;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 建行运行时日志文件保存在SD卡/tj/logs/runtime/目录中,不加密,便于导出查看
 * <p>运行日志打印:debug模式下打印w界别，便于调试分析问题，默认打印d级别
 * <p>根据是否debug模式判断打印级别,避免太多log.w导致运行缓慢
 * Created by zhanghao on 18-1-17.
 */

public class RuntimeLog {

    public static final String LOG_FILE_LOCKED_FLAG = PrintLog.LOCKED_FLAG;

    /**
     * 打印日志配置文件
     */
    public static String RUNTIME_LOG_PREF_FILE = "runtime_log_pref_file";

    /**
     * 打印日志目录保存的key
     */
    public static String RUNTIME_LOG_ROOT_DIR_KEY = "runtime_logs_root_dir";

    private final static String TAG = "RuntimeLog";

    private static boolean WRITE_LOG_ENABLED = true;

    /**
     * 沙箱中不加秘密文件路径前缀,仅用于标记此文件不加密,不会下载文件路径中
     */
    public final static String NO_ENCRYPTED_FILE_PREFIX_FLAG = "/external_org_path";

    public static final String ROOT_DIR = Environment.getExternalStorageDirectory().getAbsolutePath();
    /**
     * 默认日志输出陌路
     */
    public static final String LOGS_ROOT_DIR = ROOT_DIR + "/Hulk/logs/";
    //子目录如下
    public static final String PROCESS_MAIN_PREFIX = "main";
    public static final String PROCESS_SPORTAL_VPN_SUFFIX = "sportal_vpn";
    public static final String PROCESS_ITS_SUFFIX = "its";
    public static final String SYS_LOGS_PREFIX = "sys";
    public static final String LOGS_MAIN_DIR = PROCESS_MAIN_PREFIX + "/";
    public static final String LOGS_SPORTAL_VPN_DIR = PROCESS_SPORTAL_VPN_SUFFIX + "/";
    public static final String LOGS_SYS_DIR = SYS_LOGS_PREFIX + "/";
    public static final String LOGS_ITS_SDK_DIR = PROCESS_ITS_SUFFIX + "/";

    /**
     * 沙箱日志打印子目录
     */
    public static final String WRAP_LOGS_PREFIX = "wrap";
    //源文件目录 eg: "/external_org_path/sdcard/tj/logs/tianji/wrap/net_log1.txt"
    public static final String LOGS_WRAP_ORG_DIR = WRAP_LOGS_PREFIX + "/";
    /**
     * 解密都的文件目录 eg: "/external_org_path/sdcard/log2.txt"
     */
    public static final String LOGS_WRAP_REPORT_DIR = "report_wrap/";

    public static final boolean BUFFER_MODE = true;
    public static final int BUFFER_LENGTH = 1024 * 4;
    public static final boolean PRINT_ALL_LOG = false;

    private static boolean sPrintAllLog = PRINT_ALL_LOG;
    /**
     * 是否为封装版本
     */
    private static boolean sTianjiWrapped = false;
    /**
     * 日志根目录
     */
    private static String mLogRootDir;

    private Context mContext;
    private String mProcessName;
    /**
     * 打印log工具类
     */
    private PrintLog mLog;

    /**
     * 缓冲区大小为10kb
     */
    public boolean mBufferMode = BUFFER_MODE;

    static ExecutorService sExecutor = Executors.newCachedThreadPool();

    private static class RuntimeLogHolder {
        private static RuntimeLog INSTANCE = new RuntimeLog();
    }

    public static RuntimeLog getInstance() {
        return RuntimeLogHolder.INSTANCE;
    }

    private RuntimeLog() {
        //to do
    }

    /**
     * 初始化运行时日志文件,在最上层模块中初始化传入编辑版本标识.
     * @param context
     */
    public void init(Context context) {
        String processName = getCurrentProcessName(context);
        init(context, processName, true, false);
    }

    /**
     * 初始化运行时日志文件,在最上层模块中初始化传入编辑版本标识.
     * @param context
     * @param processName
     */
    public void init(Context context, String processName) {
        init(context, processName, true, false);
    }

    /**
     * 初始化运行时日志文件,在最上层模块中初始化传入编辑版本标识.
     * @param processName 缓冲区模式(10kb)
     * @param bufferMode 缓冲区模式(10kb)
     * @param isTianjiWrapped 是否为沙箱封装
     */
    public void init(Context context, String processName, boolean bufferMode, boolean isTianjiWrapped) {
        mContext = context;
        mProcessName = processName;
        sTianjiWrapped = isTianjiWrapped;
        mLogRootDir = getPrefLogRootDir(context);
        mLog = getProcessLog();
        String mainProcessName = context.getPackageName();
        if (processName != null && !processName.endsWith(mainProcessName)) {
            //子进程没有界面控制，缓冲区1kb，避免日志打印不全
            setBufferMode(bufferMode, 1024);
        } else {
            mBufferMode = bufferMode;
            setBufferMode(mBufferMode, BUFFER_LENGTH);
        }
        Log.w(TAG, "init: processName= " + mProcessName
                + ", isTianjiWrapped= " + isTianjiWrapped + ", mLog= " + mLog);
    }

    /**
     * 设置缓冲区模式
     */
    public void setBufferMode(boolean bufferMode, int bufferLength) {
        mBufferMode = bufferMode;
        setLogBufferMode(bufferMode, bufferLength);
    }

    /**
     * 设置缓冲区模式
     */
    public void setBufferMode(boolean bufferMode) {
        setLogBufferMode(bufferMode, BUFFER_LENGTH);
    }

    public void setLogBufferMode(boolean bufferMode, int bufferLength) {
        if (mLog != null) {
            mLog.setBufferMode(bufferMode);
            mLog.setBufferLength(bufferLength);
        }
    }

    public static boolean isBufferMode() {
        return getInstance().mBufferMode;
    }

    /**
     * 创键主进程日志工具对象,默认log文件目录
     * @return
     */
    private PrintLog createTianjiLog() {
        //主进程放在主目录
        String mainDir = getMainLogDir(mContext);
        PrintLog log = createLog(mContext, mainDir);
        if (log != null) {
            log.setLogPrefix(PROCESS_MAIN_PREFIX);
        }
        return log;
    }

    /**
     * 创键默认log文件目录
     * <p>沙箱封装版本沙箱不加密文件,在文件路径前面加上NO_ENCRYPTED_FILE_PREFIX_FLAG标记
     * @return
     */
    private PrintLog createLog(Context context, String dir) {
        return createLog(context, dir, BUFFER_MODE, BUFFER_LENGTH);
    }

    public static PrintLog createLog(Context context, String dir, boolean bufferMode, int bufferLength) {
        PrintLog log = new PrintLog(dir, bufferMode, bufferLength);
        return log;
    }

    public static String getTianjiLogDir(Context context){
        //logFolderName为空便是放在日志目录的根目录下
        return decryptWrappedDir(context, "");
    }

    /**
     * 主进程日志目录
     * @param context
     * @return
     */
    public static String getMainLogDir(Context context){
        return decryptWrappedDir(context, LOGS_MAIN_DIR);
    }

    /**
     * 获取VPN日志log目录(包括VPN SDK的vpncore.log)
     * <p>沙箱封装后的文件是加密的，想要得到不加密文件,在文件路径前面加上NO_ENCRYPTED_FILE_PREFIX_FLAG标记
     * @param context
     * @return
     */
    public static String getVpnLogDir(Context context){
        return decryptWrappedDir(context, LOGS_SPORTAL_VPN_DIR);
    }

    public static String getItsSdkLogDir(Context context){
        return decryptWrappedDir(context, LOGS_ITS_SDK_DIR);
    }

    /**
     * 封装日志原文件目录(被加密的)
     * @param context
     * @return
     */
    public static String getWrapOrgLogDir(Context context){
        return decryptWrappedDir(context, LOGS_WRAP_ORG_DIR);
    }

    /**
     * 封装日志明文文件目录
     * @param context
     * @return
     */
    public static String getWrapReportLogDir(Context context){
        return decryptWrappedDir(context, LOGS_WRAP_REPORT_DIR);
    }

    /**
     * 系统日志目录
     * @param context
     * @return
     */
    public static String getSysLogDir(Context context){
        return decryptWrappedDir(context, LOGS_SYS_DIR);
    }

    /**
     * 解密沙箱封装文件目录
     * <p>沙箱封装后的文件是加密的，想要得到不加密文件,在文件路径前面加上NO_ENCRYPTED_FILE_PREFIX_FLAG标记
     * @param context
     * @param logFolderDir 日志文件夹名称, 如 main  or warp
     * @return
     */
    public static String decryptWrappedDir(Context context, String logFolderDir){
        //直接使用缓存的
        if (!sTianjiWrapped && context != null) {
            sTianjiWrapped = isTianjiWrapped(context);
        }
        //文件夹名称前面拼接根目录
        String rootDir = getPrefLogRootDir(context);
        String logDir;
        if (rootDir.endsWith(File.separator)) {
            logDir = rootDir + logFolderDir;
        } else {
            logDir = rootDir + "/" + logFolderDir;
        }
        if (!logDir.endsWith(File.separator)) {
            logDir = logDir + File.separator;
        }
        String dirPath = decryptWrappedDir(sTianjiWrapped, logDir);
        if (!isFileExists(dirPath)) {
            Log.w(TAG, "decryptWrappedDir: Not existed dirPath: " + dirPath);
            createDir(context, dirPath);
        } else {
            Log.w(TAG, "decryptWrappedDir: dirPath is " + dirPath);
        }
        return dirPath;
    }

    public static String decryptWrappedDir(boolean isTianjiWrapped, String logDir){
        String dirPath;
        if (isTianjiWrapped) {
            //封装的应用需要加上前缀标记,避免被加密混淆
            dirPath = NO_ENCRYPTED_FILE_PREFIX_FLAG + logDir;
        } else {
            dirPath = logDir;
        }
        return dirPath;
    }

    public static String getLogRootDir(Context context) {
        if (TextUtils.isEmpty(mLogRootDir)) {
            mLogRootDir = getPrefLogRootDir(context);
        }
        return mLogRootDir;
    }

    public PrintLog getProcessLog() {
        if (mProcessName == null || mProcessName.isEmpty()) {
            Log.w(TAG, "getProcessLog: return tian ji Log for invalid mProcessName is " + mProcessName);
            return createTianjiLog();
        }
        if (mProcessName.equals(mContext.getPackageName())) {
            Log.w(TAG, "getProcessLog main process: " + mProcessName);
            return createTianjiLog();
        }
        try {
            //子进程
            int begins = mProcessName.lastIndexOf(":") + 1;
            String subProcess = mProcessName.substring(begins);
            //子进程放在子目录中
            String subProcessDir = getTianjiLogDir(mContext) + "/" + subProcess + "/";
            PrintLog log = createLog(mContext, subProcessDir);
            Log.w(TAG, "The sub process: " + subProcess);
            if (log != null) {
                log.setLogPrefix(subProcess);
            } else {
                Log.e(TAG, "getProcessLog use tian ji Log for failed to create sub process: " + mProcessName);
                log = createTianjiLog();
            }
            return log;
        } catch (Exception e) {
            Log.e(TAG, "getProcessLog error: " + e, e);
        }
        return createTianjiLog();
    }

    public PrintLog getLog() {
        return mLog;
    }

    public static PrintLog getRuntimeLog() {
        return getInstance().getLog();
    }

    public boolean lockLogFile() {
        if (mLog != null) {
            return mLog.lockFile();
        }
        return false;
    }

    private static boolean isDebugMode() {
        return PrintLogUtils.isDebugMode();
    }

    public static void d(String tag, String text) {
        printLog(LogLevel.D, tag, text, null);
    }

    public static void v(String tag, String text) {
        printLog(LogLevel.V, tag, text, null);
    }

    public static void i(String tag, String text) {
        printLog(LogLevel.I, tag, text, null);
    }

    public static void w(String tag, String text) {
        printLog(LogLevel.W, tag, text, null);
    }

    public static void w(String tag, String text, Throwable e) {
        printLog(LogLevel.W, tag, text, e);
    }

    public static void e(String tag, String text, Throwable e) {
        printLog(LogLevel.E, tag, text, e);
    }

    public static void printLog(LogLevel level, String tag, String text, Throwable e) {
        if (TextUtils.isEmpty(tag)) {
            tag = TAG;
        }
        try {
            switch (level) {
                case D:
                    if (isDebugPrintMode()) {
                        //转化为w级别,否则release版本混淆后系统日志打印不出来
                        Log.w(tag, "" + text, e);
                        printToLogFile(level, tag, text, e);
                    } else {
                        Log.d(tag, "" + text, e);
                    }
                    break;
                case V:
                    if (isDebugPrintMode()) {
                        //转化为w级别,否则release版本混淆后系统日志打印不出来
                        Log.w(tag, "" + text, e);
                        printToLogFile(level, tag, text, e);
                    } else {
                        Log.v(tag, "" + text, e);
                    }
                    break;
                case I:
                    if (isDebugPrintMode()) {
                        //转化为w级别,否则release版本混淆后系统日志打印不出来
                        Log.w(tag, "" + text, e);
                        printToLogFile(level, tag, text, e);
                    } else {
                        Log.i(tag, "" + text, e);
                    }
                    break;
                case W:
                    Log.w(tag, "" + text, e);
                    printToLogFile(level, tag, text, e);
                    break;
                case E:
                    Log.e(tag, "" + text, e);
                    printToLogFile(level, tag, text, e);
                    break;
                default:
                    Log.w(TAG, "Unknown log Level: " + level);
                    break;
            }
        } catch (Throwable ex) {
            Log.e(tag, "print failed: " + ex, ex);
        }
    }

    /**
     * 谢日log文件
     * @param level
     * @param tag
     * @param text
     * @param e
     */
    public static void printToLogFile(LogLevel level, String tag, String text, Throwable e) {
        if (!WRITE_LOG_ENABLED) {
            Log.w(TAG, "printToLogFile: Disabled");
            return;
        }
        PrintLog printLog = getRuntimeLog();
        if (printLog == null) {
            Log.e(TAG, "printToLogFile: printLog is null");
            throw new RuntimeException("printToLogFile: The printLog is null. please init it at first.");
        }
        doPrint(printLog, level, tag, text, e);
    }

    public static void printLogAsync(final LogLevel level, final String tag, final String text, final Throwable e) {
        sExecutor.execute(new Runnable() {
            @Override
            public void run() {
                printToLogFile(level, tag, text, e);
            }
        });
    }

    /**
     * 写log信息到文件中
     * @param log
     * @param tag
     * @param text
     * @param e
     */
    public static boolean doPrint(PrintLog log, LogLevel level, String tag, String text, Throwable e){
        if (log == null) {
            Log.e(TAG, "doPrint: Failed to print log for log for PrintLog is null");
            return false;
        }
        try {

            String threadInfo =  + Process.myPid() + " " + Thread.currentThread().getId();
            boolean printed = log.printLog(level, tag, text, threadInfo, e);
            if (!printed) {
                Log.w(TAG, "doPrint: Failed to print Log: " + log);
            }
            return printed;
        } catch (Exception ex) {
            Log.e(TAG, "print error: " + ex, ex);
        }
        return false;
    }

    public static boolean flush(String remark){
        if (isBufferMode()) {
            Log.i(TAG, "flush: remark= " + remark);
            return flush(getInstance().getLog());
        } else {
            Log.w(TAG, "flush: Ignored for not buffer mode remark= " + remark);
        }
        return false;
    }

    public static void flushAsync(final String remark){
        sExecutor.execute(new Runnable() {
            @Override
            public void run() {
                flush(remark);
            }
        });
    }
    public static boolean flush(PrintLog log){
        if (log == null) {
            return false;
        }
        try {
            boolean flushed = log.flush();
            if (!flushed) {
                Log.w(TAG, "Failed to flush Log: " + log);
            }
            return flushed;
        } catch (Exception e) {
            Log.e(TAG, "flush error: " + e, e);
        }
        return false;
    }

    /**
     * 合并异常堆栈信息
     * 追加异常堆栈信息
     * @param text
     * @param e
     * @return
     */
    public static String mergeStackTrace(String text, Throwable e) {
        return PrintUtil.mergeStackTrace(text, e);
    }

    /**
     * s\设置是否打印全部调用此工具类的log待SD卡文件,默认false,只打印w,e两种类型.
     * <p>但是,某些情况时需要打印全部类的log到SD卡，便于分析问题: 登录流程中，解锁流程中
     * @param isPrintAllLog
     */
    public static void setPrintAllLog(boolean isPrintAllLog) {
        sPrintAllLog = isPrintAllLog;
        RuntimeLog instance = RuntimeLog.getInstance();
        boolean bufferMode = RuntimeLog.BUFFER_MODE;
        if (isPrintAllLog) {
            bufferMode = false;
        }
        instance.setBufferMode(bufferMode);
    }

    public static boolean isPrintAllLog() {
        return sPrintAllLog;
    }

    public static boolean isDebugPrintMode() {
        return isPrintAllLog() || isDebugMode();
    }

    /**
     * 清除日志文件夹
     * @param context
     */
    public static void clearTianjiLogDir(Context context, String remark) {
        try {
            String dir = getTianjiLogDir(context);
            Log.w(TAG, "clearTianjiLogsDir: dir" + dir + ", remark= " + remark);
            boolean cleared = clearDir(dir);
            Log.w(TAG, "clearTianjiLogsDir: cleared= " + cleared);
        } catch (Exception e) {
            Log.e(TAG, "clearTianjiLogDir failed: " + e, e);
        }
    }

    /**
     * 清空文件夹
     * @param dir
     */
    public static boolean clearDir(String dir) {
        try {
            if(!TextUtils.isEmpty(dir)) {
                File directory = new File(dir);
                Log.i(TAG, "clear dir: " + dir);
                FileUtils.deleteDirectory(directory);
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "clearDir: " + e, e);
        }
        return false;
    }

    /**
     * 修改打印输出日志总目录
     * @param logRootDir
     */
    public static void setPrefLogRootDir(Context context, String logRootDir) {
        SharedPreferences prefs = getPrefs(context);
        prefs.edit().putString(RUNTIME_LOG_ROOT_DIR_KEY, logRootDir).commit();
    }

    /**
     * 获取打印输出日志总目录
     */
    public static String getPrefLogRootDir(Context context) {
        SharedPreferences prefs = getPrefs(context);
        String dir = prefs.getString(RUNTIME_LOG_ROOT_DIR_KEY, "");
        if (TextUtils.isEmpty(dir)) {
            dir = LOGS_ROOT_DIR;
        }
        return dir;
    }

    /**
     * 获取打印输出日志总目录
     */
    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(RUNTIME_LOG_PREF_FILE, Context.MODE_MULTI_PROCESS);
    }

    private static boolean isTianjiWrapped(Context context) {
        return false;
    }

    /**
     * 获取当前进程名称
     * @param context
     * @return
     */
    public static String getCurrentProcessName(Context context) {
        int pid = Process.myPid();
        String myProcessName = getProcessName(context, pid);
        return myProcessName;
    }

    /**
     * 获取进程,名称
     * @param context
     * @param pid  进程id: Process.myPid()
     * @return
     */
    public static String getProcessName(Context context, int pid) {
        if (context == null) {
            Log.w(TAG, "getProcessName: context is null ");
            return "";
        }
        if (pid < 0) {
            Log.w(TAG, "getProcessName: invalid pid: " + pid);
            return "";
        }
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> ps = am.getRunningAppProcesses();
        if (ps != null) {
            for (ActivityManager.RunningAppProcessInfo p : ps) {
                if (pid == p.pid) {
                    String processName = p.processName;
                    Log.i(TAG, "getProcessName: " + processName + " for pid: " + pid);
                    return p.processName;
                }
            }
        }
        Log.w(TAG, "Get Process name failed for pid: " + pid);
        return "";
    }

    /**
     * 创建文件夹
     * <p> targetSdkVersion 为29及以上时,不能随意创键文件夹, 需要使用如下:
     * <p> 例如：Context.getExternalFilesDir(null)， Android/data/应用包名/files/
     * <p>这个路径是在SD卡的Android目录下的data目录下的自己应用包下的files目录下的 Environment.XXX，Environment.XXX代表的意思如下图
     * @param dirPath
     * @return 实际创键的文件目录, 对于targetSdkVersion 为29及以上时, 返回路径可能 Android/data/应用包名/files/$dirPath
     */
    public static String createDir(Context context, String dirPath) {
        File dir = new File(dirPath);
        boolean existed = dir.exists();
        boolean created = true;
        if (!existed) {
            created = dir.mkdirs();
        } else {
            Log.i(TAG, "createDir: existed dir " + dir);
        }
        Log.w(TAG, "createDir: " + dir + ", created: " + created);
        if (!created) {
            //尝试在 Android/data/应用包名/files/ 中创建
            Log.w(TAG, "createDir: First create failed, try to create dir in SD external file dir, sdk:  " + Build.VERSION.SDK_INT);
            String externalDir = createExternalFilesDir(context, dirPath);
            if (!TextUtils.isEmpty(externalDir)) {
                Log.w(TAG, "createDir: return external file dir: " + externalDir);
                return externalDir;
            }
        }
        if (!created) {
            Log.w(TAG, "createDir: Create failed, please check build gradle targetSdkVersion," +
                    " if it above 29 user can not free to create folder, must user ");
            return "";
        }
        return dirPath;
    }

    /**
     * 创建文件夹: Android/data/应用包名/files/*
     * <p> targetSdkVersion 为29及以上时,不能随意创键文件夹, 需要使用如下:
     * <p> 例如：Context.getExternalFilesDir(null)， Android/data/应用包名/files/
     * <p>这个路径是在SD卡的Android目录下的data目录下的自己应用包下的files目录下的 Environment.XXX，Environment.XXX代表的意思如下图
     * @param dirPath
     * @return 实际创键的文件目录, 如果创建失败,返回空,. 对于targetSdkVersion 为29及以上时, 返回路径可能 Android/data/应用包名/files/$dirPath
     */
    public static String createExternalFilesDir(Context context, String dirPath) {
        if (TextUtils.isEmpty(dirPath)) {
            Log.w(TAG, "createExternalFilesDir: dirPath is empty");
            return "";
        }
        if (context == null) {
            Log.w(TAG, "createExternalFilesDir: context is null");
            return "";
        }
        File externalFilesDir = context.getExternalFilesDir(null);
        if (externalFilesDir != null) {
            String fixPath;
            String pkg = context.getPackageName();
            if (dirPath.contains(pkg)) {
                //去掉路径中多余的包名,externalFilesDir 中已经含包名,避免重复
                fixPath = dirPath.replace(pkg, "");
                fixPath = fixPath.replace("//", "/");
            } else {
                fixPath = dirPath;
            }
            String path = externalFilesDir.getAbsolutePath() + "/" + fixPath;
            File externalFileDir = new File(path);
            boolean created = true;
            if (!externalFileDir.exists()) {
                created = externalFileDir.mkdirs();
                Log.w(TAG, "createExternalFilesDir: " + externalFileDir + ", created: " + created);
            }
            if (created) {
                Log.w(TAG, "createExternalFilesDir: return external file dir: " + externalFileDir);
                return path;
            }
        } else {
            Log.w(TAG, "createExternalFilesDir: Got External Files Dir is null");
        }
        return "";
    }

    public static boolean isFileExists(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        File dir = new File(path);
        return dir.exists();
    }

    /**
     * 初始化日志打印工具类
     * <p>精简当前进程日志目录下的文件树龄,避免越积越多
     * @param context
     * @param rootDir
     * @param bufferMode
     * @return 返回 PrintLog 对象
     */
    public static PrintLog initProcessLog(Context context, String rootDir, boolean bufferMode) {
        Log.w(TAG, "initProcessLog: rootDir=" + rootDir + ", bufferMode="  + bufferMode);
        if (!TextUtils.isEmpty(rootDir)) {
            setPrefLogRootDir(context, rootDir);
        }
        String processName = getCurrentProcessName(context);
        boolean isWrapped = isTianjiWrapped(context);
        Log.w(TAG, "initProcessLog: processName=" + processName + ", isWrapped="  + isWrapped);
        RuntimeLog runtimeLog = RuntimeLog.getInstance();
        runtimeLog.init(context, processName, bufferMode, isWrapped);
        PrintLog log = runtimeLog.getLog();
        return log;
    }

    /**
     * 初始化日志输出目录
     * <p>每个应用修改成自己的日志输出根目录:
     * @param context
     * @param logRootDir 日志根目录, eg: /storage/emulated/0/ZeroTrustSDK/logs/
     * @return PrintLog 对象, 包括创键进程日志目录等等信息
     */
    public static PrintLog initRuntimeLog(Context context, String logRootDir) {
        if (TextUtils.isEmpty(logRootDir)) {
            Log.w(TAG, "initRuntimeLog: rootDir is empty.");
            return null;
        }
        Log.i(TAG, "initRuntimeLog: root dir is " + logRootDir);
        PrintLog log = RuntimeLog.initProcessLog(context, logRootDir, BUFFER_MODE);
        Log.w(TAG, "initRuntimeLog: " + log);
        return log;
    }

    /**
     * 初始化日志输出目录
     * <p>每个应用修改成自己的日志输出根目录:
     * @param context
     * @param rootDir 日志根目录, eg: /storage/emulated/0/ZeroTrustSDK/logs/
     * @return 已创键进程日志目录, 如: /storage/emulated/0/ZeroTrustSDK/logs/com.mobile.emm.zerotrust.demo/
     */
    public static String initRuntimeLogDir(Context context, String rootDir) {
        PrintLog log = initRuntimeLog(context, rootDir);
        return log != null ? log.getDirPath() : "";
    }

    /**
     * 初始化日志打印工具类
     * <p>精简当前进程日志目录下的文件树龄,避免越积越多
     * @param context
     * @param logRootDir
     */
    public static PrintLog initProcessLog(Context context, String logRootDir) {
        return initRuntimeLog(context, logRootDir);
    }

    /**
     * 初始化日志打印工具类
     * <p>精简当前进程日志目录下的文件树龄,避免越积越多
     * @param context
     */
    public static PrintLog initProcessLog(Context context) {
        return initRuntimeLog(context, RuntimeLog.LOGS_ROOT_DIR);
    }
}
