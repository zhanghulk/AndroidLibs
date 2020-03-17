package com.hulk.byod.ccb.xml.msg.base;

/**
 * 建行响应报文节点基本模型
 * Created by zhanghao on 2017/12/21.
 */

public abstract class ResponseTxBase<B> extends TxBase<ResponseTXHeader, B> {
    // msg special Response tx header node
    protected ResponseTXHeader TX_HEADER = null;

    public ResponseTxBase(ResponseTXHeader tx_header, B tx_body) {
        this.TX_HEADER = tx_header;
    }

    public ResponseTXHeader getTX_HEADER() {
        return TX_HEADER;
    }

    public void setTX_HEADER(ResponseTXHeader tx_header) {
        TX_HEADER = tx_header;
    }
    
    public String getErrorCode() {
        return TX_HEADER != null ? TX_HEADER.CODE : "";
    }

    public String getErrorMsg() {
        return TX_HEADER != null ? TX_HEADER.MSG : "";
    }

    public boolean isSuccess() {
        return TX_HEADER != null ? TX_HEADER.isSuccess() : false;
    }

    @Override
    public String toString() {
        return "ResponseTx{" +
                "TX_HEADER=" + TX_HEADER +
                ", TX_BODY=" + TX_BODY +
                '}';
    }
}
