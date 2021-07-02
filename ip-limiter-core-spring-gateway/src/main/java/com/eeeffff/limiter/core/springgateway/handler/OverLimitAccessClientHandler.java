package com.eeeffff.limiter.core.springgateway.handler;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;

/**
 * 对访问超量的ＩＰ的处理类
 * 
 * @author fenglibin
 *
 */
public abstract class OverLimitAccessClientHandler {
	private static OverLimitAccessClientHandler overLimitAccessHandler = new DefaultOverLimitAccessClientHandler();

	public static OverLimitAccessClientHandler getOverLimitAccessHandler() {
		return overLimitAccessHandler;
	}

	public static void setOverLimitAccessHandler(OverLimitAccessClientHandler overLimitAccessHandler) {
		OverLimitAccessClientHandler.overLimitAccessHandler = overLimitAccessHandler;
	}

	/**
	 * 对访问超量的ＩＰ进行处理
	 * 
	 * @param request   当前请求的request
	 * @param response  当前请求的响应
	 * @param ip        当前请求来源的ＩＰ地址
	 * @param accessUri 当前请求的ＵＲＩ
	 * @return 是否允许访问（返回true）系统，还是拒绝访问（返回false）
	 */
	public abstract boolean handleOverLimitAccess(ServerHttpRequest request, ServerHttpResponse response, String ip,
			String accessUri);
}
