package com.hulk.byod.parser.xml.httpbody;

import android.content.Context;

import com.hulk.byod.parser.XmlJsonUtils;
import com.hulk.byod.parser.xml.msg.AuthResponseTx;
import com.hulk.byod.parser.xml.msg.AuthResponseTx.Entry;
import com.hulk.byod.parser.xml.msg.AuthResponseTx.TXBody;
import com.hulk.byod.parser.xml.msg.base.ResponseTXHeader;
import com.hulk.byod.parser.entity.HulkPolicy;

/**
 * Created by zhanghao on 2017/10/17.
 */

public class AuthResponseHttpBody extends HttpBodyBase<AuthResponseTx> {
	private static final String TAG = "AuthResponseHttpBody";

    public AuthResponseHttpBody() {
        this(null, null);
    }

    public AuthResponseHttpBody(ResponseTXHeader tx_header, TXBody tx_body) {
        super(new AuthResponseTx(tx_header, tx_body));
    }

    public ResponseTXHeader getTX_HEADER() {
        return TX.getTX_HEADER();
    }

    public TXBody getTX_BODY() {
        return TX.getTX_BODY();
    }

    public void setTX_HEADER(ResponseTXHeader tx_header) {
        TX.setTX_HEADER(tx_header);
    }

    public void setTX_BODY(TXBody tx_body) {
        TX.setTX_BODY(tx_body);
    }

    public boolean isSuccess() {
        return getTX().isSuccess();
    }

    @Override
    public String formatAsXml(Context context, boolean original) {
        //默认直接toString
        return toJson();
    }

    @Override
    public AuthResponseTx parseFrom(String xmlText) {
        AuthResponseHttpBody httpBody = XmlJsonUtils.fromXml(xmlText, getClass(), null);
        return httpBody != null ? httpBody.getTX() : null;
    }

    @Override
    public String toString() {
        return "HttpBody{" +
                "TX=" + TX +
                '}';
    }

    public Entry getENTRY() {
        TXBody txBody = getTX_BODY();
        return txBody != null ? txBody.getENTRY() : null;
    }

    public AuthResponseTx.EntryExtends getEntryExtends() {
        Entry entry = getENTRY();
        return entry != null ? entry.EXTENDS : null;
    }

    public AuthResponseTx.EntryPolicies getEntryPolicies() {
        Entry entry = getENTRY();
        return entry != null ? entry.POLICIES : null;
    }

    public HulkPolicy getPolicy() {
        AuthResponseTx.EntryPolicies policies = getEntryPolicies();
        return policies != null ? policies.POLICY : null;
    }
}
