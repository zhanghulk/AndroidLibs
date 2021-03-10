package com.hulk.byod.parser;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.hulk.byod.parser.entity.HulkPolicy;

import java.util.Set;

/**
 * Created by zhanghao on 2017/10/24.
 */

public class HulkDataHelper {

    protected final static String TAG = "HulkDataHelper";

    public static final String SHARED_PREFERENCE_NAME = "hulk_data_pref";

    //服务器返回的策略json字段
    public final static String Hulk_POLICY = "policy";

    //默认心跳时间
    public final static String Hulk_HEARTBEAT_INTERVAL = "heartbeat_interval";

    Context mContext;
    SharedPreferences mPref = null;

    HulkPolicy mPolicy = null;
    //心跳间隔时间，单位: 毫秒
    long mHeartbeatIntervalMillis = 0;

    private static HulkDataHelper instance;

    public static HulkDataHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (HulkDataHelper.class) {
                if (instance == null) {
                    instance = new HulkDataHelper(context);
                }
            }
        }
        return instance;
    }

    private HulkDataHelper(Context context) {
        mContext = context;
        mPref = context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        mHeartbeatIntervalMillis = getLong(Hulk_HEARTBEAT_INTERVAL, 0);
        String json = getString(Hulk_POLICY, null);
        if (!TextUtils.isEmpty(json)) {
            mPolicy = HulkPolicy.fromJson(json, HulkPolicy.class);
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
            putLong(Hulk_HEARTBEAT_INTERVAL, intervalMillis);
        }
    }

    public HulkPolicy getPolicy() {
        return mPolicy;
    }

    public void savePolicy(HulkPolicy policy) {
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
                putString(Hulk_POLICY, json);
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
            putString(Hulk_POLICY, json);
        }
    }
}
