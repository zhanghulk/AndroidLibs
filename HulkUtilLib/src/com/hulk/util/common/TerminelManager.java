package com.hulk.util.common;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by zhanghao on 2017/10/24.
 */

public class TerminelManager {

    private final static String TAG = "TerminelManager";

    private final static String TERMINAL_ID_KEY = "terminal_id";

    static String sTerminalID = null;

    public static String getAndroidID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String getTerminelID(Context context) {
        if (sTerminalID == null) {
            synchronized (TerminelManager.class) {
                if (sTerminalID == null) {
                    sTerminalID = getPrefTerminalID(context);
                }
            }
        }
        return sTerminalID;
    }

    public static String getPrefTerminalID(Context context) {
        String terminalID = PackageUtils.getString(context, TERMINAL_ID_KEY, null);
        if (terminalID == null) {
            terminalID = createTerminelID(context);
            PackageUtils.setString(context, TERMINAL_ID_KEY, terminalID);
            Log.w(TAG, "Create new terminal ID: " + terminalID);
        }
        return terminalID;
    }

    public static String createTerminelID(Context context) {
        //TODO ANDROID_ID
        String terminelID = getAndroidID(context);
        String encoedText = Base64Encoder.encode(terminelID.getBytes());
        Log.i(TAG, "TerminelID: " + terminelID + ", encoedText: " + encoedText);
        return encoedText;
    }
    
}
