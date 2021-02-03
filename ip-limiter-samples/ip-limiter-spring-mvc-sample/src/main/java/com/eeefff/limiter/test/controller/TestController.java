package com.eeefff.limiter.test.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eeefff.limiter.core.web.interceptor.IpLimiterInterceptor;


/**
 *
 * Created by fenglibin on 2019/01/24.
 */
@Controller
@RequestMapping("/app")
public class TestController {
	
	private IpLimiterInterceptor ipLimiterInterceptor;

    @RequestMapping("/health")
    @ResponseBody
    public String selectAll(){
        return "SUCC";
    }


}
