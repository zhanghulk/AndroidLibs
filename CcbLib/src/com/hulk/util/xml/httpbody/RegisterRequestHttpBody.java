package com.hulk.byod.parser.xml.httpbody;

import android.content.Context;
import android.util.Log;

import com.hulk.byod.parser.HulkXmlUtils;
import com.hulk.byod.parser.XmlJsonUtils;
import com.hulk.byod.parser.entity.RegRequestJsonArray;
import com.hulk.byod.parser.xml.msg.RegisterRequestTx;
import com.hulk.byod.parser.xml.msg.RegisterRequestTx.TXBody;
import com.hulk.byod.parser.xml.msg.base.RequestTXHeader;

/**
 * 免认证注册接口请求http body (网络传输字符串需要进行加密)
 * Created by zhanghao on 2017/10/17.
 */

public class RegisterRequestHttpBody extends HttpBodyBase<RegisterRequestTx> {
    public RegisterRequestHttpBody(RequestTXHeader tx_header, TXBody tx_body) {
        super(new RegisterRequestTx(tx_header, tx_body));
    }

    public RegisterRequestHttpBody(RequestTXHeader tx_header, String op_type, RegRequestJsonArray array) {
        this(tx_header, new TXBody(op_type, array == null ? "[]" : array.toJson()));
    }

    @Override
    public RegisterRequestTx parseFrom(String xmlText) {
        RegisterRequestHttpBody httpBody = XmlJsonUtils.fromXml(xmlText, getClass(), null);
        return httpBody != null ? httpBody.getTX() : null;
    }

    @Override
    public String formatAsXml(Context context, boolean original) {
        return formatAsXml(context, this, original);
    }

    public static String formatAsXml(Context context, RegisterRequestHttpBody body, boolean original) {
        RequestTXHeader h = body.getTX().getTX_HEADER();
        TXBody b = body.getTX().getTX_BODY();
        if (b == null) {
            Log.w(TAG, "createHttpBodyText TXBody is null !! ");
            return "";
        }
        //request xml template: assets/ccb/TS1609031_REQ.xml
        String requestTemplate = HulkXmlUtils.readRequestXmlText(context, body.getTradeCode(), original);
        // requestTemplate = REQUEST_HTTP_BODY_TEMPLATE;
        return String.format(requestTemplate,
                h.TRADE_CODE, h.CLIENT_VERSION, h.CLIENT_IP, h.NETWORK_CARD_MAC,/**body header*/
                b.OP_TYPE, b.REQUEST);//body body
    }
}
