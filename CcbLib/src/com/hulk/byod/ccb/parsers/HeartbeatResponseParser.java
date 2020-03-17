package com.hulk.byod.ccb.parsers;

import com.hulk.byod.ccb.xml.httpbody.HeartbeatResponseHttpBody;
import com.hulk.byod.ccb.xml.msg.base.ResponseTXHeader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by zhanghao on 2017/10/23.
 */

public class HeartbeatResponseParser extends AbsXmlParser<HeartbeatResponseHttpBody> {
    @Override
    public boolean parseTX(XmlPullParser parser, HeartbeatResponseHttpBody httpBody) throws XmlPullParserException, IOException {
        ResponseTXHeader header = httpBody.getTX().getTX_HEADER();
        boolean headerParsed = parseTXHeader(parser, header);
        if (!headerParsed) {
            String name = parser.getName();
            String value = nextText(parser);
            //TODO TX_BODY 直接设置值，没有子元素
            if (TX_BODY.equals(name)) {
                httpBody.getTX().setTX_BODY(value);
                return true;
            }
        }
        return false;
    }
}
