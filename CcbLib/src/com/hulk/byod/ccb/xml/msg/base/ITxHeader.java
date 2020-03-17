package com.hulk.byod.ccb.xml.msg.base;

public interface ITxHeader {
	/**
	 * 业务交易码
	 * @return
	 */
	String getTradeCode();

	void setTradeCode(String tradeCode);
}
