package com.eeeffff.limiter.core.interceptor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.CollectionUtils;

import com.eeeffff.limiter.common.enumeration.AccessType;
import com.eeeffff.limiter.common.util.IpHelper;
import com.eeeffff.limiter.common.util.TimeUtil;
import com.eeeffff.limiter.common.vo.AccessVO;
import com.eeeffff.limiter.common.vo.IpLimitVO;
import com.eeeffff.limiter.core.cache.IpCacheHelper;

/**
 * 相同IP的QPS限流器，不同于漏桶限流算法以固定的速率处理请求，也不同于令牌桶限流算法以固定的速率产生令牌的方式，<br>
 * 该限流算法强调对QPS进行限流。<br>
 * 如IP的QPS限制为100，则该100个请求可以在前面100毫秒处理完，后续900毫秒则拒绝该IP的所有请求。<br>
 * 与令牌桶限流算法处理突发流量的情况类似，但是与令牌桶限流算法以固定的速率产生令牌这点又不同，令牌桶限流算法不是用于
 * 前面100毫秒产生完该秒所有的令牌，这会导致这种情况下很多请求被拒绝。 通过对IP的Qps的限流，则可以达到这种效果。
 * 
 * @author fenglibin
 *
 */
public class IpQpsRateLimiter {

	private static IpQpsRateLimiter instance;

	private static int permitsPerSecondEachIp = 0;
	private IpCacheHelper ipCacheHelper;

	/**
	 * 
	 * @param permitsPerSecondEachIp 单个IP默认的最大的QPS
	 * @param ipCacheHelper          ＩＰ请求访问记录内存缓存操作类
	 */
	private IpQpsRateLimiter(int permitsPerSecondEachIp, IpCacheHelper ipCacheHelper) {
		IpQpsRateLimiter.permitsPerSecondEachIp = permitsPerSecondEachIp;
		this.ipCacheHelper = ipCacheHelper;
	}

	/**
	 * 
	 * @param permitsPerSecondEachIp 单个IP默认的最大的QPS
	 * @param ipCacheHelper          ＩＰ请求访问记录内存缓存操作类
	 */
	public static void initIpQpsRateLimiter(int permitsPerSecondEachIp, IpCacheHelper ipCacheHelper) {
		IpQpsRateLimiter ipQpsRateLimiter = new IpQpsRateLimiter(permitsPerSecondEachIp, ipCacheHelper);
		instance = ipQpsRateLimiter;
	}
	
	/**
	 * 重新设置Limit
	 * 
	 * @param limiterCache
	 */
	public static void resetLimit(int permitsPerSecondEachIp) {
		IpQpsRateLimiter.permitsPerSecondEachIp = permitsPerSecondEachIp;
	}

	/**
	 * 获取IpQpsRateLimiter实例
	 * 
	 * @return
	 */
	public static IpQpsRateLimiter getIntance() {
		if(instance==null) {
			throw new RuntimeException("IpQpsRateLimiter实例未被初使化，请先调用方法initIpQpsRateLimiter初使化实例.");
		}
		return instance;
	}
	
	public static int getPermitsPerSecondEachIp() {
		return permitsPerSecondEachIp;
	}

	/**
	 * 尝试根据当前IP获取通行令牌，如果当前ＩＰ的ＱＰＳ没有超过当前设置的最大的ＱＰＳ数据，则获取令牌成功，否则获取失败。
	 * 获取指定IP的QPS限制设置。先获取完成IP的QPS限流配置，如果取到则用，没有取到则取其前三级IP的QPS限制设置，如果取到则用，没有取到则
	 * 取其前二级IP的QPS限制设置，如果取到则用，没有取到则使用默认的限流规则。
	 * 如IP为127.0.0.1，则首先取127.0.0.1的QPS限流配置，如果没有取到再取127.0.0的QPS限流配置，如果没有取到再取127.0的QPS限流配置，
	 * 如果都没有取到则使用默认的QPS限流配置。
	 * 
	 * @param ip
	 * @return 是否成功获取访问通行令牌
	 */
	public boolean tryAquire(String ip, String url) {
		int maxValue = getIpLimit(ip);
		boolean aquire = aquire(ip, url, maxValue);
		if (aquire) {
			ipCacheHelper.incrVisit(ip, url, AccessType.Normal, maxValue);
		} else {
			ipCacheHelper.incrVisit(ip, url, AccessType.Block, -1);
		}
		return aquire;
	}

	private boolean aquire(String ip, String url, int maxValue) {
		long currentSecond = TimeUtil.currentTimeSeconds();
		HashMap<Long, AccessVO> accessMap = ipCacheHelper.getIpAccess(ip);
		if (accessMap == null) {
			return true;
		}
		AccessVO accessVO = accessMap.get(currentSecond);
		if (accessVO == null) {
			return true;
		}
		if (accessVO.getNormal().intValue() >= maxValue) {
			return false;
		}
		return true;
	}

	/**
	 * 获取指定IP的QPS限制设置。先获取完成IP的QPS限流配置，如果取到则用，没有取到则取其前三级IP的QPS限制设置，如果取到则用，没有取到则
	 * 取其前二级IP的QPS限制设置，如果取到则用，没有取到则使用默认的限流规则。
	 * 如IP为127.0.0.1，则首先取127.0.0.1的QPS限流配置，如果没有取到再取127.0.0的QPS限流配置，如果没有取到再取127.0的QPS限流配置，
	 * 如果都没有取到则使用默认的QPS限流配置。
	 * 
	 * @param ip
	 * @return
	 */
	private int getIpLimit(String ip) {
		Map<String, IpLimitVO> ipLimitMap = ipCacheHelper.getIpLimit();
		if (CollectionUtils.isEmpty(ipLimitMap)) {
			return permitsPerSecondEachIp;
		}

		IpLimitVO ipLimit = ipLimitMap.get(ip);
		if (ipLimit != null && ipLimit.getLimit() > 0) {
			return ipLimit.getLimit();
		} else {
			String threeLevelIp = IpHelper.getThreeLevelIpAddress(ip);
			ipLimit = ipLimitMap.get(threeLevelIp);
			if (ipLimit != null && ipLimit.getLimit() > 0) {
				return ipLimit.getLimit();
			} else {
				String twoLevelIp = IpHelper.getTwoLevelIpAddress(ip);
				ipLimit = ipLimitMap.get(twoLevelIp);
				if (ipLimit != null && ipLimit.getLimit() > 0) {
					return ipLimit.getLimit();
				} else {
					String oneLevelIp = IpHelper.getOneLevelIpAddress(ip);
					ipLimit = ipLimitMap.get(oneLevelIp);
					if (ipLimit != null && ipLimit.getLimit() > 0) {
						return ipLimit.getLimit();
					}
				}
			}
		}
		return permitsPerSecondEachIp;
	}
}