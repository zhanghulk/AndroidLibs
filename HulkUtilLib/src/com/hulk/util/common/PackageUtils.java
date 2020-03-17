package com.hulk.util.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;

public class PackageUtils {
	
	public static final String SHAREPREFERENCE_NAME = "prefs";
	
	public static PackageInfo getPackageInfoByPkgName(Context context, String pkgName) {
		PackageInfo appInfo = null;
		if (!TextUtils.isEmpty(pkgName)) {
			try {
				appInfo = context.getPackageManager().getPackageInfo(pkgName, 0);
			} catch (NameNotFoundException e) {
			}
		}
		return appInfo;
	}
	
	public static SharedPreferences getPreferences(Context context) {
    	SharedPreferences pref = context.getSharedPreferences(
                SHAREPREFERENCE_NAME, Context.MODE_PRIVATE |Context.MODE_MULTI_PROCESS);
		return pref;
	}
	
	public static boolean setString(Context context, String key ,String val) {
    	SharedPreferences pref = getPreferences(context);
        return pref.edit().putString(key, val).commit();
	}
	
	public static String getString(Context context, String key ,String defValue) {
    	SharedPreferences pref = getPreferences(context);
        return pref.getString(key, defValue);
	}
}
