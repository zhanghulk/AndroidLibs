package com.hulk.byod.parser.entity;

import android.util.Log;

import com.google.gson.Gson;
import com.hulk.util.gson.GsonParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by zhanghao on 2017/10/19.
 */

public abstract class JsonArrayBase<T extends GsonParser> {
    protected final static String TAG = "JsonArray";

    protected Collection<T> data = null;

    public JsonArrayBase(Collection<T> data) {
        if (data == null) {
            throw new IllegalArgumentException("The construct function param can not be null !! ");
        }
        this.data = data;
    }

    public void add(T item) {
        data.add(item);
    }

    public boolean remove(T item) {
        return data.remove(item);
    }

    public void clear() {
        data.clear();
    }

    /**
     * 转化为json数组字符串
     * @return
     */
    public String toJson() {
        try {
            Gson gson = new Gson();
            return gson.toJson(data, Collection.class);
        } catch (Exception e) {
            Log.e(TAG, "toJson Gson parse : " + e + " , data: " + data, e);
        }
        return "[]";
    }

    public List<String> getJsonList() {
        List<String> list = new ArrayList<String>();
        if (data != null) {
            for (T log: data) {
                list.add(log.toJson());
            }
        }
        return list;
    }

    @Override
    public String toString() {
        return "JsonArrayBase{" +
                "data=" + data +
                '}';
    }
}
