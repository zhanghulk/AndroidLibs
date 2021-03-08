package com.hulk.android.report;

import com.hulk.android.log.LogUtil;

import java.io.File;
import java.io.FileFilter;

public class LogFileFilter implements FileFilter {
    public static final String TAG = "LogFileFilter";
    @Override
    public boolean accept(File pathname) {
        if (pathname.isDirectory()) {
            return false;//文件夹过滤掉
        }
        long length = pathname.length();
        if (length > ReportLogUtils.MAX_REPORT_FILE_LENGTH) {
            //文件过大的不上报，避免崩溃,文件过大base64直接崩溃
            LogUtil.w(TAG, "accept: Ignored exceeding length file: " + pathname + ", length= " + length);
            return false;
        }
        return true;
    }
}
