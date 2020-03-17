package com.hulk.byod.ccb.xml.httpbody;

import android.content.Context;
import android.util.Log;

import com.hulk.util.gson.GsonParser;
import com.hulk.byod.ccb.XmlJsonUtils;
import com.hulk.byod.ccb.xml.msg.base.TxBase;
import com.hulk.byod.ccb.xml.msg.base.TxHeaderBase;

/**
 * xml结构基类
 * Created by zhanghao on 2017/12/20.
 * @param <T> The sub class to implements ITx<H, B>
 */

public abstract class HttpBodyBase<T extends TxBase> extends GsonParser implements IHttpBody<T> {

    protected final static String TAG = "HttpBody";

    public T TX = null;//xml root node

    public HttpBodyBase(T tx) {
        TX = tx;
    }

    @Override
    public T getTX() {
        return TX;
    }

    @Override
    public void setTX(T tx) {
        TX = tx;
    }

    @Override
    public void setTradeCode(String tradeCode) {
        Object h = getTX().getTX_HEADER();
        if (h instanceof TxHeaderBase) {
            TxHeaderBase txHeaderBase = (TxHeaderBase) h;
            txHeaderBase.setTradeCode(tradeCode);
        } else {
            Log.w(TAG, "setTradeCode unknown class: " + h.getClass());
        }
    }

    @Override
    public String getTradeCode() {
        Object h = getTX().getTX_HEADER();
        if (h instanceof TxHeaderBase) {
            TxHeaderBase txHeaderBase = (TxHeaderBase) h;
            return txHeaderBase.getTradeCode();
        } else {
            Log.w(TAG, "getTradeCode unknown class: " + h.getClass());
        }
        return "";
    }

    @Override
    public String formatAsXml(Context context, boolean original) {
        //默认直接toString
        return toJson();
    }

    @Override
    public T parseFrom(String xmlText) {
        HttpBodyBase httpBody = XmlJsonUtils.fromXml(xmlText, getClass(), null);
        if (httpBody != null) {
            TxBase tb = httpBody.getTX();
            return (T)tb;
        }
        return TX;
    }

    /**
     * 使用XmlJsonUtils.fromXml(xmlText, classOfT, null); 把xml字符串解析为T对象，保存到h对象中h对象中.
     * @param h
     * @param xmlText
     * @param <H>
     * @return
     */
    public static final <H extends HttpBodyBase> H parseFrom(H h, String xmlText) {
        if (h == null || !(h instanceof HttpBodyBase)) {
            throw new IllegalArgumentException("The H object must be not null ");
        }
        if (h == null || !(h instanceof HttpBodyBase)) {
            throw new IllegalArgumentException("The H class: " + h.getClass()+ " must extends HttpBodyBase ");
        }
        TxBase tx = h.parseFrom(xmlText);
        if (tx != null) {
            h.setTX(tx);
        } else {
            Log.w(TAG, "Failed to parse From: " + xmlText);
        }
        return h;
    }

    @Override
    public String toString() {
        return "HttpBody{" +
                "TX=" + TX +
                '}';
    }

    public static void logi(String msg) {
        Log.i(TAG, "" + msg);
    }
    public static void logw(String msg) {
        Log.w(TAG,"WARNING: " + msg);
    }

    public static void loge(String msg, Exception e) {
        Log.e(TAG, "ERROR: " + msg, e);
    }
}
