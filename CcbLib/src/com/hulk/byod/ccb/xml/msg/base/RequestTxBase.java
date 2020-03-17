package com.hulk.byod.ccb.xml.msg.base;

/**
 * 建行请求报文节点基本模型
 * Created by zhanghao on 2017/10/18.
 */

public abstract class RequestTxBase<B> extends TxBase<RequestTXHeader, B> {
    // msg special Request tx header node
    protected RequestTXHeader TX_HEADER = null;

    public RequestTxBase(RequestTXHeader tx_header, B tx_body) {
        this.TX_HEADER = tx_header;
        this.TX_BODY = tx_body;
    }

    public RequestTXHeader getTX_HEADER() {
        return TX_HEADER;
    }

    public void setTX_HEADER(RequestTXHeader tx_header) {
        TX_HEADER = tx_header;
    }

    @Override
    public String toString() {
        return "RequestTx{" +
                "TX_HEADER=" + TX_HEADER +
                ", TX_BODY=" + TX_BODY +
                '}';
    }
}
