package com.hulk.byod.parser.xml.msg.base;

public interface ITxHeader {
	/**
	 * 业务交易码
	 * @return
	 */
	String getTradeCode();

	void setTradeCode(String tradeCode);
}
