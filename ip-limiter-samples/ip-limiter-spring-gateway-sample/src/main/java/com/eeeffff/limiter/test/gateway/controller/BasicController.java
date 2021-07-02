/*
 * Copyright All Rights Reserved.
 * @author ip-limiter
 * @date  2020-08-04 17:50
 */
package com.eeeffff.limiter.test.gateway.controller;

import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

/**
 * 类或方法的功能描述 :TODO
 *
 * @author ip-limiter
 * @date 2020-08-04 17:50
 */
@RestController
public class BasicController {

    private static Logger logger = LoggerFactory.getLogger(BasicController.class);
    
    @GetMapping("/hello")
    public Mono<String> sayHelloWorld() {
        String  traceId = TraceContext.traceId();
        logger.info(traceId);
        return Mono.just(traceId);
    }

    @GetMapping("/test")
    public String test() {
        return "test";
    }
}