
package com.daohelper.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

public class DaoUtils {

    private static final String TAG = "DaoUtils";

    /**
     * check path Exist
     * 
     * @param path
     * @return
     */
    public static boolean isExist(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        File f = new File(path);
        return isExist(f);
    }

    public static boolean isExist(File f) {
        if (f != null && f.exists()) {
            return true;
        } else {
            return false;
        }
    }

    public static String[] listFile(String path) {
        File f = new File(path);
        if(f.exists()) {
            return f.list();
        }
        return null;
    }

    public static File[] listFiles(String path) {
        File f = new File(path);
        if(f.exists()) {
            return f.listFiles();
        }
        return null;
    }

    public static boolean copyAssetsFile(Context context, String fileName, String destPath) {
        if(TextUtils.isEmpty(fileName) || TextUtils.isEmpty(destPath)) {
            return false;
        }
        try {
            File destFile = new File(destPath);
            if(!destFile.exists()) {
                copyFileFromAssets(context, fileName, destPath);
                Log.i(TAG, "copied Assets fileName: " + fileName + " TO " + destPath);
            } else {
                Log.w(TAG, "End copy existed file:" + fileName + " IN " + destPath);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * copy file to destination path from assets dir
     */
    public static void copyFileFromAssets(Context context, String fileName, String destFilePath) throws IOException {
        if (TextUtils.isEmpty(destFilePath) || TextUtils.isEmpty(fileName)) {
            Log.e(TAG, "destPath= " + destFilePath + ", fileName= " + fileName);
            return;
        }
        // if no the path, create it.
        File parentFile = new File(destFilePath).getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        InputStream input = context.getAssets().open(fileName);
        OutputStream output = new FileOutputStream(destFilePath);

        // read src path to dest path
        byte[] buffer = new byte[1024];
        int length;
        while ((length = input.read(buffer)) > 0) {
            output.write(buffer, 0, length);
        }

        // Close the streams
        output.flush();
        output.close();
        input.close();
    }
}
