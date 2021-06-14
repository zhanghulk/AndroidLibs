package com.hulk.android.log;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.hulk.util.common.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

//TODO 目前探索过得方法均无法导出，代码暂时放在这里都需研究
public class AnrUtil {

    public static final String TAG = "AnrUtil";

    public static final String LOGS_ANR_DIR = "hulk/log/anr/";
    public static final String ANR_RAW_DIR = "/data/anr/";//系统根目录的anr原始目录
    public static final String ANR_RAW_TRACES = "/data/anr/traces.txt";//traces.txt文件目录

    /**
     * ANR导出后的可见目录
     * @param context
     * @return
     */
    public static String getAnrLogDir(Context context){
        return RuntimeLog.decryptWrappedDir(context, LOGS_ANR_DIR);
    }

    /**
     * ANR文件原始目录
     * <p>经过封装标记修复过的，如果想会与原始系统的/data/anr/，请直接使用ANR_RAW_DIR常量.
     * @param context
     * @return
     */
    public static String getAnrRawDir(Context context){
        return RuntimeLog.decryptWrappedDir(context, ANR_RAW_DIR);
    }

    /**
     * 执行命令
     * <p>eg in adb shell:
     * <p>HWNXT:/data/anr $ cat traces.txt > ../../mnt/sdcard/tj/logs/tianji/traces.txt
     * <p>HWNXT:/data/anr $ cp traces.txt  ../../mnt/sdcard/tj/logs/
     * @param command
     * @return 命令执行结果的文本内容
     */
    public static String execCommand(String command) {
        try {
            Log.w(TAG, "execCommand: " + command);
            Process proc = Runtime.getRuntime().exec(command);
            String resultStr = readInputText(proc.getInputStream(), true);
            Log.w(TAG, "The result is follows:\n" + resultStr);
            return resultStr;
        } catch (Throwable e) {
            Log.e(TAG, "execCommand failed: " + e + ", cmd= " + command, e);
            return e.toString();
        }
    }

    /**
     * 读取输入流中的文本内容
     * @param input
     * @param close
     * @return 文本内容
     */
    public static String readInputText(InputStream input, boolean close) {
        try {
            InputStreamReader isr = new InputStreamReader(input);
            BufferedReader reader = new BufferedReader(isr, 1024);
            String line = null;
            StringBuffer buff = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                //Log.w(TAG, line);
                buff.append(line).append("\n");
            }
            if (buff.length() > 0) {
                buff.deleteCharAt((buff.length() - 1));
            }
            return buff.toString();
        } catch (Throwable e) {
            Log.e(TAG, "readInputText failed: " + e, e);
            return e.toString();
        } finally {
            if (input != null &&close) {
                try {
                    input.close();
                } catch (IOException e) {
                    //ignore
                }
            }
        }
    }

    /**
     * 通过cat命令,把源文件内容输出到目标文件
     * @param srcFilePath
     * @param destFilePath
     */
    public static String doCatCommand(String srcFilePath, String destFilePath) {
        String command = "cat " + srcFilePath + " > " + destFilePath;
        String resultStr = execCommand(command);
        return resultStr;
    }

    /**
     * 通过cat命令,把源文件内容输出到目标文件
     * @param srcFilePath
     * @param destFilePath
     */
    public static String doCPCommand(String srcFilePath, String destFilePath) {
        String command = "cp " + srcFilePath + " " + destFilePath;
        String resultStr = execCommand(command);
        return resultStr;
    }

    public static void exportAnrTraces(Context context) {
        String srcFilePath = ANR_RAW_TRACES;
        String destDir = LOGS_ANR_DIR;
        File dir = new File(destDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String destPath = destDir + "/traces_cat.txt";
        doCatCommand(srcFilePath, destPath);
        String destPath2 = destDir + "/traces_cp.txt";
        doCPCommand(srcFilePath, destPath2);
    }

    /**
     * 导出anr的traces到SD卡 /tj/logs/tianji/anr目录
     * @return
     */
    public static boolean exportAnrFiles(Context context) {
        String anrRawDir = getAnrRawDir(context);
        String destDir = getAnrRawDir(context);
        return exportAnrFiles(anrRawDir, destDir);
    }

    public static void exportAnrFilesAsync(final Context context) {
        RuntimeLog.sExecutor.execute(new Runnable() {
            @Override
            public void run() {
                exportAnrFiles(context);
            }
        });
    }

    /**
     * 导出anr文件到SD卡目录
     * <p> 考虑到某些设备ROM不是规矩的/data/anr/traces.txt,遍历anr下所有文件.
     * @param anrRawDir /data/anr/目录
     * @param destDir SD卡目标目录
     * @return
     */
    public static boolean exportAnrFiles(String anrRawDir, String destDir) {
        try {
            //考虑到某些设备ROM不是规矩的/data/anr/traces.txt,遍历anr下所有文件.
            //String anrDir = "/data/anr/";
            File dir = new File(anrRawDir);
            if (!dir.exists()) {
                Log.e(TAG, "exportAnrFiles: Not exists dir: " + dir);
                return false;
            }
            File[] list = dir.listFiles();
            if (list == null || list.length <= 0) {
                Log.e(TAG, "exportAnrFiles: file list is empty in dir " + dir);
                return false;
            }
            Log.w(TAG, "pullAnrTraces: Start abd pull to dir " + destDir);
            for (File file: list) {
                String path = file.getAbsolutePath();
                boolean result = copyFile(path, destDir);
                Log.w(TAG, "exportAnrFiles: " + path + ", result= " + result);
            }
            return true;
        } catch (Throwable e) {
            Log.e(TAG, "exportAnrFiles failed: " + e, e);
        }
        return false;
    }

    public static boolean copyFile(String srcFilePath, String destDir) {
        try {
            if (!TextUtils.isEmpty(srcFilePath)) {
                File srcFile = new File(srcFilePath);
                if (srcFile.exists()) {
                    srcFile.setWritable(true);
                    srcFile.setReadable(true);
                    String name = srcFile.getName();
                    File destFile = new File(destDir, name);
                    FileUtils.copyFile(srcFile, destFile);
                    return true;
                } else {
                    Log.w(TAG, "copyFile: Not existed file: " + srcFilePath);
                }
            }
        } catch (Throwable e) {
            Log.e(TAG, "copyFile failed: " + e + " >> " + srcFilePath + " to " + destDir, e);
        }
        return false;
    }
}
