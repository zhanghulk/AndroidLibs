package com.hulk.byod.ccb;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.hulk.util.file.TxtFile;
import com.hulk.byod.ccb.entity.ActivityJsonArray;
import com.hulk.byod.ccb.entity.ActivityLog;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 上报至建行服务端的活动日志保存到txt里面, 这是一个独立的日志文件
 * Created by zhanghao on 2017/11/24.
 */

public class CCBLogManager {

    private final static String TAG = "CCBLogManager";

    //需要上报至建行服务端的活动日志保存到txt里面
    private final static String LOGS_FILE_PATH = "/logs/report_logs/ccb_logs.txt";

    //活动类型: 1表示违规事件, 默认0
    private final static String ACT_TYPE_UNSPECIAL = "0";
    private final static String ACT_TYPE_VIOLATION = "1";

    TxtFile mTxtFile;
    private static CCBLogManager instance;

    public static CCBLogManager getInstance() {
        if (instance == null) {
            synchronized (CCBLogManager.class) {
                if (instance == null) {
                    instance = new CCBLogManager();
                }
            }
        }
        return instance;
    }

    private CCBLogManager() {
        String sdDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        mTxtFile = new TxtFile(sdDir + LOGS_FILE_PATH);
    }

    public boolean addLog(String logJson) {
        if (TextUtils.isEmpty(logJson)) {
            return false;
        }
        return write(logJson, true);
    }

    public boolean addLog(ActivityLog log) {
        if (log == null) {
            return false;
        }
        return write(log.toJson(), true);
    }

    public boolean writeLogList(List<ActivityLog> logList) {
        return writeLogList(logList, true);
    }

    public boolean addJsonArray(ActivityJsonArray array) {
        return wirteJsonArray(array, true);
    }

    public boolean wirteJsonArray(ActivityJsonArray array, boolean append) {
        if (array == null) {
            return false;
        }
        List<String> jsonList = array.getJsonList();
        return write(jsonList, append);
    }

    public boolean writeLogList(List<ActivityLog> logList, boolean append) {
        if (logList == null || logList.isEmpty()) {
            return false;
        }
        StringBuffer buffer = new StringBuffer();
        for (ActivityLog log: logList) {
            String json = log.toJson();
            if (!TextUtils.isEmpty(json)) {
                buffer.append(json).append('\n'); //new line
            }
        }
        return write(buffer.toString(), append);
    }

    public boolean write(List<String> list, boolean append){
        if (list == null || list.isEmpty()) {
            Log.w(TAG, "write canceled, list is null or empty !! ");
            return false;
        }
        StringBuffer buffer = new StringBuffer();
        for (String line: list) {
            if (!TextUtils.isEmpty(line)) {
                buffer.append(line).append('\n');//new line
            }
        }
        mTxtFile.writeLines(list, append);
        return write(buffer.toString(), append);
    }

    public boolean clear(String remark) {
        boolean cleared = mTxtFile.clear();
        Log.w(TAG, remark + " >> clear activity logs, cleared= " + cleared);
        return cleared;
    }

    /**
     * write text into file, per line is a activity log
     * @param value
     * @param append whether append line depending primary text or not.
     */
    public boolean write(String value, boolean append){
        if (append && TextUtils.isEmpty(value)) {
            Log.w(TAG, "write canceled, can not append a empty text !!");
            return false;
        }
        Log.i(TAG, "write log append= " + append + " >> text value:\n" + value);
        return mTxtFile.write(value, append);
    }

    /**
     * read all logs
     * @return  log list
     */
    public List<String> read() {
        return mTxtFile.readLines();
    }

    public ActivityJsonArray readArray() {
        ActivityJsonArray array = new ActivityJsonArray();
        List<String> list = read();
        if (list == null || list.isEmpty()) {
            return array;
        }
        for (String logJson: list) {
            if (TextUtils.isEmpty(logJson)) {
                Log.w(TAG, "readArray ignored for logJson is null or Empty !! ");
                continue;
            }
            Log.v(TAG, "read log json: " + logJson);
            ActivityLog log = ActivityLog.fromJson(logJson, ActivityLog.class);
            if (log != null) {
                array.add(log);
            } else {
                Log.w(TAG, "Failed to parse ActivityLog json: " + logJson);
            }
        }
        return array;
    }

    public String readJsonArray() {
        ActivityJsonArray array = readArray();
        return array != null ? array.toJson() : "[]";
    }

    public static String getTimeText(long time) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(time);
    }

    public static ActivityLog create(String act_name, String act_type, String act_desc) {
        ActivityLog log = new ActivityLog(act_name, act_type, act_desc);
        log.ACT_DATE = getTimeText(System.currentTimeMillis());
        return log;
    }

    public static ActivityLog create(String act_name, String act_desc) {
        return create(act_name, act_desc, ACT_TYPE_UNSPECIAL);
    }

    public static ActivityLog createViolationLog(String act_name, String act_desc) {
        return create(act_name, act_desc, ACT_TYPE_VIOLATION);
    }

    public static void addActLog(String actName, String actDesc) {
        addActLog(actName, ACT_TYPE_UNSPECIAL, actDesc);
    }

    public static void addActLog(String actName, String act_type, String actDesc) {
        ActivityLog log = CCBLogManager.create(actName, act_type, actDesc);
        boolean added = CCBLogManager.getInstance().addLog(log);
        if (added) {
            Log.i(TAG, "Success add ActLog: " + log);
        } else {
            Log.w(TAG, "Failed add ActLog: " + log);
        }
    }
}
