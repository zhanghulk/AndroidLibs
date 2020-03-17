package com.hulk.byod.ccb.xml.httpbody;

import android.content.Context;

/**
 * xml文本TX中的元素结构体接口
 * Created by zhanghao on 2017/11/28.
 */

public interface IHttpBody<T> {

    /**
     * 设置交易码
     * @param tradeCode
     */
    void setTradeCode(String tradeCode);

    /**
     * 获取交易码
     * @return
     */
    String getTradeCode();

    /**
     * xml文本的TX节点
     * @return
     */
    T getTX();

    /**
     * xml文本的TX根节点赋值
     * @param tx_body
     */
    void setTX(T tx_body);

    /**
     * 根据模板，格式化对象为HttpBody的xml文本
     * @param context
     * @param original 是否保持xml模板文件的原样格式， 为false将去掉换行符及首尾空格
     * @return
     */
    String formatAsXml(Context context, boolean original);

    String toJson();

    /**
     * 使用XmlJsonUtils.fromXml(xmlText, classOfT, null); xml字符串解析为T对象
     * <p>eg:
     * <p>@Override
     * <p>public AuthRequestTx parseFrom(String xmlText) {
     * <p>      // 此处XmlNodeCallback传null,如果存在xml中时数组的情况，就实现callback
     * <p>      AuthRequestHttpBody httpBody = XmlJsonUtils.fromXml(xmlText, getClass(), null);
     * <p>      return httpBody != null ? httpBody.getTX() : null;
     * <p>}
     * <p> 此处XmlNodeCallback传null,如果存在xml中时数组的情况，就实现callback
     * <p>AuthRequestHttpBody httpBody = XmlJsonUtils.fromXml(xmlText, getClass(), null);
     * <p>return httpBody != null ? httpBody.getTX() : null;
     * @param xmlText
     * @return
     */
    T parseFrom(String xmlText);
}
