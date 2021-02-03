package com.eeefff.limiter.dashboard.iplimit;

import java.util.List;

import com.eeefff.limiter.common.vo.IpLimitVO;

/**
 * IP限流处理类
 * 
 * @author fenglibin
 *
 */
public interface IpQpsLimiter {
	/**
	 * 增加IP到IP限流配置中
	 * 
	 * @param ip
	 */
	public IpLimitVO addIpLimit(String appName, String ip, int limit);

	/**
	 * 根据ＩＰ获取IP限流配置，并返回对应的IpLimitVO
	 * 
	 * @param ip
	 * @return
	 */
	public IpLimitVO getIpLimit(String appName, String ip);

	/**
	 * 从IP限流配置中删除指定的ＩＰ的限流配置，如果存在该ＩＰ的限流配置，则返回该ＩＰ原来所代表的IP限流配置对象
	 * 
	 * @param ip
	 * @return
	 */
	public IpLimitVO delIpLimit(String appName, String ip);

	/**
	 * 清除所有的IP限流配置
	 */
	public void cleanAllIpLimits(String appName);

	/**
	 * 获取所有的IP限流配置列表
	 * 
	 * @return
	 */
	public List<IpLimitVO> getAllIpLimits(String appName);

	/**
	 * 获取所有IP的QPS限制配置
	 * 
	 * @return
	 */
	public List<IpLimitVO> updateIpQpsLimit(String appName);

	/**
	 * 设置IP的默认最大的QPS
	 * 
	 * @return
	 */
	public void setDefaultIpMaxQps(String appName, int defaultIpMaxQps);

	/**
	 * 获取IP的默认最大的QPS
	 * 
	 * @return
	 */
	public Integer getDefaultIpMaxQps(String appName);
}
