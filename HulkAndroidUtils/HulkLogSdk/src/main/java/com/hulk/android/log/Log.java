package com.hulk.android.log;

/**
 * 日志打印工具类
 * <p> Log作为日志打印代理,方便替换原有代码
 * <p>运行日志打印:debug模式下打印w界别，便于调试分析问题，默认打印d级别
 * <p>根据是否debug模式判断打印级别,避免太多log.w导致运行缓慢
 * @author: zhanghao
 * @Time: 2020-01-08 16:29
 */
public class Log {
    /**
     * 运行日志打印:debug模式下打印w界别，便于调试分析问题，默认打印d级别
     * <p>根据是否debug模式判断打印级别,避免太多log.w导致运行缓慢
     * @param tag
     * @param msg
     */
    public static void d(String tag, String msg) {
        RuntimeLog.d(tag, msg);
    }

    /**
     * 打印日志,debug模式下改成w,并写到文件中
     * <p>根据是否debug模式判断打印级别,避免太多log.w导致运行缓慢
     * @param tag
     * @param msg
     */
    public static void i(String tag, String msg) {
        RuntimeLog.i(tag, msg);
    }

    public static void v(String tag, String msg) {
        RuntimeLog.v(tag, msg);
    }

    /**
     * 打印日志,并写到文件中
     * @param tag
     * @param msg
     */
    public static void w(String tag, String msg) {
        RuntimeLog.w(tag, msg);
    }

    /**
     * 打印日志,并写到文件中
     * @param tag
     * @param msg
     * @param e
     */
    public static void w(String tag, String msg, Throwable e) {
        RuntimeLog.w(tag, msg, e);
    }

    /**
     * 打印日志,并写到文件中
     * @param tag
     * @param msg
     */
    public static void e(String tag, String msg) {
        RuntimeLog.e(tag, msg, null);
    }

    public static void e(String tag, String msg, Throwable e) {
        RuntimeLog.e(tag, msg, e);
    }
}
