package com.hulk.byod.ccb;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.JsonSyntaxException;
import com.hulk.util.gson.XmlGsonUtils;
import com.hulk.util.xml.XmlJsonParser;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by zhanghao on 2017/10/30.
 */

public class XmlJsonUtils {

    public static final String TAG = "XmlJsonUtils";
    /**
     * 解析xml文本，通过XmlJsonParser和Gson, 输出classOfT对象
     * @param xmlText xml格式文本
     * @param classOfT  解析输出实体对象类型
     * @param callback  xml节点属性回调,主要实现节点是否为数组，还是字段集合， 为null时，默认字段集合
     * @param <T>
     * @return
     */
    public static <T> T fromXml(String xmlText, Class<T> classOfT, XmlJsonParser.XmlNodeCallback callback) {
        if (TextUtils.isEmpty(xmlText)) {
            Log.e(TAG, "fromXml canceled, for xmlText is empty !! ");
            return null;
        }
        try {
            T t = XmlGsonUtils.fromXml(xmlText, classOfT, callback);
            Log.i(TAG, "fromXml: " + xmlText + " >>>>>>to:\n" + t);
            return t;
        } catch (XmlPullParserException e) {
            Log.e(TAG, "fromXml: " + e, e);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "fromXml: " + e, e);
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "fromXml: " + e, e);
        } catch (IOException e) {
            Log.e(TAG, "fromXml: " + e, e);
        }
        return null;
    }
}
