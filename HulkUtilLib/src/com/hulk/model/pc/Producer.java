package com.hulk.model.pc;

import java.util.Random;

/**
 * 生产者
 * @author zhanghao
 *
 * @param <T>
 */
public abstract class Producer<T> implements Runnable {

	private static final String TAG = "Producer";
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
	
	public Producer(IWarehouse<T> warehouse) {
		this.mWarehouse = warehouse;
	}
	
	@Override
	public void run() {
		SysLog.i(TAG, "run: Stasrting...");
		running = true;
		doRun();
		SysLog.i(TAG, "run: Finished");
		running = false;
	}

	/**
	 * 执行run函数
	 * 死循环服务生产产品
	 */
	private void doRun() {
		while(true) {
			count++;
			try {
				if(isStopped()) {
					SysLog.i(TAG,  "doRun: Stopped, count= " + count);
					return;
				}
				//mWarehouse.get()为阻塞仓库，没有货物是会等待
				T data = doProduce();
				if(data != null) {
					mWarehouse.put(data);
				} else {
					SysLog.e(TAG,  "doRun: Failed produce data.");
				}
				//建议每次睡眠间隔一定时间，避免出现该线程一直占用cup情况
				long sleepTime = sleepTime();
				if (sleepTime > 0) {
					Thread.sleep(sleepTime);
				}
			} catch (InterruptedException e) {
				SysLog.e(TAG, "doRun Interrupted: " + e, e);
			} finally {
				//do something
				doFinal();
			}
		}
	}
	
	/**
	 * 实际完成生产的函数
	 * @return
	 */
	protected abstract T doProduce();
	
	/**
	 * 每次循环睡眠时间， 0表示不睡眠
	 * @return
	 */
	protected long sleepTime() {
		return 5000;
	}
	
	/**
	 * do something in finally
	 */
	protected void doFinal() {
		//do something in finally
	}
	
	public void setStopped(boolean stopped) {
		this.stopped = stopped;
	}
	
	public boolean isStopped() {
		if(onStopped()) {
			return true;
		}
		return this.stopped;
	}
	
	/**
	 * 子类可以动态实现是否需要停止
	 * @return
	 */
	public boolean onStopped() {
		return false;
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
