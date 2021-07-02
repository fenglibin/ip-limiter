package com.eeeffff.limiter.dashboard.iplimit;

import java.util.List;

import com.eeeffff.limiter.common.vo.BlackIpVO;

/**
 * IP黑名单处理类
 * 
 * @author fenglibin
 *
 */
public interface BlackIpLimiter {
	/**
	 * 增加IP到黑名单中
	 * 
	 * @param appName 应用名称
	 * @param ip      IP地址
	 */
	public BlackIpVO addBlackIp(String appName, String ip);

	/**
	 * 更新IP黑名称中该IP的信息
	 * 
	 * @param appName 应用名称
	 * @param ip      IP地址
	 * @param blackIp
	 * @return
	 */
	public BlackIpVO updateBlackIp(String appName, String ip, BlackIpVO blackIp);

	/**
	 * 根据ＩＰ获取黑名单配置，并返回对应的BlackIpVO
	 * 
	 * @param appName 应用名称
	 * @param ip      IP地址
	 * @return
	 */
	public BlackIpVO getBlackIp(String appName, String ip);

	/**
	 * 从ＩＰ黑名单中删除指定的ＩＰ，如果存在该ＩＰ则返回该ＩＰ原来所代表的对象
	 * 
	 * @param appName 应用名称
	 * @param ip      IP地址
	 * @return
	 */
	public BlackIpVO delBlackIp(String appName, String ip);

	/**
	 * 清除所有的ＩＰ黑名单
	 * 
	 * @param appName 应用名称
	 */
	public void cleanAllBlackIps(String appName);

	/**
	 * 获取所有的IP黑名单列表
	 * 
	 * @param appName 应用名称
	 * @return
	 */
	public List<BlackIpVO> getAllBlackIps(String appName);
}
