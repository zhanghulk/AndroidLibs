package com.hulk.byod.ccb.xml.msg.base;

/**
 * TX_BODY为字符串的响应xml节点TX
 * Created by zhanghao on 2017/12/21.
 */

public class StringResponseTx extends ResponseTxBase<String> {

    public StringResponseTx(ResponseTXHeader tx_header, String tx_body) {
    	super(tx_header, tx_body);
    }

    @Override
    public String toString() {
        return "StringResponseTx{" +
                "TX_HEADER=" + TX_HEADER +
                ", TX_BODY=" + TX_BODY +
                '}';
    }
}
