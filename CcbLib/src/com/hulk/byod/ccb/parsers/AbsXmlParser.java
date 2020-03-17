package com.hulk.byod.ccb.parsers;

import android.util.Log;

import com.hulk.byod.ccb.xml.httpbody.HttpBodyBase;
import com.hulk.byod.ccb.xml.msg.base.ResponseTXHeader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

/**
 * Created by zhanghao on 2017/10/23.
 */

public abstract class AbsXmlParser<T extends HttpBodyBase> {

    protected final static String TAG = "AbsXmlParser";
    private static final String UTF_8 = "utf-8";

    //TX_HEADER fields:
    public static final String TX_HEADER = "TX_HEADER";
    public static final String HEADER_CODE = "CODE";
    public static final String HEADER_MSG = "MSG";
    public static final String HEADER_TRADE_CODE = "TRADE_CODE";
    public static final String HEADER_PROCESS_SN = "PROCESS_SN";

    public static final String TX_BODY = "TX_BODY";

    public static XmlPullParser createPullParser(InputStream inputStream) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();
            //parser.setInput(inputStream, UTF_8);
            parser.setInput(new BufferedInputStream(inputStream), UTF_8);
            return parser;
        } catch (XmlPullParserException e) {
            loge("createPullParser " + e, e);
        }
        return null;
    }

    public static XmlPullParser createPullParser(String xmltext) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();
            Reader reader = new StringReader(xmltext);
            parser.setInput(reader);
            return parser;
        } catch (XmlPullParserException e) {
            loge("createPullParser " + e, e);
        }
        return null;
    }

    /**
     * nextText()直接获取下一个TEXT节点，获取text值，此时游标自动下移指向END_TAG.
     * @param parser
     * @return
     * @throws XmlPullParserException
     * @throws IOException
     */
    public static String nextText(XmlPullParser parser) throws XmlPullParserException, IOException {
        //确认当前的TAG是否为START_TAG，否则抛出异常
        parser.require(XmlPullParser.START_TAG, null, parser.getName());
        String value = parser.nextText();
        if (parser.getEventType() != XmlPullParser.END_TAG) {
            //TODO 注意: 4.0以下低版本SDK不会下一个END_TAG，需要兼容
            int type = parser.next();
        }
        return value;
    }

    public boolean parseXml(String xmlText, T httpBody) {
        return parseXml(createPullParser(xmlText), httpBody);
    }

    public boolean parseXml(InputStream input, T httpBody) {
        return parseXml(createPullParser(input), httpBody);
    }

    public boolean parseXml(XmlPullParser parser, T httpBody) {
        if(parser != null) {
            try {
                int eventType = parser.getEventType();
                int depth = parser.getDepth();
                while ((eventType = parser.nextTag()) != XmlPullParser.END_DOCUMENT) {
                    String name = parser.getName();
                    depth = parser.getDepth();
                    switch (eventType) {
                        case XmlPullParser.START_DOCUMENT:
                            logi("parseXml START_DOCUMENT: " + name + ", depth= " + depth);
                            break;
                        case XmlPullParser.START_TAG:
                            //在START_TAG中使用nextText()直接获取value，不需要在TEXT中单独解析value
                            logi("parseXml START_TAG: " + name + ", depth= " + depth);
                            boolean parsed = parseTX(parser, httpBody);
                            if (!parsed) {
                                Log.i(TAG, "Not need to parse filed: " + name);
                            }
                            break;
                        case XmlPullParser.TEXT:
                            logi("parseXml TEXT: " + name + ", Value= " + parser.getText() + ", depth= " + depth);
                            break;
                        case XmlPullParser.END_TAG:
                            logi("parseXml END_TAG: " + name + ", depth= " + depth);
                            break;
                        case XmlPullParser.END_DOCUMENT:
                            logi("parseXml END_DOCUMENT: " + name + ", depth= " + depth);
                            break;
                        default:
                            logw("parseXml eventType= " + eventType + ", depth= " + depth);
                            break;
                    }
                }
                return true;
            } catch (XmlPullParserException e) {
                loge("parseXml: " + e, e);
            } catch (IOException e) {
                loge("parseXml: " + e, e);
            }
        }
        return false;
    }

    public abstract boolean parseTX(XmlPullParser parser, T httpBody) throws XmlPullParserException, IOException;

    public boolean parseTXHeader(XmlPullParser parser, ResponseTXHeader header) {
        if (parser == null) {
            return false;
        }
        try {
            String name = parser.getName();
            if (HEADER_CODE.equals(name)) {
                header.CODE = nextText(parser);
                return true;
            } else if (HEADER_MSG.equals(name)) {
                header.MSG = nextText(parser);
                return true;
            } else if (HEADER_TRADE_CODE.equals(name)) {
                header.TRADE_CODE = nextText(parser);
                return true;
            } else if (HEADER_PROCESS_SN.equals(name)) {
                header.PROCESS_SN = nextText(parser);
                return true;
            }
        } catch (XmlPullParserException e) {
            loge("parseTXHeader: " + e, e);
        } catch (IOException e) {
            loge("parseTXHeader: " + e, e);
        }
        return false;
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
