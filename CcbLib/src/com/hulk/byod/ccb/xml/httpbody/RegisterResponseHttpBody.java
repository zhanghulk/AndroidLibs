package com.hulk.byod.ccb.xml.httpbody;

import com.hulk.byod.ccb.XmlJsonUtils;
import com.hulk.byod.ccb.xml.msg.RegisterResponseTx;
import com.hulk.byod.ccb.xml.msg.base.ResponseTXHeader;

/**
 * 建行免认证注册接口请求返回结果响应(网络传输字符串需要进行加密)
 * <p>xml template refer to assets/ccb/TS1609050_RESP.xml
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
