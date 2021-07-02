package com.eeeffff.limiter.common.action;

import com.eeeffff.limiter.common.vo.Result;

public interface HttpAction {
	/**
	 * 对HTTP响应结果执行一些操作处理
	 * 
	 * @param result
	 */
	public void doAction(Result<?> result);
}
