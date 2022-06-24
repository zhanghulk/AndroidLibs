package com.hulk.util.file;

import com.hulk.model.pc.Consumer;
import com.hulk.model.pc.IWarehouse;
import com.hulk.model.pc.LogConsumer;
import com.hulk.model.pc.SysLog;

/**
 * 日志打印消费者实现类
 * <p>消费者实际消费： 把日志信息写到文件里面
 * @author zhanghao
 *
 */
class LogFileConsumer extends Consumer<String> {

	private static final String TAG = "LogFileConsumer";
	
	TxtFile mTxtFile = null;
	boolean mAppend = true;

	public LogFileConsumer(IWarehouse<String> warehouse, TxtFile txtFile) {
		super(warehouse);
		this.mTxtFile = txtFile;
	}

	public void setAppend(boolean append) {
		mAppend = append;
	}
	
	/**
	 * 消费者实际消费： 把日志信息写到文件里面
	 */
	@Override
	protected boolean doConsume(String data) {
		//SysLog.i(TAG, "doConsume: ### Consumed data=" + data + "\n\n");
		boolean written = writeToFile(data);
        return written;
	}
	
	protected boolean writeToFile(String text) {
		if(mTxtFile == null) {
    		//为空说明之前初始化时已经失败，此处没不要重复创建，有些设备会耗时过长，出现ANR
			if(isDebugMode()) {
				SysLog.e(TAG, "writeToFile: mTxtFile is null");
			}
    		return false;
    	}
		boolean written = false;
		try {
			written = mTxtFile.write(text, mAppend);
		} catch (Throwable e) {
			if(isDebugMode()) {
				SysLog.e(TAG, "writeToFile Failed:" + e);
			}
		}
        return written;
	}
}
