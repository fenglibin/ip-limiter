package com.eeefff.limiter.dashboard.metric.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.eeefff.limiter.common.vo.AccessVO;

/**
 * 处理当前应用以秒为纬度统计的统计，与当前应用的其它节点的数据进行汇总统计
 * 
 * @author fenglibin
 *
 */
@Service(value = "secondsAccessMetricHandler")
public class SecondsAccessMetricHandler extends AbstraceSecondsAccessMetricHandler {

	@Override
	public void handleAccessMetric(String appName, String ipPort, List<AccessVO> topAccessMetricList) {
		// @TODO
	}

}
