package com.hulk.byod.parser.xml.httpbody;

import android.content.Context;
import android.util.Log;

import com.hulk.byod.parser.HulkXmlUtils;
import com.hulk.byod.parser.XmlJsonUtils;
import com.hulk.byod.parser.entity.ActivityJsonArray;
import com.hulk.byod.parser.entity.TerminalInfo;
import com.hulk.byod.parser.xml.msg.HeartbeatRequestTx;
import com.hulk.byod.parser.xml.msg.HeartbeatRequestTx.TXBody;
import com.hulk.byod.parser.xml.msg.base.RequestTXHeader;

/**
 * Created by zhanghao on 2017/10/17.
 */

public class HeartbeatRequestHttpBody extends HttpBodyBase<HeartbeatRequestTx> {
    public HeartbeatRequestHttpBody(RequestTXHeader tx_header, TXBody tx_body) {
        super(new HeartbeatRequestTx(tx_header, tx_body));
    }

    public HeartbeatRequestHttpBody(RequestTXHeader tx_header, TerminalInfo info, ActivityJsonArray array) {
        this(tx_header, new TXBody(info, array));
    }

    @Override
    public HeartbeatRequestTx parseFrom(String xmlText) {
        HeartbeatRequestHttpBody httpBody = XmlJsonUtils.fromXml(xmlText, getClass(), null);
        return httpBody != null ? httpBody.getTX() : null;
    }

    @Override
    public String formatAsXml(Context context, boolean original) {
        return formatAsXml(context, this, original);
    }

    public static String formatAsXml(Context context, HeartbeatRequestHttpBody body, boolean original) {
        RequestTXHeader h = body.getTX().getTX_HEADER();
        TXBody b = body.getTX().getTX_BODY();
        if (b == null) {
            Log.w(TAG, "createHttpBodyText HeartReqTXBody is null !! ");
            return "";
        }
        String requestTemplate = HulkXmlUtils.getHeartbeatReqTemplate(context, original);
        // requestTemplate = REQUEST_HTTP_BODY_TEMPLATE;
        return String.format(requestTemplate,
                h.TRADE_CODE, h.CLIENT_VERSION, h.CLIENT_IP, h.NETWORK_CARD_MAC,/**body header*/
                b.TERMINAL_INFO, b.ACTIVITY);//body body
    }
}
