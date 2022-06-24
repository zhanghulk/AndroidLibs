package com.hulk.model.pc.core;

/**
 * 生产者-消费者 模型接口
 * @author zhanghao
 *
 * @param <T>
 */
public interface IPCModel<T> {

	/**
	 * 获取产品仓库
	 * @return
	 */
	IWarehouse<T> getWarehouse();
	
	/**
	 * 设置产品仓库
	 * @param warehouse
	 */
	void setWarehouse(IWarehouse<T> warehouse);
	
	/**
	 * 启动线程运行
	 */
	void start();
	
	/**
	 * 停止线程运行
	 */
	void stop();
	
	/**
	 * 获取循环次数
	 * @return
	 */
	int getLoopCount();
	
	/**
	 * 线程是否正在运行
	 * @return
	 */
	boolean isRunning();
	
	/**
	 * 设置debug模式
	 * @param debugMode
	 */
	void setDebugMode(boolean debugMode);
}
