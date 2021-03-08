package com.hulk.android.report;

import java.io.File;
import java.io.FilenameFilter;

public class LogFilenameFilter implements FilenameFilter {
    @Override
    public boolean accept(File dir, String name) {
        File file = new File(dir, name);
        if (file.isDirectory()) {
            return false;//文件夹过滤掉
        }
        return true;
    }
}
