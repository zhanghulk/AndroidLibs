package com.hulk.android.http.content;

import android.util.Log;

import com.hulk.android.http.download.DownloadListener;
import com.hulk.android.http.download.FileDownloadImpl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 文件帮助类
 * @author: zhanghao
 * @Time: 2021-03-05 16:42
 */
public class FileHelper {
    private static final String TAG = "FileHelper";

    /**
     * 将输入流写入文件
     * <p>也可以在此函数中监听进度
     * @param input
     * @param filePath
     * @param callback
     * @throws IOException
     */
    public static void writeFile(InputStream input, String filePath, InputWriteCallback callback) throws IOException {
        Log.i(TAG, "writeFile: filePath=" + filePath);
        File file = new File(filePath);

        if (!file.getParentFile().exists()) {
            boolean mkdirs = file.getParentFile().mkdirs();
            Log.w(TAG, "writeFile: Parent dir mkdirs: " + mkdirs);
        }

        if (file.exists()) {
            boolean deleted = file.delete();
            Log.w(TAG, "writeFile: old file deleted " + deleted);
        }
        //file.createNewFile();

        int code = -1;
        String msg = "";
        Throwable error = null;

        FileOutputStream fos = null;
        long writtenLength = 0;
        try {
            fos = new FileOutputStream(file);
            byte[] buffer = new byte[1024 * 4];
            int byteRead = 0;
            while ((byteRead = input.read(buffer)) != -1) {
                fos.write(buffer, 0, byteRead);
                writtenLength += byteRead;
                //更新进度
                if (callback != null) {
                    callback.onInputWriteProgress(writtenLength);
                }
            }
            fos.flush();
            code = 0;
            msg = "Finished";
            Log.i(TAG, "writeFile: Finished filePath: " + filePath);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "writeFile: FileNotFoundException ", e);
            error = e;
            throw e;
        } catch (IOException e) {
            Log.e(TAG, "writeFile: IOException", e);
            error = e;
            throw e;
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                //ignored
            }
            if (callback != null) {
                callback.onInputWriteFinished(code, msg, error);
            }
        }
    }

    /**
     * 将输入流写入文件
     * @param input
     * @param filePath
     * @throws IOException
     */
    public static void writeFile(InputStream input, final String filePath) throws IOException {
        writeFile(input, filePath, null);
    }

    /**
     * 将输入流写入文件
     * @param input
     * @param filePath
     * @param callback
     * @throws IOException
     */
    public static void writeDownloadFile(InputStream input, String filePath, FileDownloadImpl callback) throws IOException {
        writeFile(input, filePath, callback);
    }
}
