package com.hulk.byod.ccb.xml.msg.base;

/**
 * 请求xml报文TX_HEADER节点结构
 * Created by zhanghao on 2017/10/18.
 */

public class RequestTXHeader extends TxHeaderBase {
    public String CLIENT_VERSION = "";
    public String CLIENT_IP = "";
    public String NETWORK_CARD_MAC = "";
    public RequestTXHeader(String tradeCode) {
        super(tradeCode);
    }

    public RequestTXHeader(String trade_code, String client_version, String client_ip, String network_card_mac) {
        super(trade_code);
        this.CLIENT_VERSION = client_version;
        this.CLIENT_IP = client_ip;
        this.NETWORK_CARD_MAC = network_card_mac;
    }
    
    @Override
    public String toString() {
        return "RequestTXHeader{" +
                "TRADE_CODE='" + TRADE_CODE + '\'' +
                ", CLIENT_VERSION='" + CLIENT_VERSION + '\'' +
                ", CLIENT_IP='" + CLIENT_IP + '\'' +
                ", NETWORK_CARD_MAC='" + NETWORK_CARD_MAC + '\'' +
                '}';
    }
}
