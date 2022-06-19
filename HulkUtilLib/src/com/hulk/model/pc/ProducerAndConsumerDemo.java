package com.hulk.model.pc;

import java.util.LinkedList;

public class ProducerAndConsumerDemo {

	private static final String TAG = "ProducerAndConsumerDemo";

	public static void main(String[] args) {
		IWarehouse warehouse = new LogWarehouse(5, new LinkedList<String>());
		
		//多个线程表示多个生产者
		Thread producer = new Thread(new LogProducer(warehouse), "producer");
		Thread producer2 = new Thread(new LogProducer(warehouse), "producer2");
		Thread producer3 = new Thread(new LogProducer(warehouse), "producer3");
		Thread producer4 = new Thread(new LogProducer(warehouse), "producer4");
		
		//消费者线程
		Thread consumer = new Thread(new LogConsumer(warehouse), "consumer");
		
		startThread(producer, producer2, producer3, producer4, consumer);
		//startThread(producer, producer2, consumer);
		//startThread(producer, consumer);
	}

	/**
	 * 启动线程
	 * @param threads
	 */
	private static void startThread(Thread... threads) {
		if(threads == null) {
			SysLog.e(TAG, "startThread: threads is null");
			return;
		}
		for(Thread t: threads) {
			t.start();
		}
	}
}
