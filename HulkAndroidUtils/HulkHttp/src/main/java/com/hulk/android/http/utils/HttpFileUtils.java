package com.hulk.android.http.utils;

import android.content.res.Resources;
import android.util.Log;

import com.hulk.android.http.content.InputWriteCallback;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 文件工具类
 * @author: zhanghao
 * @Time: 2021-03-04 15:58
 */
public class HttpFileUtils {
    private static final String TAG = "HttpFileUtils";

    /**
     * 根据全路径文件名称取文件名
     *
     * @param fullFileName 文件路径/url
     * @return 文件名
     */
    public static String getFileName(String fullFileName) {
        try {
            return fullFileName.substring(fullFileName.lastIndexOf(File.separator) + 1);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
        return fullFileName;
    }
}
