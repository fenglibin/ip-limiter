package com.eeeffff.limiter.dashboard.metric;

import java.util.List;

import com.eeeffff.limiter.common.vo.AccessVO;

public interface IMetric {
	/**
	 * 获取指定分钟的访问纬度数据
	 * 
	 * @param appName
	 * @param minute
	 * @return
	 */
	public List<AccessVO> getOneMinuteData(String appName, String minute);

	/**
	 * 获取指定的多个分钟的访问纬度数据
	 * 
	 * @param appName
	 * @param minutes
	 * @return
	 */
	public List<List<AccessVO>> getMultiMinutesData(String appName, List<Object> minutes);

	/**
	 * 获取指定应用、指定IP节点所有可用分钟的访问纬度数据
	 * 
	 * @param appName     应用名称
	 * @param ip          应用的单个节点的IP＋端口
	 * @param lastMinutes 查看最近几分钟的数据
	 * @return
	 */
	public List<List<AccessVO>> getMinutesData(String appName, String ip, int lastMinutes);

	/**
	 * 获取所有可用分钟的访问纬度数据的分钟数据(keys)，不包括具体的访问数据
	 * 
	 * @return
	 */
	public List<Object> getMinuteDataKeys(String appName);
}
