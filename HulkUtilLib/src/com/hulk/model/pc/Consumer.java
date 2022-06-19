package com.hulk.model.pc;

public abstract class Consumer<T> implements Runnable {

	private static final String TAG = "Consumer";
	/**
	 * 产品仓库
	 */
	protected IWarehouse<T> mWarehouse;
	/**
	 * 是否正在运行
	 */
	protected boolean running = false;
	
	/**
	 * 是否被外界设置为停止的
	 */
	protected boolean stopped = false;
	
	/**
	 * 循环工作次数
	 */
	protected int count = 0;
	
	private boolean mDebugMode = false;
	
	public Consumer(IWarehouse<T> warehouse) {
		this.mWarehouse = warehouse;
	}
	
	@Override
	public void run() {
		SysLog.i(TAG, "run: Stasrting...");
		running = true;
		doRun();
		SysLog.e(TAG, "run: Finished");
		running = false;
	}
	
	/**
	 * 执行run函数
	 */
	private void doRun() {
		//死循环服务消费产品
		while(true) {
			count++;
			try {
				if(isStopped()) {
					SysLog.i(TAG,  "doRun: Stopped, count= " + count);
					return;
				}
				//mWarehouse.get()为阻塞仓库，没有货物是会等待
				T data = mWarehouse.get();
				boolean consumed = doConsume(data);
				if(!consumed && isDebugMode()) {
					SysLog.e(TAG,  "doRun: Failed consume data=" + data);
				}
				
				//建议每次睡眠间隔一定时间，避免出现该线程一直占用cup情况
				long sleepTime = sleepTime();
				if (sleepTime > 0) {
					Thread.sleep(sleepTime);
				}
			} catch (InterruptedException e) {
				SysLog.e(TAG, "doRun Interrupted: " + e);
			} finally {
				//do something
				doFinal();
			}
		}
	}
	
	/**
	 * 实际完成消费的函数
	 * @param data
	 * @return 是否消费完成
	 */
	protected abstract boolean doConsume(T data);
	
	/**
	 * 每次循环睡眠时间， 0表示不睡眠
	 * @return
	 */
	protected long sleepTime() {
		return 50;
	}
	
	protected void doFinal() {
		//do something in finally
	}
	
	public void setStopped(boolean stopped) {
		this.stopped = stopped;
	}
	
	/**
	 * 子类可以动态实现是否需要停止
	 * @return
	 */
	public boolean onStopped() {
		return false;
	}
	
	public boolean isStopped() {
		if(onStopped()) {
			return true;
		}
		return this.stopped;
	}
	
	public void stopp() {
		this.stopped = true;
	}
	
	public boolean isRunning() {
		return this.running;
	}
	
	public void setDebugMode(boolean debugMode) {
    	this.mDebugMode = debugMode;
    }
    
    public boolean isDebugMode() {
    	return this.mDebugMode;
    }
}
