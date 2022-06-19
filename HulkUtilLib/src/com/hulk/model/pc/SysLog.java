package com.hulk.model.pc;

/**
 * 系统日志打印工具类
 * @author zhanghao
 *
 */
public class SysLog {

	public static void i(String tag, String msg) {
		System.out.println(tag + ": " + msg);
	}
	
	public static void e(String tag, String msg) {
		e(tag, msg, null);
	}
	
	public static void e(String tag, String msg, Throwable e) {
		if(e != null) {
			System.err.println(tag + ": " + msg + ", e=" + e);
			e.printStackTrace();
		} else {
			System.err.println(tag + ": " + msg);
		}
	}
}
