package com.eeeffff.limiter.dashboard.metric;

import java.util.List;

import com.eeeffff.limiter.common.vo.AccessVO;

/**
 * 针对IP访问数据的处理
 * 
 * @author fenglibin
 *
 */
public interface AccessMetricHandler {
	/**
	 * 处理应用这一段时间的访问纬度数据，纬度数据可能是按秒访问统计的纬度数据，也可能是按分访问统计的纬度数据。
	 * 
	 * @param appName             应用的名称
	 * @param ipPort              应用某个节点的IP
	 * @param topAccessMetricList 应用这一段时间的一些访问量最多的IP访问统计纬度数据
	 */
	public void handleAccessMetric(String appName, String ipPort, List<AccessVO> topAccessMetricList);
}
