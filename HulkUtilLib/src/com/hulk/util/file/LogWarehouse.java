package com.hulk.model.pc;

import java.util.LinkedList;

/**
 * 日志仓库
 * 使用synchronized关键字实现同步锁.
 * @author zhanghao
 *
 * @param <T>
 */
public class LogWarehouse extends WarehouseBase<String> {

	private static final String TAG = "LogWarehouse";
	
	public LogWarehouse() {
		super();
	}
	
	public LogWarehouse(int max, LinkedList<String> products) {
		super(max, products);
	}
	
	/**
	 * 具体放入数据
	 */
	protected void doPut(String product) {
		addProduct(product);
		logProductInfo(TAG, "doPut", product);
	}
	
	/**
	 * 具体获取数据
	 */
	protected String doGet() {
		String product = removeFistProduct();
		logProductInfo(TAG, "doGet", product);
		return product;
	}
}
