package com.hulk.byod.parser.xml.msg.base;

public abstract class TxHeaderBase implements ITxHeader {

    //common filed TRADE_CODE
    public String TRADE_CODE = "";

    public TxHeaderBase(String tradeCode) {
        this.TRADE_CODE = tradeCode;
    }

    public String getTradeCode() {
        return this.TRADE_CODE;
    }

    @Override
    public void setTradeCode(String tradeCode) {
        this.TRADE_CODE = tradeCode;
    }
}
