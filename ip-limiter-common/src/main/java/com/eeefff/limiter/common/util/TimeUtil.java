package com.eeefff.limiter.common.util;

import java.util.concurrent.TimeUnit;

/**
 * Provides millisecond-level time of OS.
 *
 * @author fenglibin
 */
public final class TimeUtil {

	private static volatile long currentTimeMillis = System.currentTimeMillis();
	private static volatile long currentTimeSenconds = currentTimeMillis / 1000;
	private static volatile long currentTimeMinutes = currentTimeMillis / 1000 / 60;

	static {
		currentTimeMillis = System.currentTimeMillis();
		Thread daemon = new Thread(new Runnable() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void run() {
				while (true) {
					currentTimeMillis = System.currentTimeMillis();
					currentTimeSenconds = currentTimeMillis / 1000;
					currentTimeMinutes = currentTimeMillis / 1000 / 60;
					try {
						TimeUnit.MILLISECONDS.sleep(1);
					} catch (Throwable e) {

					}
				}
			}
		});
		daemon.setDaemon(true);
		daemon.setName("ip-limiter-time-tick-thread");
		daemon.start();
	}

	/**
	 * 获取当前毫秒数
	 * 
	 * @return
	 */
	public static long currentTimeMillis() {
		return currentTimeMillis;
	}

	/**
	 * 获取当前秒数
	 * 
	 * @return
	 */
	public static long currentTimeSeconds() {
		return currentTimeSenconds;
	}

	/**
	 * 获取当前分钟数
	 * 
	 * @return
	 */
	public static long currentTimeMinutes() {
		return currentTimeMinutes;
	}
}
