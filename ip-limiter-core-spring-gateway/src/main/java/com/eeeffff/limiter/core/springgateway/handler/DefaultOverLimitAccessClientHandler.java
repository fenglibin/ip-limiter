package com.eeeffff.limiter.core.springgateway.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;

/**
 * 默认的超量ＩＰ访问处理类
 * 
 * @author fenglibin
 *
 */
public class DefaultOverLimitAccessClientHandler extends OverLimitAccessClientHandler {

	@Override
	public boolean handleOverLimitAccess(ServerHttpRequest request, ServerHttpResponse response, String ip,
			String accessUri) {
		response.setStatusCode(HttpStatus.FORBIDDEN);
		return false;
	}

}
