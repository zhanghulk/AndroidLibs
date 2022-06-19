package com.hulk.model.pc;

public class LogConsumer extends Consumer<String> {
	private static final String TAG = "LogConsumer";
	
	private static final int TEST_MAX = 10;

	public LogConsumer(IWarehouse<String> warehouse) {
		super(warehouse);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected boolean doConsume(String data) {
		SysLog.i(TAG, "doConsume: ### Consumed data=" + data + "\n\n");
		return true;
	}

	public boolean onStopped() {
		if(TEST_MAX > 0 && count > TEST_MAX) {
			//SysLog.e(TAG,  "onStopped: count > TEST_MAX， count=" + count);
			return true;
		}
		return false;
	}
}
