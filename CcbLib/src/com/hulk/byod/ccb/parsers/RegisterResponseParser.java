package com.hulk.byod.ccb.parsers;

import com.hulk.byod.ccb.xml.httpbody.RegisterResponseHttpBody;
import com.hulk.byod.ccb.xml.msg.base.ResponseTXHeader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * 注册返回结果解析器
 * Created by zhanghao on 2017/10/23.
 */

public class RegisterResponseParser extends AbsXmlParser<RegisterResponseHttpBody> {
    @Override
    public boolean parseTX(XmlPullParser parser, RegisterResponseHttpBody httpBody) throws XmlPullParserException, IOException {
        ResponseTXHeader header = httpBody.getTX().getTX_HEADER();
        boolean headerParsed = parseTXHeader(parser, header);
        //TODO 不需要解析TX_BODY 为空
        return headerParsed;
    }
}
