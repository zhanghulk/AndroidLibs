package com.hulk.byod.parser.xml.msg.base;

/**
 * 响应xml报文TX_HEADER节点结构
 * Created by zhanghao on 2017/10/18.
 */

public class ResponseTXHeader extends TxHeaderBase {

    public static final String TRAN_ERROR_SUCCESS = "000000000000";

    public String CODE = "";//错误码
    public String MSG = "";//错误信息
    public String PROCESS_SN = "";
    public String SERVER_VERSION = "";

    public ResponseTXHeader(String tradeCode) {
        super(tradeCode);
    }

    public ResponseTXHeader(String code, String msg, String trade_code, String process_sn, String server_version) {
        super(trade_code);
        this.CODE = code;
        this.MSG = msg;
        this.PROCESS_SN = process_sn;
        this.SERVER_VERSION = server_version;
    }

    public String getCode() {
        return CODE;
    }

    public String getMsg() {
        return MSG;
    }

    public boolean isSuccess() {
        return TRAN_ERROR_SUCCESS.equals(CODE);
    }

    @Override
    public String toString() {
        return "ResponseTXHeader{" +
                "TRADE_CODE='" + TRADE_CODE + '\'' +
                ", CODE='" + CODE + '\'' +
                ", MSG='" + MSG + '\'' +
                ", PROCESS_SN='" + PROCESS_SN + '\'' +
                ", SERVER_VERSION='" + SERVER_VERSION + '\'' +
                '}';
    }
}
