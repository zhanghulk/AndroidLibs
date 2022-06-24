package com.hulk.model.pc.core;

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
	 * 默认最大缓冲区数量大小
	 */
	public static final int MAX_PRODUST_BUFFER_SIZE = 10;
	
	/**
	 * 产品列表
	 */
	protected LinkedList<T> mProducts;
	/**
	 * 最大数量限制
	 */
	protected int max = MAX_PRODUST_BUFFER_SIZE;
	
	private boolean mDebugMode = false;
	
	private boolean mShowInfoMode = false;
	
	public WarehouseBase() {
		this.mProducts = new LinkedList<T>();
	}
	
	public WarehouseBase(int max, LinkedList<T> products) {
		this.max = max;
		this.mProducts = products;
	}
	
	/**
	 * 存放产品
	 * <p>生产者调用，如果缓存列表的产品数量超了最大值，该生产者线程就处于等待状态.
	 */
	@Override
	public void put(T product) {
		synchronized (mProducts) {
			try {
				int size = this.mProducts.size();
				if(isShowInfoMode()) {
					SysLog.i(TAG, "put: products size=" + size + ", max=" + max);
				}
				if(size >= max) {
					if(isDebugMode()) {
						String thread = getCurrentThreadInfo();
						SysLog.w(TAG, "put: ## products is full, please wait...thread=" + thread);
					}
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
	
	/**
	 * 获取产品
	 * <p>消费者调用，如果仓库缓存列表的产品数量为空，该消费者线程就处于等待状态.
	 */
	@Override
	public T get() {
		synchronized (mProducts) {
			try {
				int size = this.mProducts.size();
				if(isShowInfoMode()) {
					SysLog.i(TAG, "get: Current mProducts size=" + size);
				}
				if(this.mProducts.isEmpty()) {
					if(isDebugMode()) {
						String thread = getCurrentThreadInfo();
						SysLog.w(TAG, "get: ## products is empty, please wait...thread=" + thread);
					}
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
	 * 执行具体的放入数据
	 * @param product
	 */
	protected void doPut(T product) {
		boolean put = onPut(product);
		if(!put) {
			//子类没有执行加入，自定加入队列
			addProduct(product);
			if(isShowInfoMode()) {
				SysLog.i(TAG, "doPut: add product=" + product);
			}
		}
	}
	
	/**
	 * 放入数据
	 * 子类实现具体的放入数据
	 * @param product
	 * @return 如果完全接管放入函数，则返回true，不执行默认添加功能.
	 */
	protected abstract boolean onPut(T product);
	
	/**
	 * 执行具体的获取数据
	 * @return
	 */
	protected T doGet() {
		T product = onGet();
		if(product == null) {
			//子类没有执行具体获取产品，自动获取第一个
			product = removeFistProduct();
			if(isShowInfoMode()) {
				SysLog.i(TAG, "doGet: onGet result is null, remove fist product " + product);
			}
		}
		return product;
	}
	
	/**
	 * 子类实现具体的获取数据
	 * @return
	 */
	protected abstract T onGet();
	
	public void setMax(int max) {
		this.max = max;
	}
	
	public int getMax() {
		return this.max;
	}
	
	private void ensureProductsAvailable() {
		if(mProducts == null) {
			throw new IllegalStateException("mProducts is null, please create it in constructors");
		}
	}
	
	public boolean isProductsEmpty() {
		return mProducts == null || mProducts.isEmpty();
	}
	
	/**
	 * 添加产品
	 * @param product
	 */
	protected void addProduct(T product) {
		ensureProductsAvailable();
		mProducts.add(product);
	}
	
	/**
	 * 添加产品
	 * @param index
	 * @param product
	 */
	protected void addProduct(int index, T product) {
		ensureProductsAvailable();
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
		ensureProductsAvailable();
		mProducts.addAll(Arrays.asList(products));
	}
	
	/**
	 * 添加产品集合
	 * @param c
	 */
	protected void addProducts(Collection<? extends T> c) {
		if(c == null || c.isEmpty()) {
			return;
		}
		ensureProductsAvailable();
		mProducts.addAll(c);
	}
	
	/**
	 * 移除并返回最前面一个
	 */
	protected T removeFistProduct() {
		if(isProductsEmpty()) {
			return null;
		}
		return mProducts.removeFirst();
	}
	
	/**
	 * 移除并返回最后一个
	 */
	protected T removeLastProduct() {
		if(isProductsEmpty()) {
			return null;
		}
		return mProducts.removeLast();
	}
	
	protected T getFirstProduct() {
		if(isProductsEmpty()) {
			return null;
		}
		return mProducts.getFirst();
	}
	
	protected T getLastProduct() {
		if(isProductsEmpty()) {
			return null;
		}
		return mProducts.getLast();
	}
	
	protected T getProduct(int index) {
		if(isProductsEmpty()) {
			return null;
		}
		if(index >= mProducts.size()) {
			return null;
		}
		return mProducts.get(index);
	}
	
	protected int getProductSize() {
		if(mProducts == null) {
			return 0;
		}
		int size = mProducts.size();
		return size;
	}
	
	protected int size() {
		return getProductSize();
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
		return SysLog.getCurrentThreadInfo();
	}
	
	public void setDebugMode(boolean debugMode) {
    	this.mDebugMode = debugMode;
    }
    
    public boolean isDebugMode() {
    	return this.mDebugMode;
    }
    
    public void setShowInfoMode(boolean showInfoMode) {
    	this.mShowInfoMode = showInfoMode;
    }
    
    public boolean isShowInfoMode() {
    	return this.mShowInfoMode;
    }
    
    protected void logProductInfo(String tag, String func, String product) {
		String thread = getCurrentThreadInfo();
		int size = getProductSize();
		SysLog.i(tag, func + ": product=" + product + ", size=" + size + ", thread=" + thread);
	}
}
