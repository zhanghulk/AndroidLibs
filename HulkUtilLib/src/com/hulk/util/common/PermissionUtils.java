package com.hulk.util.common;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

@SuppressLint("NewApi")
public class PermissionUtils {

	public static final int REQUEST_CODE = 101;
	public static final String[] PERMISSIONS = { Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE };
	/**
	 * 动态获取权限，Android 6.0 新特性，一些保护权限，除了要在AndroidManifest中声明权限，还要使用如下代码动态获取
	 * @param activity
	 * @return
	 */
	public static boolean requestPermissions(Activity activity) {
		//Android 6.0 新特性
		return requestPermissions(activity, REQUEST_CODE);
	}

	/**
	 * 动态获取权限，Android 6.0 新特性，一些保护权限，除了要在AndroidManifest中声明权限，还要使用如下代码动态获取
	 * @param activity
	 * @param requestCode
	 * @return
	 */
	public static boolean requestPermissions(Activity activity, int requestCode) {
		//Android 6.0 新特性
		if (Build.VERSION.SDK_INT >= 23) {
			// 验证是否许可权限
			for (String per : PERMISSIONS) {
				if (activity.checkSelfPermission(per) != PackageManager.PERMISSION_GRANTED) {
					// 申请权限
					activity.requestPermissions(PERMISSIONS, requestCode);
					return true;
				}
			}
		}
		return false;
	}
}
