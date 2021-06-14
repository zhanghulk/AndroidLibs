package com.hulk.byod.parser.xml.httpbody;

import android.content.Context;
import android.util.Log;

import com.hulk.byod.parser.HulkXmlUtils;
import com.hulk.byod.parser.XmlJsonUtils;
import com.hulk.byod.parser.xml.msg.AuthRequestTx;
import com.hulk.byod.parser.xml.msg.AuthRequestTx.Entry;
import com.hulk.byod.parser.xml.msg.AuthRequestTx.TXBody;
import com.hulk.byod.parser.xml.msg.base.RequestTXHeader;

/**
 * Created by zhanghao on 2017/10/17.
 */

public class AuthRequestHttpBody extends HttpBodyBase<AuthRequestTx> {

    public AuthRequestHttpBody(RequestTXHeader tx_header, Entry entry) {
        this(new AuthRequestTx(tx_header, new TXBody(entry)));
    }

    public AuthRequestHttpBody(AuthRequestTx tx) {
        super(tx);
    }

    @Override
    public String formatAsXml(Context context, boolean original) {
        return formatAsXml(context, this, original);
    }

    @Override
    public AuthRequestTx parseFrom(String xmlText) {
        // 此处XmlNodeCallback传null,如果存在xml中时数组的情况，就实现callback
        AuthRequestHttpBody httpBody = XmlJsonUtils.fromXml(xmlText, getClass(), null);
        return httpBody != null ? httpBody.getTX() : null;
    }

    public static String formatAsXml(Context context, AuthRequestHttpBody body, boolean original) {
        RequestTXHeader h = body.getTX().getTX_HEADER();
        TXBody b = body.getTX().getTX_BODY();
        if (b == null) {
            Log.w(TAG, "createHttpBodyText TXBody is null !! ");
            return "";
        }
        Entry e = b.getENTRY();
        if (e == null) {
            Log.w(TAG, "createHttpBodyText Entry is null !! ");
            return "";
        }
        //request xml template: assets/ccb/TS1604051_REQ.xml
        String requestTemplate = HulkXmlUtils.readRequestXmlText(context, body.getTradeCode(), original);
        // requestTemplate = REQUEST_HTTP_BODY_TEMPLATE;
        return String.format(requestTemplate,
                h.TRADE_CODE, h.CLIENT_VERSION, h.CLIENT_IP, h.NETWORK_CARD_MAC,/**body header*/
                e.AUTH_TYPE, e.LOGIN_NAME, e.SEC_VOUCHER, e.IS_CREATE_TICKET, e.IS_GET_LOGINED_APP, e.SERVICE_POINT);//body body
    }
}
