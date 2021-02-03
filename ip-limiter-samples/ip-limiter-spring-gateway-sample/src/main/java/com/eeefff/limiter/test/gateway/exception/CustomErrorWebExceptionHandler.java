/*
 * Copyright All Rights Reserved.
 * @author ip-limiter
 * @date  2020-07-23 11:35
 */
package com.eeefff.limiter.test.gateway.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

/**
 * 类或方法的功能描述 :TODO
 *
 * @author ip-limiter
 * @date 2020-07-23 11:35
 */
public class CustomErrorWebExceptionHandler extends DefaultErrorWebExceptionHandler {

    @Autowired
    private GatewayExceptionHandlerAdvice gateWayExceptionHandlerAdvice;

    /**
     * Create a new {@code DefaultErrorWebExceptionHandler} instance.
     *
     * @param errorAttributes    the error attributes
     * @param resourceProperties the resources configuration properties
     * @param errorProperties    the error configuration properties
     * @param applicationContext the current application context
     */
    public CustomErrorWebExceptionHandler(ErrorAttributes errorAttributes, ResourceProperties resourceProperties,
                                          ErrorProperties errorProperties, ApplicationContext applicationContext) {
        super(errorAttributes, resourceProperties, errorProperties, applicationContext);
    }

    
    /***
     * 指定响应处理方法为JSON处理的方法
     *
     * @param errorAttributes errorAttributes 
     * @return org.springframework.web.reactive.function.server.RouterFunction<org.springframework.web.reactive.function.server.ServerResponse>
     * @throws
     * @author ip-limiter
     * @date 2020/8/3:10:58
     */
    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }


    /**
     * 根据code获取对应的HttpStatus 原始的方法是通过status来获取对应的HttpStatus的，
     * 如果我们定义的格式中没有status字段的话，这么就会报错，找不到对应的响应码，
     * 要么返回数据格式中增加status子段，要么重写.
     * 此处不重写
     *
     * @param errorAttributes
     */
    @Override
    protected int getHttpStatus(Map<String, Object> errorAttributes) {
        return (int) errorAttributes.get("status");
    }


    @Override
    protected Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Map<String, Object> error = getErrorAttributes(request, getErrorAttributeOptions(request, MediaType.ALL));
        HttpStatus httpStatus = HttpStatus.valueOf(getHttpStatus(error));
        Throwable throwable = getError(request);
        return ServerResponse.status(httpStatus)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(gateWayExceptionHandlerAdvice.handle(request, throwable)));
    }

 
    /***
     * 构建返回的JSON数据格式
     *
     * @param errorCode errorCode 
     * @param errorMessage errorMessage 
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @throws
     * @author ip-limiter
     * @date 2020/8/24:17:01
     */
    public static Map<String, Object> response(int errorCode, String errorMessage) {
        Map<String, Object> map = new HashMap<>(3);
        map.put("result", 1);
        map.put("msg", errorMessage);
        map.put("status", errorCode);
        return map;
    }


}
