package com.eeeffff.limiter.core.util;

public class MultiThreadTest {
	public static void main(String[] args) {
		两个不同的字符串做锁();
	}

	/**
	 * 该示例演示两个不相同的字符串用于锁定的情况，表明被synchronized认为不是同一个对象锁
	 */
	public static void 两个不同的字符串做锁() {
		Thread thread1 = new Thread(new TestThread("lock"));
		thread1.setName("thread1");
		Thread thread2 = new Thread(new TestThread("lock2"));
		thread2.setName("thread2");
		thread1.start();
		thread2.start();
	}

	/**
	 * 该示例演示相同字符用于锁定的情况，表明被synchronized认为是同一个对象锁
	 */
	public static void 同一个字符串用做锁() {
		String lock = "lock";
		Thread thread1 = new Thread(new TestThread(lock));
		thread1.setName("thread1");
		Thread thread2 = new Thread(new TestThread(lock));
		thread2.setName("thread2");
		thread1.start();
		thread2.start();
	}

	/**
	 * 该示例演示两个值相同但对象不同的两个字符用于锁定的情况，表明被synchronized认为是同一个对象锁
	 */
	public static void 两个相同的字符串做锁() {
		Thread thread1 = new Thread(new TestThread("lock"));
		thread1.setName("thread1");
		Thread thread2 = new Thread(new TestThread("lock"));
		thread2.setName("thread2");
		thread1.start();
		thread2.start();
	}

	/**
	 * 该示例演示两个值相同但对象不同的两个字符用于锁定的情况，表明被synchronized认为是同一个对象锁
	 */
	public static void 两个相同的字符串做锁_2() {
		String lock1 = "lock";
		Thread thread1 = new Thread(new TestThread(lock1));
		thread1.setName("thread1");
		String lock2 = "lock";
		Thread thread2 = new Thread(new TestThread(lock2));
		thread2.setName("thread2");
		thread1.start();
		thread2.start();
	}

}

class TestThread extends Thread {
	private String lock = null;

	public TestThread(String lock) {
		this.lock = lock;
	}

	public void run() {
		String _lock = new StringBuilder(lock).append("-lock").toString();
		synchronized (_lock) {
			System.out.println(System.currentTimeMillis() + "==" + Thread.currentThread().getName());
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
}
