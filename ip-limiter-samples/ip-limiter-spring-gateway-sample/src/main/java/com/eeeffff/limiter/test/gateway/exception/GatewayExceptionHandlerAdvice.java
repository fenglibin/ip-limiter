/*
 * Copyright All Rights Reserved.
 * @author ip-limiter
 * @date  2020-07-23 10:25
 */
package com.eeeffff.limiter.test.gateway.exception;

import java.net.ConnectException;
import java.security.SignatureException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.cloud.gateway.support.TimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;

import com.eeeffff.limiter.test.gateway.vo.ApiResponses;

import io.netty.channel.ConnectTimeoutException;

/**
 * 类或方法的功能描述 :TODO
 *
 * @author ip-limiter
 * @date 2020-07-23 10:25
 */

@Component
public class GatewayExceptionHandlerAdvice {

    private static Logger log = LoggerFactory.getLogger(GatewayExceptionHandlerAdvice.class);

    @ExceptionHandler(value = {ResponseStatusException.class})
    public ApiResponses handle(ServerRequest request, ResponseStatusException ex) {
        String requestUri = request.exchange().getRequest().getPath().toString();
        log.error("response status exception:{} - {}", ex.getMessage(), requestUri);
        if (ex.getStatus().equals(HttpStatus.NOT_FOUND)) {
            return ApiResponses.failure(HttpStatus.NOT_FOUND.value(), requestUri);
        }else if(ex.getStatus().equals(HttpStatus.GATEWAY_TIMEOUT)){
            return ApiResponses.failure(HttpStatus.GATEWAY_TIMEOUT.value(), ex.getReason());
        }
        return ApiResponses.failure(0, "SystemErrorType.GATEWAY_ERROR");
    }


    @ExceptionHandler(value = {ConnectException.class})
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ApiResponses handle(ServerRequest request, ConnectException ex) {
        String requestUri = request.exchange().getRequest().getPath().toString();
        log.error("response status exception:{} - {}", ex.getMessage(), requestUri);
        return ApiResponses.failure(HttpStatus.SERVICE_UNAVAILABLE.value(), requestUri);
    }

    @ExceptionHandler(value = {ConnectTimeoutException.class})
    public ApiResponses handle(ConnectTimeoutException ex) {
        log.error("connect timeout exception:{}", ex.getMessage());
        return ApiResponses.failure(0, "SystemErrorType.GATEWAY_CONNECT_TIME_OUT");
    }

    @ExceptionHandler(value = {TimeoutException.class})
    public ApiResponses handle(TimeoutException ex) {
        log.error("connect timeout exception:{}", ex.getMessage());
        return ApiResponses.failure(0, "SystemErrorType.GATEWAY_CONNECT_TIME_OUT");
    }

    @ExceptionHandler(value = {NotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponses handle(NotFoundException ex) {
        log.error("not found exception:{}", ex.getMessage());
        return ApiResponses.failure(0, "SystemErrorType.GATEWAY_NOT_FOUND_SERVICE");
    }


    @ExceptionHandler(value = {SignatureException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponses handle(SignatureException ex) {
        log.error("SignatureException:{}", ex.getMessage());
        return ApiResponses.failure(0, "SystemErrorType.INVALID_TOKEN");
    }


    @ExceptionHandler(value = {RuntimeException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponses handle(RuntimeException ex) {
        log.error("runtime exception:{}", ex.getMessage());
        return ApiResponses.failure();
    }

    @ExceptionHandler(value = {Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponses handle(Exception ex) {
        log.error("exception:{}", ex.getMessage());
        return ApiResponses.failure();
    }

    @ExceptionHandler(value = {Throwable.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponses handle(ServerRequest request, Throwable throwable) {
        ApiResponses apiResponses = new ApiResponses();

        if (throwable instanceof ConnectException) {

            apiResponses = handle(request, (ConnectException) throwable);
            
        } else if (throwable instanceof ResponseStatusException) {

            apiResponses = handle(request, (ResponseStatusException) throwable);
            
        } else if (throwable instanceof ConnectTimeoutException) {

            apiResponses = handle((ConnectTimeoutException) throwable);
            
        } else if (throwable instanceof NotFoundException) {

            apiResponses = handle((NotFoundException) throwable);
            
        } else if (throwable instanceof RuntimeException) {

            apiResponses = handle((RuntimeException) throwable);
            
        } else if (throwable instanceof Exception) {

            apiResponses = handle((Exception) throwable);
            
        }
        return apiResponses;
    }
}
