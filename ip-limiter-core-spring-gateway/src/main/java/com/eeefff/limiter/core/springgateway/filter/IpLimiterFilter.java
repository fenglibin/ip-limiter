package com.eeefff.limiter.core.springgateway.filter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import com.eeefff.limiter.core.config.SystemEnv;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class IpLimiterFilter implements GlobalFilter, Ordered {
	@Autowired
	private IpLimiterFilterHandler ipLimiterHandlerFilter;

	public IpLimiterFilter() {
		log.info("IpLimiterFilter 初使化成功.");
	}

	@Override
	public int getOrder() {
		return -999;
	}

	Mono<Void> filter(ServerWebExchange exchange) {
		ServerHttpRequest request = exchange.getRequest();
		String _appName = request.getQueryParams().getFirst("appName");
		// 应用指定IP节点
		String appIp = request.getQueryParams().getFirst("ip");
		if (StringUtils.isEmpty(_appName)) {
			_appName = SystemEnv.getAppName();
		}
		// 这两个属性是给FreeMarker在页面上引用的属性
		exchange.getAttributes().put("appName", _appName);
		if (appIp != null) {
			exchange.getAttributes().put("ip", appIp);
		}
		ServerHttpResponse response = exchange.getResponse();
		boolean result = ipLimiterHandlerFilter.preHandle(request, response);
		if (!result) {
			DataBuffer buffer = response.bufferFactory().wrap(HttpStatus.FORBIDDEN.getReasonPhrase().getBytes());
			return response.writeWith(Mono.just(buffer));
		}
		return null;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		Mono<Void> mono = filter(exchange);
		if (mono != null) {
			return mono;
		}
		return chain.filter(exchange);
	}

	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		Mono<Void> mono = filter(exchange);
		if (mono != null) {
			return mono;
		}
		return chain.filter(exchange);
	}

}
