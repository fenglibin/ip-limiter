package com.eeefff.limiter.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eeefff.limiter.common.vo.Result;
import com.eeefff.limiter.core.service.ClientService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping(value = "/ip-limiter/client", produces = MediaType.APPLICATION_JSON_VALUE)
public class ClientRegisterController {
	@Autowired
	private ClientService clientRegisterService;

	@ResponseBody
	@RequestMapping("/registerToServer")
	public Result<String> registerToServer() {
		Result<String> result = Result.empty();
		log.info("往ＩＰ限流控制台发起注册");
		boolean registerResult = clientRegisterService.registerToServer();
		if (registerResult) {
			result.setSuccess(true);
			result.setData("往ＩＰ限流控制台发起注册成功.");
			log.info("往ＩＰ限流控制台发起注册成功.");
		} else {
			result.setCode(-1);
			result.setSuccess(false);
			result.setData("往ＩＰ限流控制台发起注册失败！");
			log.info("往ＩＰ限流控制台发起注册失败！");
		}
		return result;
	}
}
