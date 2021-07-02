package com.eeeffff.limiter.core.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eeeffff.limiter.common.vo.Result;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping(value = "/ip-limiter/health", produces = MediaType.APPLICATION_JSON_VALUE)
public class HealthCheckController {
	@ResponseBody
	@RequestMapping("/check")
	public Result<String> healthCheck() {
		log.debug("健康检查接口被调用.");
		return Result.ofSuccess("SUCC");
	}
}
