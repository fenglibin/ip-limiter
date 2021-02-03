package com.eeefff.limiter.common.action;

/**
 * 
 * @author fenglibin
 *
 */
@FunctionalInterface
public interface RetryAction {
	/**
	 * 执行指定的任务，并返回执行的结果，执行成功返回true，执行失败返回false
	 * 
	 * @return
	 */
	public boolean doAction();
}
