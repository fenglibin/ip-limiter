package com.eeeffff.limiter.common.util;

import com.eeeffff.limiter.common.action.RetryAction;

/**
 * 对任务执行失败的任务进行重试的帮助类
 * 
 * @author fenglibin
 *
 */
public class RetryHelper {
	/**
	 * 对任务进行重试，执行结束有以下三种情况：<br>
	 * １、任务执行成功；<br>
	 * ２、任务执行执行了未捕获的异常；<br>
	 * ３、任务执行次数达到了最大重试次数；<br>
	 * 默认最大的重试次数为5次，任务执行失败后默认的间隔时间为10ms
	 * 
	 * @param retryAction 待执行的任务
	 */
	public static boolean doRetryAction(RetryAction retryAction) {
		int maxRetryTimes = 5;
		int sleepTime = 10;
		return doRetryAction(retryAction, maxRetryTimes, sleepTime);
	}

	/**
	 * 对任务进行重试，执行结束有以下三种情况：<br>
	 * １、任务执行成功；<br>
	 * ２、任务执行执行了未捕获的异常；<br>
	 * ３、任务执行次数达到了最大重试次数；<br>
	 * 任务执行失败后默认的间隔时间为10ms
	 * 
	 * @param retryAction   待执行的任务
	 * @param maxRetryTimes 最大的重试次数
	 */
	public static boolean doRetryAction(RetryAction retryAction, int maxRetryTimes) {
		int sleepTime = 10;
		return doRetryAction(retryAction, maxRetryTimes, sleepTime);
	}

	/**
	 * 对任务进行重试，执行结束有以下三种情况：<br>
	 * １、任务执行成功；<br>
	 * ２、任务执行执行了未捕获的异常；<br>
	 * ３、任务执行次数达到了最大重试次数；<br>
	 * 
	 * @param retryAction   待执行的任务
	 * @param maxRetryTimes 最大的重试次数
	 * @param sleepTime     每次重试的间隔时间，单位为毫秒
	 */
	public static boolean doRetryAction(RetryAction retryAction, int maxRetryTimes, int sleepTime) {
		int tryTimes = 1;
		boolean result = false;
		while (tryTimes <= maxRetryTimes) {
			result = retryAction.doAction();
			// 如果执行成功也直接退出
			if (result) {
				break;
			}
			try {
				if (sleepTime > 0) {
					Thread.sleep(sleepTime);
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			tryTimes++;
		}
		return result;
	}
}
