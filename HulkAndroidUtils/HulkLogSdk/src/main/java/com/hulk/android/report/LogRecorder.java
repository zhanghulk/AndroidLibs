package com.hulk.android.report;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.hulk.util.common.FileUtils;
import com.hulk.android.log.RuntimeLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class LogRecorder {

    public static final String TAG = "LogRecorder";

    //默认最长日志最长记录时间（更久的话循环删除）
    public static final long DEFAULT_MAX_LOG_MINUTES = 24*60;

    //文件大小限制，适当大一点免得日志信息断断续续,太大也不方便查看
    public static final int SYS_LOGS_FILE_LENGTH_LIMIT = 1024 * 1024 * 2;

    public static final int LOG_LEVEL_NO_SET = 0;

    public static final int LOG_BUFFER_MAIN = 1;
    public static final int LOG_BUFFER_SYSTEM = 1 << 1;
    public static final int LOG_BUFFER_RADIO = 1 << 2;
    public static final int LOG_BUFFER_EVENTS = 1 << 3;
    public static final int LOG_BUFFER_KERNEL = 1 << 4;

    //默认支持main和system，其他的再考虑
    public static final int LOG_BUFFER_DEFAULT = LOG_BUFFER_MAIN | LOG_BUFFER_SYSTEM;

    public static final int INVALID_PID = -1;

    public String mFilePrefix;                             //文件前缀
    public String mFolderPath;                             //路径
    public int mFileSizeLimitation = SYS_LOGS_FILE_LENGTH_LIMIT;                        //文件大小
    public int mFileMaxCount = 0;                        //文件数量限制
    public int mLevel;                                     //输出日志级别
    public List<String> mFilterTags = new ArrayList<>();   //tags
    public List<String> mGrepWords = new ArrayList<>();    //grep关键字
    public int mPID = INVALID_PID;                         //pid

    public boolean mUseLogcatFileOut = false;

    private static boolean isRecording = false;             //是否正在记录日志
    private static boolean sStopped = false;             //是否停止记录日志

    private LogDumper mLogDumper = null;

    public static final int EVENT_RESTART_LOG = 1001;

    private Context mContext;
    private Callback mCallback;

    private RestartHandler mHandler;

    private static class RestartHandler extends Handler {
        final LogRecorder logRecorder;
        public RestartHandler(LogRecorder logRecorder) {
            this.logRecorder = logRecorder;
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == EVENT_RESTART_LOG) {
                logRecorder.stop();
                logRecorder.start();
            }
        }
    }

    //回调
    public interface Callback{
        void onChangeRecordingState(boolean isRecording);
        void onLogRecordStopped(Context context, File file);
    }

    public LogRecorder(Context context) {
        mContext = context;
        mHandler = new RestartHandler(this);
    }

    public void setFileMaxCount(int fileMaxCount) {
        this.mFileMaxCount = fileMaxCount;
    }

    public void start() {
        File file = new File(mFolderPath);
        if (!file.exists()) {
            file.mkdirs();
        }

        setStopped(false);

        //日志收集指令：logcat -v time -s tag:level *:level | grep pid
        //可指定多个tag、level和进程
        //todo 是否有必要指定缓冲区radio(通信系统)、system（系统组件）、event（event模块）、main（java层），不指定情况下默认是system和main.
        String cmdStr = collectLogcatCommand();
        Log.w(TAG, "start: cmdStr="+ cmdStr + ", mFolderPath=" + mFolderPath + ", mFileMaxCount=" + mFileMaxCount);
        if (mLogDumper != null) {
            mLogDumper.stopDumping();
            mLogDumper = null;
        }

        mLogDumper = new LogDumper(mFolderPath, mFilePrefix, mFileSizeLimitation, mFileMaxCount, cmdStr, mHandler);
        mLogDumper.start();

        //delete 多余文件
        if (mFileMaxCount > 0) {
            Log.w(TAG, "start: Delete surplus file");
            deleteSurplusFilesAsync(mFolderPath, mFileMaxCount);
        }
    }

    /**
     * 异步删除多余旧文件
     * @param dir
     * @param maxCount
     */
    public void deleteSurplusFilesAsync(final String dir, final int maxCount) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //deleteSurplusFiles(dir, maxCount);
                int deletedCount = FileUtils.deleteOldFiles(new File(dir), maxCount, RuntimeLog.LOG_FILE_LOCKED_FLAG);
                Log.w(TAG, "run: deletedCount= " + deletedCount);
            }
        }).start();
    }

    /**
     * 删除多余旧文件
     * @param dir
     * @param maxCount
     * @deprecated
     */
    public void deleteSurplusFiles(String dir, int maxCount) {
        if (TextUtils.isEmpty(dir)) {
            Log.w(TAG, "deleteSurplusFiles: Ignored for dir is empty");
            return;
        }
        if (maxCount <= 0) {
            Log.w(TAG, "deleteSurplusFiles: Ignored for invalid maxCount: " + maxCount);
            return;
        }
        File parentDir = new File(dir);
        if (!parentDir.exists()) {
            Log.w(TAG, "deleteSurplusFiles: Ignored for Not exists dir: " + dir);
            return;
        }
        File[] files = parentDir.listFiles();
        if (files == null) {
            Log.w(TAG, "deleteSurplusFiles: Ignored for invalid list files is nul");
            return;
        }
        if (files.length < maxCount) {
            Log.w(TAG, "deleteSurplusFiles: Ignored for files.length= ; " + files.length + ", logFileMaxCount= " + maxCount);
            return;
        }
        Log.i(TAG, "logFileMaxCount= " + maxCount + "， files.length= " + files.length);
        //升序排列
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (o1.lastModified() > o2.lastModified()) {
                    return 1;//交换位置
                } else if (o1.lastModified() < o2.lastModified()) {
                    return -1;//不交换位置
                }
                return 0;
            }
        });
        if (files.length > maxCount) {
            //从前面开始删除,需要删除的个数为:files.length - maxCount
            int delCount = files.length - maxCount;
            Log.w(TAG, "deleteSurplusFiles: delCount= " + delCount);
            for (int i = 0; i < delCount; i++) {
                File file = files[i];
                if (file != null && file.exists()) {
                    boolean deleted = file.delete();
                    Log.w(TAG, "Delete file: " + file + ", deleted= " + deleted);
                }
            }
        }
    }

    public void stop() {
        if (mLogDumper != null) {
            mLogDumper.stopDumping();
            mLogDumper = null;
        }
        setIsRecording(false);
    }

    public void setDeleteLogFileEnabled(boolean deleteLogFileEnabled) {
        if (mLogDumper != null) {
            mLogDumper.setDeleteLogFileEnabled(deleteLogFileEnabled);
        }
    }

    public void setLockFileEnabled(boolean lockFileEnabled) {
        if (mLogDumper != null) {
            mLogDumper.setLockFileEnabled(lockFileEnabled);
        }
    }

    private String collectLogcatCommand() {
        StringBuilder stringBuilder = new StringBuilder();
        final String SPACE = " ";
        stringBuilder.append("logcat");
        //todo 考虑-b信息 指定buffer（radio、system、event、main）
        stringBuilder.append(SPACE);
        //暂定输出格式为time，timeThread也可以考虑
        stringBuilder.append("-v time");

        //增加tag和level过滤
        String levelStr = getLevelStr();

        if (!mFilterTags.isEmpty()) {
            stringBuilder.append(SPACE);
            stringBuilder.append("-s");
            for (int i = 0; i < mFilterTags.size(); i++) {
                String tag = mFilterTags.get(i) + ":" + levelStr;
                stringBuilder.append(SPACE);
                stringBuilder.append(tag);
            }
        } else {
            if (!TextUtils.isEmpty(levelStr)) {
                stringBuilder.append(SPACE);
                stringBuilder.append("*:" + levelStr);
            }
        }

        //todo 可以通过logcat -f file &来指定输出目录; 通过-f来指定文件和文件大小， &是否后台运行（慎重，因为如果不停掉，会一直打下去）
        //todo pid的指定可以通过-p或者--pid来指定，可以考虑
        String pidStr = "";
        if (mPID != INVALID_PID) {
            pidStr = adjustPIDStr();
        }

        //grep关键字规则 grep "a|b|c",关键字或关系。暂时不考虑与关系
        if((!TextUtils.isEmpty(pidStr)) || (mGrepWords!=null && mGrepWords.size()!= 0)){
            stringBuilder.append(SPACE).append("|").append(SPACE).append("grep").append(SPACE);
            //过滤pid
            if(!TextUtils.isEmpty(pidStr)){
                stringBuilder.append("(" + pidStr + ")");
            }
            //过滤关键字
            for (int i = 0; i<mGrepWords.size();i++){
                if(i != 0 || (!TextUtils.isEmpty(pidStr))){
                    //如果为0，则之前必须有过pid，才增加"|"
                    stringBuilder.append("|");
                }
                stringBuilder.append(mGrepWords.get(i));
            }
        }

        return stringBuilder.toString();
    }

    //根据数字切换日志输出级别
    private String getLevelStr() {
        switch (mLevel) {
            case 2:
                return "V";
            case 3:
                return "D";
            case 4:
                return "I";
            case 5:
                return "W";
            case 6:
                return "E";
            case 7:
                return "F";
        }

        return "V";
    }

    //pid
    private String adjustPIDStr() {
        if (mPID == INVALID_PID) {
            return null;
        }

        String pidStr = String.valueOf(mPID);
        int length = pidStr.length();
        if (length < 4) {
            pidStr = " 0" + pidStr;
        }

        if (length == 4) {
            pidStr = " " + pidStr;
        }

        return pidStr;
    }

    /**
     * 创建日期时间文件名,txt文件(方便打开), eg: prefix_yyyy-MM-dd-HH-mm-SS.txt
     * @param prefix
     * @return
     */
    public static String createLogFilename(String prefix) {
        String date = new SimpleDateFormat("yyyy-MM-dd-HH-mm-SS")
                .format(new Date(System.currentTimeMillis()));
        String fileName = (TextUtils.isEmpty(prefix)) ? date : (prefix + "_" + date + ".txt");
        return fileName;
    }

    //日志采集线程,是否有必要考虑线程池，防止多线程同时采集
    private class LogDumper extends Thread {
        final String logPath;
        final String logFilePrefix;
        final int logFileLimitation;
        final String logCmd;
        final int logFileMaxCount;

        final RestartHandler restartHandler;

        private Process logcatProc;
        private BufferedReader mReader = null;
        private FileOutputStream out = null;

        private boolean mRunning = true;
        private boolean mDeleteLogFileEnabled = false;//停止打印log时,是否删除打印的log文件
        private boolean mLockFileEnabled = false;//停止打印log时,是否锁定log文件
        final private Object mRunningLock = new Object();

        private long currentFileSize;
        private File mLogFile;

        public LogDumper(String folderPath, String prefix,
                         int fileSizeLimitation, int mFileMaxCount, String command,
                         RestartHandler handler) {
            logPath = folderPath;
            logFilePrefix = prefix;
            logFileLimitation = fileSizeLimitation;
            logFileMaxCount = mFileMaxCount;
            logCmd = command;
            restartHandler = handler;

            //文件名后缀
            String fileName = createLogFilename(logFilePrefix);
            Log.w(TAG, "--- fileName:" + fileName);
            File parentDir = new File(logPath);
            try {
                if (!parentDir.exists()) {
                    boolean mkdir = parentDir.mkdirs();
                    if (mkdir) {
                        Log.w(TAG, "LogDumper: make parent failed" + parentDir);
                        return;
                    }
                    Log.w(TAG, "mk parent dir： " + parentDir);
                }

                mLogFile = new File(parentDir, fileName);
                out = new FileOutputStream(mLogFile);
                Log.w(TAG, "LogDumper: mLogFile= " + mLogFile + ", out= " + out);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Create file error: " + e + ", parentDir= " + parentDir + ", mLogFile= " + mLogFile, e);
                e.printStackTrace();
            }
        }

        public void stopDumping() {
            synchronized (mRunningLock) {
                mRunning = false;
            }
        }

        public void setDeleteLogFileEnabled(boolean deleteLogFileEnabled) {
            this.mDeleteLogFileEnabled = deleteLogFileEnabled;
        }

        public void setLockFileEnabled(boolean lockFileEnabled) {
            this.mLockFileEnabled = lockFileEnabled;
        }

        @Override
        public void run() {
            try {
                setIsRecording(true);
                logcatProc = Runtime.getRuntime().exec(logCmd);
                mReader = new BufferedReader(new InputStreamReader(
                        logcatProc.getInputStream()), 1024);
                String line = null;
                while (mRunning && (line = mReader.readLine()) != null) {
                    if (!mRunning) {
                        Log.w(TAG, "run:  break for mRunning is false");
                        break;
                    }
                    if (isStopped()) {
                        Log.w(TAG, "run:  break for stopped by user.");
                        setIsRecording(false);
                        break;
                    }
                    if (line.length() == 0) {
                        continue;
                    }
                    if (out != null && !line.isEmpty()) {
                        byte[] data = (line + "\n").getBytes();
                        out.write(data);
                        if (logFileLimitation != 0) {
	                        currentFileSize += data.length;
	                        if (logFileLimitation > 0) {
                                if (currentFileSize > logFileLimitation) {
                                    Log.w(TAG, "run: break for currentFileSize= " + currentFileSize + ", logFileLimitation= " + logFileLimitation + "Kb");
                                    restartHandler.sendEmptyMessage(EVENT_RESTART_LOG);
                                    break;
                                }
                            }
                        }
                    }
                }
                //每次获取日志后清除日志，防止logcat缓冲区堆积，下个日志文件重复写日志
                String clearLogCmd = "logcat -c";
                Runtime.getRuntime().exec(clearLogCmd);
                Log.w(TAG, "run: exec clearLogCmd= " + clearLogCmd + ", mDeleteLogFileEnabled= " + mDeleteLogFileEnabled);
                if (mLockFileEnabled) {
                    String lockPath = mLogFile.getParent() + "/"+ RuntimeLog.LOG_FILE_LOCKED_FLAG + "_" + mLogFile.getName();
                    File file = new File(lockPath);
                    mLogFile.renameTo(file);
                    Log.w(TAG, "rename mLogFile: " + mLogFile + " to  " + lockPath);
                } else if (mDeleteLogFileEnabled) {
                    //todo 每个日志记录完成后，检查是否有特超过最大记录周期（默认24h）的，有的话删掉，防止文件过大；
                    if (mLogFile != null && mLogFile.exists()) {
                        boolean deleted = mLogFile.delete();
                        Log.w(TAG, "Delete mLogFile: " + mLogFile + ", deleted= " + deleted);
                    }
                }
                if (mCallback != null) {
                    mCallback.onLogRecordStopped(mContext, mLogFile);
                }
            } catch (Exception e) {
                Log.e(TAG, "LogDumper run error: " + e, e);
                e.printStackTrace();
            } finally {
                if (logcatProc != null) {
                    logcatProc.destroy();
                    logcatProc = null;
                }
                if (mReader != null) {
                    try {
                        mReader.close();
                        mReader = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    out = null;
                }
            }
        }
    }

    public static class Builder {

        private Context mContext;

        /**
         * 日志路径 /sdcard/$mLogFolderName
         * 默认/sdcard/$ApplicationName
         */
        private String mLogFolderName;

        /**
         * 全路径
         */
        private String mLogFolderPath;

        /**
         * 文件后缀
         */
        private String mLogFileNamePrefix = "";

        /**
         * 单日志文件大小（kb）
         */
        private int mLogFileSizeLimitation = SYS_LOGS_FILE_LENGTH_LIMIT;

        private int mLogFileMaxCount = 0;

        /**
         * 日志输出级别2~7
         * 分别对应V、D、I、W、E、F, 默认V
         */
        private int mLogLevel = LogRecorder.LOG_LEVEL_NO_SET;

        /**
         * 过滤tags
         */
        private List<String> mLogFilterTags = new ArrayList<>();

        /**
         * pid
         * 可以输入自己的pidandroid.os.Process.myPid()
         * android4.1之后，只有root后的设备应用才可以拿到设备的所有log，非root的只能取到自己的log
         */
        private int mPID = LogRecorder.INVALID_PID;

        /**
         * buffer
         * 默认 -b main -b system. 如果有crash，默认还会增加-b crash
         * todo 可以考虑增加radio和events
         * todo 5.1以后还有crash（很有用，考虑增加）、default（main + system）、all，考虑增加
         */
        private int mLogBuffersSelected = LogRecorder.LOG_BUFFER_DEFAULT;

        /**
         * 日志输出模式。time timeThread long raw等
         */
        private int mLogOutFormat;

        /**
         * 指定打印日志超时时间（单位s）
         * 默认打印5min的日志，防止一直打印，占据存储
         */
        private long outTime = 300;

        /**
         * 日志grep过滤关键字，注意与tag区分开来
         */
        private List<String> grepWords = new ArrayList<>();

        /**
         * 回调，用于更新界面等
         */
        private Callback callback;

        /**
         * 文件夹名
         */
        public Builder setLogFolderName(String logFolderName) {
            this.mLogFolderName = logFolderName;
            return this;
        }

        /**
         * 设置路径
         */
        public Builder setLogFolderPath(String logFolderPath) {
            this.mLogFolderPath = logFolderPath;
            return this;
        }

        /**
         * 设置前缀
         */
        public Builder setLogFileNamePrefix(String logFileNamePrefix) {
            this.mLogFileNamePrefix = logFileNamePrefix;
            return this;
        }

        /**
         * 设置单个日志文件大小
         */
        public Builder setLogFileSizeLimitation(int fileSizeLimitation) {
            this.mLogFileSizeLimitation = fileSizeLimitation;
            return this;
        }

        public Builder setLogFileMaxCount(int logFileMaxCount) {
            this.mLogFileMaxCount = logFileMaxCount;
            return this;
        }

        /**
         * 设置日志级别 2~7
         * 分别对应V、D、I、W、E、F, 默认V
         */
        public Builder setLogLevel(int logLevel) {
            this.mLogLevel = logLevel;
            return this;
        }

        /**
         * 添加tag，可指定多个
         */
        public Builder addLogFilterTag(String tag) {
            if(!TextUtils.isEmpty(tag)){
                mLogFilterTags.add(tag);
            }
            return this;
        }

        /**
         * 指定pid
         * 注意4.1以上非root设备只能不能读取除自己外其他应用的log，所以其实这个有没有用待定
         * 可以考虑使用-p或者--pid
         */
        public Builder setPID(int mPID) {
            this.mPID = mPID;
            return this;
        }

        /**
         * 指定buffer
         */
        public Builder setLogBufferSelected(int logBuffersSelected) {
            this.mLogBuffersSelected = logBuffersSelected;
            return this;
        }

        /**
         * 设置日志输出格式
         */
        public Builder setLogOutFormat(int logOutFormat) {
            this.mLogOutFormat = mLogOutFormat;
            return this;
        }

        /**
         * 添加过滤关键字
         */
        public Builder addGrepWord(String word){
            if(!TextUtils.isEmpty(word)){
                this.grepWords.add(word);
            }
            return this;
        }

        /**
         * 添加回调
         */
        public Builder addCallback(Callback callback){
            this.callback = callback;
            return this;
        }

        public Builder(Context context) {
            mContext = context;
        }


        private void applyAppNameAsOutfolderName() {
            try {
                String appName = mContext.getPackageName();
                String versionName = mContext.getPackageManager().getPackageInfo(
                        appName, 0).versionName;
                int versionCode = mContext.getPackageManager()
                        .getPackageInfo(appName, 0).versionCode;
                mLogFolderName = appName + "-" + versionName + "-" + versionCode;
                mLogFolderPath = applyOutfolderPath();
            } catch (Exception e) {
            }
        }

        private String applyOutfolderPath() {
            String outPath = "";
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                outPath = Environment.getExternalStorageDirectory()
                        .getAbsolutePath() + File.separator + mLogFolderName;
            }

            return outPath;
        }

        public LogRecorder build() {
            LogRecorder logRecorder = new LogRecorder(mContext);

            if (TextUtils.isEmpty(mLogFolderName)
                    && TextUtils.isEmpty(mLogFolderPath)) {
                applyAppNameAsOutfolderName();
            }

            if (TextUtils.isEmpty(mLogFolderPath)) {
                mLogFolderPath = applyOutfolderPath();
            }

            logRecorder.mFolderPath = mLogFolderPath;
            logRecorder.mFilePrefix = mLogFileNamePrefix;
            logRecorder.mFileSizeLimitation = mLogFileSizeLimitation;
            logRecorder.mFileMaxCount = mLogFileMaxCount;
            logRecorder.mLevel = mLogLevel;
            if (!mLogFilterTags.isEmpty()) {
                for (int i = 0; i < mLogFilterTags.size(); i++) {
                    logRecorder.mFilterTags.add(mLogFilterTags.get(i));
                }
            }
            if(!grepWords.isEmpty()){
                for (String s: grepWords){
                    logRecorder.mGrepWords.add(s);
                }
            }
            logRecorder.mPID = mPID;
            logRecorder.mCallback = callback;
            return logRecorder;
        }
    }

    //是否正在记录日志
    public static boolean isRecording() {
        return isRecording;
    }

    private void setIsRecording(boolean isRecording) {
        LogRecorder.isRecording = isRecording;
        if(mCallback != null){
            mCallback.onChangeRecordingState(isRecording);
        }
    }

    //是否已停止记录日志
    public static boolean isStopped() {
        return sStopped;
    }

    public static void setStopped(boolean stopped) {
        LogRecorder.sStopped = stopped;
    }

    @Override
    public String toString() {
        return "LogRecorder{" +
                "mFilePrefix='" + mFilePrefix + '\'' +
                ", mFolderPath='" + mFolderPath + '\'' +
                ", mFileSizeLimitation=" + mFileSizeLimitation +
                ", mFileMaxCount=" + mFileMaxCount +
                ", mLevel=" + mLevel +
                ", mFilterTags=" + mFilterTags +
                ", mGrepWords=" + mGrepWords +
                ", mPID=" + mPID +
                ", mUseLogcatFileOut=" + mUseLogcatFileOut +
                ", mLogDumper=" + mLogDumper +
                ", mCallback=" + mCallback +
                ", mHandler=" + mHandler +
                '}';
    }
}