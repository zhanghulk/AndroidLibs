package com.hulk.byod.parser.xml.httpbody;

import com.hulk.byod.parser.XmlJsonUtils;
import com.hulk.byod.parser.xml.msg.RegisterResponseTx;
import com.hulk.byod.parser.xml.msg.base.ResponseTXHeader;

/**
 * Created by zhanghao on 2017/10/17.
 */

public class RegisterResponseHttpBody extends HttpBodyBase<RegisterResponseTx> {
    public RegisterResponseHttpBody() {
        this(null, null);
    }

    public RegisterResponseHttpBody(ResponseTXHeader tx_header, String tx_body) {
        super(new RegisterResponseTx(tx_header, tx_body));
    }

    public boolean isSuccess() {
        return getTX().isSuccess();
    }

    @Override
    public RegisterResponseTx parseFrom(String xmlText) {
        RegisterResponseHttpBody httpBody = XmlJsonUtils.fromXml(xmlText, getClass(), null);
        return httpBody != null ? httpBody.getTX() : null;
    }
}
