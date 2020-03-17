package com.hulk.util.gson;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.hulk.util.common.ConvertUtil;
import com.hulk.util.xml.XmlCallback;
import com.hulk.util.xml.XmlJsonParser;
import com.hulk.util.xml.XmlNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import org.xmlpull.v1.XmlPullParserException;

/**
 * xml转换为对应的实体对象.
 * <p>此工具类必须依赖于google的gson解析库
 * <p>xml文本不能带有换行符，读取文本时去掉换行符和首尾空格，否则会解析错误
 * Created by zhanghao on 2017/10/30.
 */

public class XmlGsonUtils {

    public static final String TAG = "XmlGsonUtils";

    /**
     * 解析xml输入流，通过XmlJsonParser和Gson, 输出classOfT对象
     * @param input  输入字节流
     * @param classOfT 解析输出实体对象类型
     * @param callback xml节点属性回调,主要实现节点是否为数组，还是字段集合， 为null时，默认字段集合
     * @param <T>
     * @return the object of classOfT according to parse xml
     * @throws IllegalArgumentException 
     * @throws XmlPullParserException 
     * @throws JsonSyntaxException 
     * @throws IOException 
     */
    public static <T> T fromXml(InputStream input, Class<T> classOfT, XmlCallback callback)
    		throws IllegalArgumentException, XmlPullParserException, JsonSyntaxException, IOException {
    	if (input == null) {
            Log.e(TAG, "fromXml ERROR: input is null");
            throw new IllegalArgumentException("fromXml ERROR: input is null");
        }
        XmlJsonParser p = new XmlJsonParser(input);
        return fromXml(p, classOfT, callback);
    }

    /**
     * 解析xml文本，通过XmlJsonParser和Gson, 输出classOfT对象
     * @param mXmlText xml格式文本
     * @param classOfT  解析输出实体对象类型
     * @param callback  xml节点属性回调,主要实现节点是否为数组，还是字段集合， 为null时，默认字段集合
     * @param <T>
     * @return the object of classOfT according to parse xml
     * @throws IllegalArgumentException 
     * @throws XmlPullParserException 
     * @throws JsonSyntaxException 
     * @throws IOException 
     */
    public static <T> T fromXml(String xmlText, Class<T> classOfT, XmlCallback callback)
    		throws IllegalArgumentException, XmlPullParserException, JsonSyntaxException, IOException {
    	if (xmlText == null || "".equals(xmlText)) {
            Log.e(TAG, "fromXml ERROR: xmlText is null or \"\"");
            throw new IllegalArgumentException("fromXml ERROR: xmlText is null or \\\"\\\"");
        }
    	if (!xmlText.startsWith("<?xml")) {
    		String errMsg = "fromXml ERROR: Not startsWith \"<?xml\", Invalid xmlText: " + xmlText;
            Log.e(TAG, errMsg);
            throw new IllegalArgumentException(errMsg);
        }
        if (classOfT == null) {
            Log.e(TAG, "fromXml ERROR: classOfT is null");
            throw new IllegalArgumentException("fromXml ERROR: classOfT is null");
        }
      //去除字符串中的回车、换行符、制表符，否则解析失败
        String fixedXml = ConvertUtil.removeLineBlank(xmlText);
        Log.i(TAG, "fromXml fixed xml: " + fixedXml + ", origin xmlText: " + xmlText);
        StringReader reader = new StringReader(fixedXml);
        return fromXml(reader, classOfT, callback);
    }

    /**
     * 解析xml文本，通过XmlJsonParser和Gson, 输出classOfT对象
     * @param reader   xml内容读取器
     * @param classOfT 解析输出实体对象类型
     * @param callback xml节点属性回调,主要实现节点是否为数组，还是字段集合， 为null时，默认字段集合
     * @param <T>
     * @return the object of classOfT according to parse xml
     * @throws IllegalArgumentException 
     * @throws XmlPullParserException 
     * @throws JsonSyntaxException 
     * @throws IOException 
     */
    public static <T> T fromXml(Reader reader, Class<T> classOfT, XmlCallback callback)
    		throws IllegalArgumentException, XmlPullParserException, JsonSyntaxException, IOException {
    	if (reader == null) {
            Log.e(TAG, "fromXml ERROR: reader is null");
            throw new IllegalArgumentException("fromXml ERROR: reader is null");
        }
        XmlJsonParser p = new XmlJsonParser(reader);
        return fromXml(p, classOfT, callback);
    }

    /**
     * 解析xml文本，通过XmlJsonParser和Gson, 输出classOfT对象
     * @param parser  XmlJsonParser对象
     * @param classOfT 解析输出实体对象类型
     * @param callback xml节点属性回调,主要实现节点是否为数组，还是字段集合， 为null时，默认字段集合
     * @param <T>
     * @return the object of classOfT according to parse xml
     * @throws IllegalArgumentException 
     * @throws XmlPullParserException 
     * @throws JsonSyntaxException 
     * @throws IOException 
     */
    public static <T> T fromXml(XmlJsonParser parser, Class<T> classOfT, XmlCallback callback)
    		throws IllegalArgumentException, XmlPullParserException, JsonSyntaxException, IOException {
        if (parser == null) {
            Log.e(TAG, "fromXml ERROR: XmlJsonParser is null");
            throw new IllegalArgumentException("fromXml ERROR: XmlJsonParser is null");
        }
        if (classOfT == null) {
            Log.e(TAG, "fromXml ERROR: Gson parse template classOfT is null");
            throw new IllegalArgumentException("fromXml ERROR: classOfT is null");
        }
        //start parse xml
        XmlNode rootNode = parser.parse(callback);
        if(rootNode == null) {
        	throw new XmlPullParserException("fromXml parse ERROR: rootNode is null");
        }
        try {
        	String json = rootNode.toJsonText();
            Gson gson = new Gson();
            T out = gson.fromJson(json, classOfT);
            Log.i(TAG, "fromXml json: " + json + " \n  >>> to a instance if " + classOfT +"\n >>> " + out);
            return out;
        } catch(JsonSyntaxException e) {
        	Log.e(TAG, "fromXml Gson parse failed: " + e);
        	throw e;
        }
    }
}
