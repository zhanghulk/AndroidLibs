package com.hulk.byod.ccb;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.hulk.byod.ccb.entity.CCBPolicy;

import java.util.Set;

/**
 * Created by zhanghao on 2017/10/24.
 */

public class CCBDataHelper {

    protected final static String TAG = "CCBDataHelper";

    public static final String SHARED_PREFERENCE_NAME = "ccb_data_pref";

    //建行服务器返回的策略json字段
    public final static String CCB_POLICY = "ccb_policy";

    //默认心跳时间
    public final static String CCB_HEARTBEAT_INTERVAL = "ccb_heartbeat_interval";

    Context mContext;
    SharedPreferences mPref = null;

    CCBPolicy mPolicy = null;
    //心跳间隔时间，单位: 毫秒
    long mHeartbeatIntervalMillis = 0;

    private static CCBDataHelper instance;

    public static CCBDataHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (CCBDataHelper.class) {
                if (instance == null) {
                    instance = new CCBDataHelper(context);
                }
            }
        }
        return instance;
    }

    private CCBDataHelper(Context context) {
        mContext = context;
        mPref = context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        mHeartbeatIntervalMillis = getLong(CCB_HEARTBEAT_INTERVAL, 0);
        String json = getString(CCB_POLICY, null);
        if (!TextUtils.isEmpty(json)) {
            mPolicy = CCBPolicy.fromJson(json, CCBPolicy.class);
        }
    }

    public boolean putLong(String key , long value) {
        return mPref.edit().putLong(key, value).commit();
    }

    public long getLong(String key, long def) {
        return mPref.getLong(key, def);
    }

    public boolean putString(String key ,String value) {
        return mPref.edit().putString(key, value).commit();
    }

    public String getString(String key, String def) {
        return mPref.getString(key, def);
    }

    public Set<String> getStringSet(String key) {
        return mPref.getStringSet(key, null);
    }

    public boolean putStringSet(String key, Set<String> set) {
        return mPref.edit().putStringSet(key, set).commit();
    }

    /**
     * 心跳间隔时间，单位: 毫秒
     * @return
     */
    public long getHeartbeatIntervalMillis() {
        return mHeartbeatIntervalMillis;
    }

    public void setHeartbeatIntervalMillis(long intervalMillis) {
        if (mHeartbeatIntervalMillis != intervalMillis) {
            mHeartbeatIntervalMillis = intervalMillis;
            putLong(CCB_HEARTBEAT_INTERVAL, intervalMillis);
        }
    }

    public CCBPolicy getPolicy() {
        return mPolicy;
    }

    public void savePolicy(CCBPolicy policy) {
        if (policy != null) {
            if (TextUtils.isEmpty(policy.POLICY_CODE)) {
                Log.e(TAG, "savePolicy failed, invalid policy: " + policy);
                return;
            }
            if (mPolicy == null) {
                mPolicy = policy;
            } else {
                mPolicy.POLICY_CODE = policy.POLICY_CODE;
                mPolicy.POLICY_NAME = policy.POLICY_NAME;
                mPolicy.POLICY_BIND_TYPE = policy.POLICY_BIND_TYPE;
                mPolicy.STATUS = policy.STATUS;
            }

            String json = policy.toJson();
            if (!TextUtils.isEmpty(json)) {
                Log.i(TAG, "Save new policy json: " + json);
                putString(CCB_POLICY, json);
            } else {
                Log.w(TAG, "Save failed new policy: " + policy);
            }
        }
    }

    /**
     * 更新本地安全策略状态， 如果没有上传
     * @param status 1 表示已经上传执行更新成功，其他只已在扩展，默认0
     */
    public void setPolicyStatus(int status) {
        if (mPolicy != null) {
            mPolicy.setStatus(status);
            String json = mPolicy.toJson();
            Log.i(TAG, "set Policy new Status: " + status);
            putString(CCB_POLICY, json);
        }
    }
}
