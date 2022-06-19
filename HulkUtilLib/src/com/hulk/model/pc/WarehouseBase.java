package com.hulk.model.pc;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

/**
 * 日志仓库基类
 * 使用synchronized关键字实现同步锁.
 * @author zhanghao
 *
 * @param <T>
 */
public abstract class WarehouseBase<T> implements IWarehouse<T> {

	private static final String TAG = "WarehouseBase";
	
	/**
	 * 默认一次获取或者存放睡眠时间，避免一直占用CPN导致程序占用套多资源
	 * <p> 如果确实需要调整策略，可以自行在seepTime()函数中返回具体数值，0表示不睡眠。
	 */
	public static final long DEFAULT_ONCE_SLEEP_TIME = 50;
	
	/**
	 * 产品列表
	 */
	protected LinkedList<T> mProducts;
	/**
	 * 最大数量限制
	 */
	protected int max = 10;
	
	private boolean mDebugMode = false;
	
	public WarehouseBase() {
		this.mProducts = new LinkedList<T>();
	}
	
	public WarehouseBase(int max, LinkedList<T> products) {
		this.max = max;
		this.mProducts = products;
	}
	
	@Override
	public void put(T product) {
		synchronized (mProducts) {
			try {
				int count = this.mProducts.size();
				if(count >= max) {
					String thread = getCurrentThreadInfo();
					SysLog.i(TAG, "put: mProducts is full, please wait...thread=" + thread);
					mProducts.wait();
				}
				
				//放入数据
				doPut(product);
				
				//建议每次睡眠间隔一定时间，避免出现该线程一直占用cup情况
				doThreadSleep();
			} catch (InterruptedException e) {
				SysLog.e(TAG, "put Interrupted: " + e, e);
			} finally {
				//产品放完后，通知其他线程释放锁
				mProducts.notify();
			}
		}
	}
	
	@Override
	public T get() {
		synchronized (mProducts) {
			try {
				if(this.mProducts.isEmpty()) {
					String thread = getCurrentThreadInfo();
					SysLog.i(TAG, "get: mProducts is empty, please wait...thread=" + thread);
					mProducts.wait();
				}
				
				//获取数据
				T product = doGet();
				
				//建议每次睡眠间隔一定时间，避免出现该线程一直占用cup情况
				doThreadSleep();
				return product;
			} catch (InterruptedException e) {
				String thread = getCurrentThreadInfo();
				SysLog.e(TAG, "put Interrupted: " + e + ", thread=" + thread, e);
			} finally {
				//产品放完后，通知其他线程释放锁
				mProducts.notify();
			}
		}
		return null;
	}
	
	/**
	 * 子类实现具体的放入数据
	 * @param product
	 */
	protected abstract void doPut(T product);
	
	/**
	 * 子类实现具体的获取数据
	 * @return
	 */
	protected abstract T doGet();
	
	public void setMax(int max) {
		this.max = max;
	}
	
	public int getMax() {
		return this.max;
	}
	
	/**
	 * 添加产品
	 * @param product
	 */
	protected void addProduct(T product) {
		if(mProducts == null) {
			return;
		}
		mProducts.add(product);
	}
	
	/**
	 * 添加产品
	 * @param index
	 * @param product
	 */
	protected void addProduct(int index, T product) {
		if(mProducts == null) {
			return;
		}
		mProducts.add(index, product);
	}
	
	/**
	 * 添加产品列表
	 * @param products
	 */
	protected void addProducts(T... products) {
		if(products == null) {
			return;
		}
		if(mProducts == null) {
			return;
		}
		mProducts.addAll(Arrays.asList(products));
	}
	
	/**
	 * 添加产品集合
	 * @param c
	 */
	protected void addProducts(Collection<? extends T> c) {
		if(c == null) {
			return;
		}
		if(mProducts == null) {
			return;
		}
		mProducts.addAll(c);
	}
	
	/**
	 * 移除并返回最前面一个
	 */
	protected T removeFistProduct() {
		if(mProducts == null) {
			return null;
		}
		return mProducts.removeFirst();
	}
	
	/**
	 * 移除并返回最后一个
	 */
	protected T removeLastProduct() {
		if(mProducts == null) {
			return null;
		}
		return mProducts.removeLast();
	}
	
	protected T getFirstProduct() {
		if(mProducts == null) {
			return null;
		}
		return mProducts.getFirst();
	}
	
	protected T getLastProduct() {
		if(mProducts == null) {
			return null;
		}
		return mProducts.getLast();
	}
	
	protected T getProduct(int index) {
		if(mProducts == null) {
			return null;
		}
		if(index >= mProducts.size()) {
			return null;
		}
		return mProducts.get(index);
	}
	
	protected int productSize() {
		if(mProducts == null) {
			return 0;
		}
		int size = mProducts.size();
		return size;
	}
	
	/**
	 * 建议每次睡眠间隔一定时间，避免出现该线程一直占用cup情况
	 * @throws InterruptedException
	 */
	private void doThreadSleep() throws InterruptedException {
		long sleepTime = onSleepTime();
		if (sleepTime > 0) {
			Thread.sleep(sleepTime);
		}
	}
	
	/**
	 * 每次循环睡眠时间， 0表示不睡眠
	 * <p> 如果确实需要调整策略，可以自行在子类onSleepTime()函数中返回具体数值，0表示不睡眠。
	 * @return
	 */
	protected long onSleepTime() {
		return DEFAULT_ONCE_SLEEP_TIME;
	}
	
	/**
	 * 获取当前线程信息
	 * @return
	 */
	protected String getCurrentThreadInfo() {
		Thread t = Thread.currentThread();
		return t.getName() + "-" + t.getId();
	}
	
	public void setDebugMode(boolean debugMode) {
    	this.mDebugMode = debugMode;
    }
    
    public boolean isDebugMode() {
    	return this.mDebugMode;
    }
    
    protected void logProductInfo(String tag, String func, String product) {
		String thread = getCurrentThreadInfo();
		int size = productSize();
		SysLog.i(tag, func + ": product=" + product + ", size=" + size + ", thread=" + thread);
	}
}
