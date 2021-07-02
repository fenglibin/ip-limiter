/*
 * Copyright All Rights Reserved.
 * @author ip-limiter
 * @date  2020-07-31 17:35
 */
package com.eeeffff.limiter.test.gateway.filter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;

import com.eeeffff.limiter.test.gateway.common.GatewayConstant;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 最高优先级复制请求的body后续使用
 *
 * @author ip-limiter
 * @date 2020-07-31 17:35
 */
@Component
public class AccessBodyFilter implements GlobalFilter, Ordered {
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerRequest serverRequest = ServerRequest.create(exchange, HandlerStrategies.withDefaults().messageReaders());
		// mediaType
		MediaType mediaType = exchange.getRequest().getHeaders().getContentType();

		String method = exchange.getRequest().getMethodValue();
		if (HttpMethod.POST.name().equalsIgnoreCase(method)) {

			if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(mediaType)) {
				// 此处获取原参数的boundary，
				// String boundary = body.substring(28, 52);

			}
			if (MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)) {
				Mono<Object> modifiedBody = serverRequest.bodyToMono(Object.class).flatMap(body -> {
					exchange.getAttributes().put(GatewayConstant.CACHE_REQUEST_BODY, body);
					return Mono.just(body);
				});
				return modifyRequestBodyGateway(exchange, chain, Object.class, modifiedBody);
			} else if (MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(mediaType)) {
				Mono<String> modifiedBody = serverRequest.bodyToMono(String.class).flatMap(body -> {
					// origin body map
					Map<String, Object> bodyMap = decodeBody(body);
					exchange.getAttributes().put(GatewayConstant.CACHE_REQUEST_BODY, bodyMap);
					// encodeBody(newBodyMap);
					return Mono.just(body);
				});

				return modifyRequestBodyGateway(exchange, chain, String.class, modifiedBody);
			}
		} else if (HttpMethod.GET.name().equalsIgnoreCase(method)) {

			exchange.getAttributes().put(GatewayConstant.CACHE_REQUEST_BODY, exchange.getRequest().getQueryParams());
			return chain.filter(exchange);
		}

		return chain.filter(exchange.mutate().request(exchange.getRequest()).build());

	}

	/***
	 * 参照 ModifyRequestBodyGatewayFilterFactory.java 截取的方法
	 *
	 * @param exchange     exchange
	 * @param chain        chain
	 * @param outClass     outClass
	 * @param modifiedBody modifiedBody
	 * @return reactor.core.publisher.Mono<java.lang.Void>
	 * @throws @author ip-limiter
	 * @date 2020/8/25:17:51
	 */
	private Mono<Void> modifyRequestBodyGateway(ServerWebExchange exchange, GatewayFilterChain chain, Class outClass,
			Mono<?> modifiedBody) {
		BodyInserter bodyInserter = BodyInserters.fromPublisher(modifiedBody, outClass);
		HttpHeaders headers = new HttpHeaders();
		headers.putAll(exchange.getRequest().getHeaders());

		// the new content type will be computed by bodyInserter
		// and then set in the request decorator
		headers.remove(HttpHeaders.CONTENT_LENGTH);
		CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(exchange, headers);
		return bodyInserter.insert(outputMessage, new BodyInserterContext())
				// .log("modify_request", Level.INFO)
				.then(Mono.defer(() -> {
					ServerHttpRequestDecorator decorator = new ServerHttpRequestDecorator(exchange.getRequest()) {
						@Override
						public HttpHeaders getHeaders() {
							long contentLength = headers.getContentLength();
							HttpHeaders httpHeaders = new HttpHeaders();
							httpHeaders.putAll(super.getHeaders());
							if (contentLength > 0) {
								httpHeaders.setContentLength(contentLength);
							} else {
								// TODO: this causes a 'HTTP/1.1 411 Length Required' on httpbin.org
								httpHeaders.set(HttpHeaders.TRANSFER_ENCODING, "chunked");
							}
							return httpHeaders;
						}

						@Override
						public Flux<DataBuffer> getBody() {
							return outputMessage.getBody();
						}
					};
					return chain.filter(exchange.mutate().request(decorator).build());
				}));
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

	private Map<String, Object> decodeBody(String body) {

		// key 重复取第一个记录日志
		return Arrays.stream(body.split("&")).map(s -> s.split("="))
				.collect(Collectors.toMap(arr -> arr[0], arr -> arr.length > 1 ? arr[1] : "", (arr1, arr2) -> arr1));
	}

	private String encodeBody(Map<String, Object> map) {
		return map.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining("&"));
	}

}