package com.hulk.byod.ccb;

import com.hulk.util.common.AesUtils;

import android.text.TextUtils;
import android.util.Log;


/**
 * Created by zhanghao on 2017/10/19.
 */

public class CCBEncryptUtils {

    public final static String TAG = "CCBEncryptUtils";

    public final static String KEY = "1234567890123456";//测试用的key,必须16个字符

    private static byte[] sRawKey = null;

    public static byte[] getRawKey() {
        if (sRawKey == null) {
            sRawKey = AesUtils.getRawKey(KEY);
        }
        return sRawKey;
    }

    public static byte[] encryptText(String clearText) {
        if (TextUtils.isEmpty(clearText)) {
            Log.e(TAG, "encryptText clearText is null !! ");
            return null;
        }
        String cipherText = AesUtils.encrypt(getRawKey(), clearText);
        if (cipherText == null) {
            Log.e(TAG, "encryptText cipherText is null !! ");
            return null;
        }
        Log.i(TAG, "encryptText: " + clearText + " >>> " + cipherText);
        return cipherText.getBytes();
    }

    public static String decryptText(String cipherText) {
        if (cipherText == null) {
            Log.e(TAG, "encryptText cipherText is null !! ");
            return "";
        }

        String clearText = AesUtils.decrypt(getRawKey(), cipherText);
        if (TextUtils.isEmpty(clearText)) {
            Log.e(TAG, "decryptText clearText is null !! ");
            return "";
        }
        Log.i(TAG, "decryptText: " + cipherText + " >>> " + clearText);
        return cipherText;
    }
}
