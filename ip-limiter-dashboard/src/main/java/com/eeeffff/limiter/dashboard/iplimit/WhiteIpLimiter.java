package com.eeeffff.limiter.dashboard.iplimit;

import java.util.List;

import com.eeeffff.limiter.common.vo.WhiteIpVO;

/**
 * IP白名单处理类
 * 
 * @author fenglibin
 *
 */
public interface WhiteIpLimiter {
	/**
	 * 增加IP到白名单中
	 * 
	 * @param ip
	 */
	public WhiteIpVO addWhiteIp(String appName, String ip);

	/**
	 * 根据ＩＰ获取白名单配置，并返回对应的WhiteIpVO
	 * 
	 * @param ip
	 * @return
	 */
	public WhiteIpVO getWhiteIp(String appName, String ip);

	/**
	 * 从ＩＰ白名单中删除指定的ＩＰ，如果存在该ＩＰ则返回该ＩＰ原来所代表的对象
	 * 
	 * @param ip
	 * @return
	 */
	public WhiteIpVO delWhiteIp(String appName, String ip);

	/**
	 * 清除所有的ＩＰ白名单
	 */
	public void cleanAllWhiteIps(String appName);

	/**
	 * 获取所有的IP白名单列表
	 * 
	 * @return
	 */
	public List<WhiteIpVO> getAllWhiteIps(String appName);
}
