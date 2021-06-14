package com.hulk.android.log;

import java.io.File;
import java.io.FileFilter;

/**
 * 列出文件过滤器，列出给定大小文件
 */
public class ListFileFilter implements FileFilter {
    public static final String TAG = "ListFileFilter";
    /**
     * 取大文件时最小限制
     */
    private long largeFileMinLength = 0;
    /**
     * 取小文件时最大限制
     */
    private long smallFileMaxLength = 0;

    private ListFileFilter(long largeFileMinLength, long smallFileMaxLength) {
        this.largeFileMinLength = largeFileMinLength;
        this.smallFileMaxLength = smallFileMaxLength;
    }

    @Override
    public boolean accept(File pathname) {
        if (pathname.isDirectory()) {
            //文件夹过滤掉
            return false;
        }
        long length = pathname.length();
        if (largeFileMinLength > 0) {
            return length >= largeFileMinLength;
        }
        if (smallFileMaxLength > 0) {
            return length >= smallFileMaxLength;
        }
        return true;
    }

    public static ListFileFilter getLargeFileFilter(long minLength) {
        return new ListFileFilter(minLength, 0);
    }

    public static ListFileFilter getSmallFileFilter(long maxLength) {
        return new ListFileFilter(0, maxLength);
    }
}
