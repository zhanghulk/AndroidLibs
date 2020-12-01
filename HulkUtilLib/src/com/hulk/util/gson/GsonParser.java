package com.hulk.util.gson;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * gson解析器，可实现子类和json字符串之间互转
 *
 * @Author: zhanghao
 * @Time: 2020-04-28 14:59:15
 * @Version: 1.0.0
 */
public class GsonParser {
    public static final String TAG = "GsonParser";
    public static final boolean CATCH_ERROR = false;//是否补货异常
    //原始的json字符串
    protected String rawJson;

    public void setRawJson(String rawJson) {
        this.rawJson = rawJson;
    }

    public String getRawJson() {
        return rawJson;
    }

    /**
     * 对象转化为Json
     * @return
     */
    public String toJson() {
        return toJson(this);
    }

    /**
     * 对象转化为Json
     * @return
     */
    public JSONObject toJsonObject() {
        String json = toJson();
        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            Log.e(TAG, "toJsonObject: " + e + ", json= " + json);
        }
        return null;
    }

    public static String toJson(Object src) {
        try {
            Gson gson = new Gson();
            return gson.toJson(src);
        } catch (Exception e) {
            Log.e(TAG, "toJson: " + e + ", src= " + src);
        }
        return "";
    }

    /**
     * Json转化为对象
     * @param jsonObject
     * @param classOfT
     * @param <T>
     * @return
     */
    public static <T extends GsonParser> T fromJson(JSONObject jsonObject, Class<T> classOfT) {
        if (jsonObject == null) {
            Log.e(TAG, "fromJson failed: jsonObject is null");
            return null;
        }
        return fromJson(jsonObject.toString(), classOfT);
    }

    /**
     * Json转化为对象
     * @param json
     * @param classOfT
     * @param <T>
     * @return
     */
    public static <T extends GsonParser> T fromJson(String json, Class<T> classOfT) {
        if (TextUtils.isEmpty(json) || !json.startsWith("{")) {
            Log.e(TAG, "fromJson failed invalid json: " + json);
            return null;
        }
        if (CATCH_ERROR) {
            try {
                return fromJsonBase(json, classOfT);
            } catch (Exception e) {
                Log.e(TAG, "fromJson failed: " + e + ", classOfT= " + classOfT + ", json= " + json);
            }
        }
        return fromJsonBase(json, classOfT);
    }

    /**
     * Json转化为对象,如果字符串非法或者错误，直接抛出异常
     * @param json
     * @param classOfT
     * @param <T>
     * @return
     */
    public static <T extends GsonParser> T fromJsonBase(String json, Class<T> classOfT) {
        Gson gson = new Gson();
        T t = gson.fromJson(json, classOfT);
        t.setRawJson(json);
        return t;
    }

    /**
     *  json数组对象转化为对象数组
     * @param jsonArray
     * @param classOfT
     * @param <T>
     * @return
     */
    public static <T extends GsonParser> ArrayList<T> fromJsonArrayOpt(JSONArray jsonArray, Class<T> classOfT) {
        if (jsonArray == null) {
            Log.e(TAG, "fromJsonArrayOpt failed: jsonArray is null");
            return null;
        }
        ArrayList<T> list = new ArrayList<T>();
        int length = jsonArray.length();
        for (int i = 0; i < length; i++) {
            JSONObject json = jsonArray.optJSONObject(i);
            T detail = fromJson(json, classOfT);
            if (detail != null) {
                list.add(detail);
            }
        }
        return list;
    }

    public static <T extends GsonParser> ArrayList<T> fromJsonArrayOpt(String jsonArrayStr, Class<T> classOfT) {
        try {
            return fromJsonArrayOpt(new JSONArray(jsonArrayStr), classOfT);
        } catch (JSONException e) {
            Log.e(TAG, "fromJsonArrayOpt failed: " + e + ", classOfT= " + classOfT + ", jsonArrayStr= " + jsonArrayStr);
        }
        return null;
    }

    /**
     * json数组字符串转化为对象数组
     * eg:
     * String userJson = "[{'name': 'Alex','id': 1}, "
     *                 + "{'name': 'Brian','id':2}, "
     *                 + "{'name': 'Charles','id': 3}]";
     * Gson gson = new Gson();
     * User[] userArray = gson.fromJson(userJson, User[].class);
     *
     * @param jsonArrayStr
     * @param classOfTArray 数组的class eg User[].class
     * @param <T>
     * @return
     */
    public static <T> T[] fromJsonArrayBase(String jsonArrayStr, Class classOfTArray) {
        Gson gson = new Gson();
        T[] array = (T[]) gson.fromJson(jsonArrayStr, classOfTArray);
        return array;
    }

    /**
     * json数组字符串转化为对象数组
     * eg:
     * String userJson = "[{'name': 'Alex','id': 1}, "
     *                 + "{'name': 'Brian','id':2}, "
     *                 + "{'name': 'Charles','id': 3}]";
     * Gson gson = new Gson();
     * User[] userArray = gson.fromJson(userJson, User[].class);
     *
     * @param jsonArrayStr
     * @param classOfTArray 数组的class eg User[].class
     * @param <T>
     * @return
     */
    public static <T> T[] fromJsonArray(String jsonArrayStr, Class classOfTArray) {
        if (CATCH_ERROR) {
            try {
                return fromJsonArrayBase(jsonArrayStr, classOfTArray);
            } catch (Exception e) {
                Log.e(TAG, "fromJsonArray failed: " + e + ", classOfTArray= " + classOfTArray + ", jsonArrayStr= " + jsonArrayStr);
                return null;
            }
        }
        return fromJsonArrayBase(jsonArrayStr, classOfTArray);
    }

    public static Object opt(String json, String name) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            return jsonObject.opt(name);
        } catch (Exception e) {
            Log.e(TAG, "opt failed: " + e + ", json= " + json);
        }
        return "";
    }

    public static String optString(String json, String name) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            return jsonObject.optString(name, "");
        } catch (Exception e) {
            Log.e(TAG, "optString failed: " + e + ", json= " + json);
        }
        return "";
    }

    public static String optInt(String json, String name) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            return jsonObject.optString(name, "");
        } catch (Exception e) {
            Log.e(TAG, "optInt failed: " + e + ", json= " + json);
        }
        return "";
    }

    public static boolean optBoolean(String json, String name) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            return jsonObject.optBoolean(name, false);
        } catch (Exception e) {
            Log.e(TAG, "optBoolean failed: " + e + ", json= " + json);
        }
        return false;
    }

    public static JSONObject optJSONObject(String json, String name) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            return jsonObject.optJSONObject(name);
        } catch (Exception e) {
            Log.e(TAG, "optJSONObject failed: " + e + ", json= " + json);
        }
        return null;
    }

    public static JSONArray optJSONArray(String json, String name) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            return jsonObject.optJSONArray(name);
        } catch (Exception e) {
            Log.e(TAG, "optJSONArray failed: " + e + ", json= " + json);
        }
        return null;
    }

    public static boolean exists(String json, String name) {
        return opt(json, name) != null;
    }

    @Override
    public String toString() {
        return "GsonParser{" +
                "rawJson='" + rawJson + '\'' +
                '}';
    }
}
