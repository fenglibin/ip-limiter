package com.eeeffff.limiter.dashboard.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eeeffff.limiter.dashboard.vo.Status;

/**
 * @author fenglibin
 */
@Controller
@RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
public class HealthController {


	@ResponseBody
	@RequestMapping("actuator/health")
	public Status health() {
		return new Status();
	}
	
	@ResponseBody
	@RequestMapping("app/health")
	public Status appHhealth() {
		return health();
	}

}
