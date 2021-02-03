package com.eeefff.limiter.dashboard.metric.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.eeefff.limiter.common.vo.AccessVO;

/**
 * 处理所有以秒为纬度统计的统计，与所有应用的以秒为统计纬度的数据进行汇总统计
 * 
 * @author fenglibin
 *
 */
@Service(value = "globalSecondsAccessMetricHandler")
public class GlobalSecondsAccessMetricHandler extends AbstraceSecondsAccessMetricHandler {

	@Override
	public void handleAccessMetric(String appName, String ipPort, List<AccessVO> topAccessMetricList) {
		// @TODO
	}

}
