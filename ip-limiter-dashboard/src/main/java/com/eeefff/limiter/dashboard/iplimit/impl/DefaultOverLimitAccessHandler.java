package com.eeefff.limiter.dashboard.iplimit.impl;

import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.eeefff.limiter.common.vo.AccessVO;
import com.eeefff.limiter.dashboard.iplimit.OverLimitAccessHandler;

@Service(value = "defaultOverLimitAccessHandler")
public class DefaultOverLimitAccessHandler extends OverLimitAccessHandler {

	@Override
	@Async
	public void handleOverLimitAccess(String appName, List<AccessVO> topAccessMetricList) {
		// 什么都不做

	}

}
