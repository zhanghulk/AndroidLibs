package com.hulk.model.pc;

import java.util.Random;

public class LogProducer extends Producer<String> {
	
	private static final String TAG = "LogProducer";
	private static final int TEST_MAX = 100;
	
	protected Random random;

	public LogProducer(IWarehouse<String> warehouse) {
		super(warehouse);
		random = new Random();
	}
	
	@Override
	protected String doProduce() {
		int randomInt = random.nextInt(10) * 1000;
		String product = "log-" + count + ", randomInt=" + randomInt;
		SysLog.i(TAG, "doProduce: product=" + product);
		return product;
	}
	
	/**
	 * 每次循环睡眠时间， 0表示不睡眠
	 * @return
	 */
	protected long sleepTime() {
		int randomTime = random.nextInt(10) * 1000;
		return randomTime;
	}
	
	public boolean onStopped() {
		if(TEST_MAX > 0 && count > TEST_MAX) {
			SysLog.e(TAG,  "doRun: count > TEST_MAX， count=" + count);
			return true;
		}
		return false;
	}
}
