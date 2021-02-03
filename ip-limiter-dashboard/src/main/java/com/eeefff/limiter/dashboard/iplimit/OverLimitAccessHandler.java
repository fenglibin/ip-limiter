package com.eeefff.limiter.dashboard.iplimit;

import java.util.List;

import org.springframework.scheduling.annotation.Async;

import com.eeefff.limiter.common.vo.AccessVO;

public abstract class OverLimitAccessHandler {
	/**
	 * 对传入一段时间访问量最多的IP进行分析，判断其是否有超量访问的IP，然后对其进行处理
	 * 
	 * @param appName             应用名称
	 * @param topAccessMetricList 一段时间访问量最多的IP访问纬度
	 */
	@Async
	public abstract void handleOverLimitAccess(String appName, List<AccessVO> topAccessMetricList);
}
