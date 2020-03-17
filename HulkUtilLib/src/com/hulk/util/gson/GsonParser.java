package com.hulk.util.gson;

import android.util.Log;

import com.google.gson.Gson;

/**
 * Created by zhanghao on 2017/10/19.
 */

public class GsonParser {

    public String toJson() {
        try {
            Gson gson = new Gson();
            return gson.toJson(this, getClass());
        } catch (Exception e) {
            Log.e("GsonParser", "toJson: " + toString(), e);
        }
        return "";
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        if (json == null || classOfT == null) {
            Log.e("GsonParser", "fromJson invalid json: " + json + ", classOfT: " + classOfT);
            return null;
        }
        try {
            Gson gson = new Gson();
            return gson.fromJson(json, classOfT);
        } catch (Exception e) {
            Log.e("GsonParser", "fromJson: " + json, e);
        }
        return null;
    }

    @Override
    public String toString() {
        return toJson();
    }
}
