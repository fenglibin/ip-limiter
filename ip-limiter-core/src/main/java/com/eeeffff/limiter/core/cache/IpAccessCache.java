package com.eeeffff.limiter.core.cache;

import java.util.HashMap;
import java.util.Map;

import com.eeeffff.limiter.common.enumeration.AccessType;
import com.eeeffff.limiter.common.vo.AccessVO;

public interface IpAccessCache {

	/**
	 * 
	 * @param ip
	 * @param url
	 * @param accessType
	 * @param maxValue
	 * @return
	 */
	/**
	 * 根据访问类型，增加指定ＩＰ指定秒钟的访问总量、正常访问量或被拒绝的访问量，操作成功返回true,操作失败返回false
	 * 
	 * @param ip         访问来源IP
	 * @param url        被访问的url
	 * @param accessType 当前访问的类型，是被拒绝的请求还是正常的访问请求
	 * @param maxValue   QPS正常访问的值的最大值，正常访问的不可以超过该值
	 * @return 返回正常访问的QPS是否超过了最大值，确保即使在是多线程的环境下最大的正常访问量不超过设置的最大值
	 */
	public boolean incrVisit(String ip, String url, AccessType accessType,int maxValue);

	/**
	 * 获取指定IP的以秒为统计纬度的所有可用的统计
	 * 
	 * @param ip
	 * @return
	 */
	public HashMap<Long, AccessVO> getVisit(String ip);

	/**
	 * 获取指定IP指定时间(秒)的统计，该统计是获取内存中的统计，如果内存中的统计被删除则不能够获取到值
	 * 
	 * @param ip
	 * @param senconds
	 * @return
	 */
	public AccessVO getVisit(String ip, Long senconds);

	/**
	 * 获取缓存中所有缓存的内容
	 * 
	 * @return
	 */
	public Map<String, HashMap<Long, AccessVO>> getAllVisit();

	/**
	 * 清除指定ＩＰ的访问记录
	 * 
	 * @param ip
	 * @return
	 */
	public boolean cleanVisit(String ip);

	/**
	 * 清除全部缓存
	 * 
	 * @return
	 */
	public int cleanAllVisit();

}