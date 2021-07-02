package com.eeeffff.limiter.core.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.cache.Cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.eeeffff.limiter.common.constant.Constants;
import com.eeeffff.limiter.common.enumeration.AccessType;
import com.eeeffff.limiter.common.vo.AccessVO;
import com.eeeffff.limiter.common.vo.IpLimitVO;
import com.eeeffff.limiter.core.config.IpLimiterConfigurationProperties;

/**
 * ＩＰ请求访问记录缓存
 * 
 * @author fenglibin
 *
 */
@Service("ipCacheHelper")
public class IpCacheHelper {
	@Resource(name = "ipCache")
	private Cache<String,Object> ipCache;
	@Resource(name = "ipLimitCache")
	private Cache<String,Object> ipLimitCache;
	@Autowired
	private MinuteIpCacheHelper minuteIpCacheHelper;
	@Autowired
	private SecondIpCacheHelper secondIpCacheHelper;
	@Autowired
	private IpLimiterConfigurationProperties ipLimiterConfigurationProperties;

	// 用于保存以分钟为统计纬度的数据的队列,默认保存60分钟的统计数据，可以通过配置文件中的maxTopAccessMinutes属性指定
	// Map<Long,Map<String, HashMap<Long, AccessVO>>>:
	// 最外层的Long型Key，代表的是当前的分钟
	// 第二层的String型Key，代表的是IP地址
	// 第三层的Long型Key，代表的是当前的分钟，第三层的AccessVO代表的是访问记录对象
	private Queue<Map<Long, Map<String, HashMap<Long, AccessVO>>>> minuteAccessQueue = null;
	// 用于保存以秒钟为统计纬度的数据的队列,默认保存60分钟的统计数据，可以通过配置文件中的secondsMetricReportInterval及secondsAccessNums属性控制存储的统计纬度
	// Map<Long,Map<String, HashMap<Long, AccessVO>>>:
	// 最外层的Long型Key，代表的是当前的秒钟
	// 第二层的String型Key，代表的是IP地址
	// 第三层的Long型Key，代表的是当前的秒钟，第三层的AccessVO代表的是访问记录对象
	private Queue<Map<Long, Map<String, HashMap<Long, AccessVO>>>> secondAccessQueue = null;

	@SuppressWarnings("unchecked")
	@PostConstruct
	public void init() {
		ipLimitCache.put("minuteAccessQueue",
				new LinkedBlockingQueue<Map<Long, Map<String, HashMap<Long, AccessVO>>>>(
						ipLimiterConfigurationProperties.getMaxTopAccessMinutes()));
		ipLimitCache.put("secondAccessQueue",
				new LinkedBlockingQueue<Map<Long, Map<String, HashMap<Long, AccessVO>>>>(
						ipLimiterConfigurationProperties.getSecondsMetricLocalKeeped()));
		/*
		 * minuteAccessQueue = new LinkedBlockingQueue<List<AccessVO>>(
		 * ipLimiterConfigurationProperties.getMaxTopAccessMinutes()); secondAccessQueue
		 * = new LinkedBlockingQueue<Map<String, HashMap<Long, AccessVO>>>(
		 * ipLimiterConfigurationProperties.getSecondsAccessNums());
		 */
		minuteAccessQueue = (Queue<Map<Long, Map<String, HashMap<Long, AccessVO>>>>) ipLimitCache
				.get("minuteAccessQueue");
		secondAccessQueue = (Queue<Map<Long, Map<String, HashMap<Long, AccessVO>>>>) ipLimitCache
				.get("secondAccessQueue");
	}

	/**
	 * 增加指定ＩＰ在当前秒的访问数
	 * 
	 * @param ip
	 */
	@Async
	public void incrVisit(String ip, String url, AccessType accessType, int maxValue) {
		boolean result = secondIpCacheHelper.incrVisit(ip, url, accessType, maxValue);
		if (!result) {
			accessType = AccessType.Block;
		}
		minuteIpCacheHelper.incrVisit(ip, url, accessType, maxValue);
	}

	/**
	 * 将分钟访问数据加到当前的分钟访问队列中，但是队列的长度即总共保留多分钟的访问数据，可以通过配置文件中的maxTopAccessMinutes属性指定
	 * 
	 * @param minuteAccess
	 */
	public void addMinuteAccessQueue(Map<Long, Map<String, HashMap<Long, AccessVO>>> minuteAccess) {
		if (minuteAccessQueue.size() == ipLimiterConfigurationProperties.getMaxTopAccessMinutes()) {
			// 满了就扔掉最旧的
			minuteAccessQueue.remove();
		}
		minuteAccessQueue.add(minuteAccess);
	}

	/**
	 * 将秒钟访问数据加到当前的分钟访问队列中，但是队列的长度即总共保留多少份秒钟的访问数据（注：每一份默认保留的是１０秒钟的访问数据），可以通过配置文件中的secondsMetricReportInterval及secondsAccessNums属性指定
	 * 
	 * @param secondAccess
	 */
	public void addSecondAccessQueue(Map<Long, Map<String, HashMap<Long, AccessVO>>> secondAccess) {
		if (secondAccessQueue.size() == ipLimiterConfigurationProperties.getSecondsMetricLocalKeeped()) {
			// 满了就扔掉最旧的
			secondAccessQueue.remove();
		}
		secondAccessQueue.add(secondAccess);
	}

	/**
	 * 获取IP在本地缓存中记录的QPS情况
	 * 
	 * @param ip
	 * @return
	 */
	public HashMap<Long, AccessVO> getIpAccess(String ip) {
		Object obj = ipCache.get(ip);
		if (obj == null) {
			return null;
		}
		@SuppressWarnings("unchecked")
		HashMap<Long, AccessVO> accessMap = (HashMap<Long, AccessVO>) obj;
		return accessMap;
	}

	/**
	 * 获取本地保存的所有IP的QPS限制配置，这些配置都是从远程控制台同步过来的
	 * 
	 * @return
	 */
	public Map<String, IpLimitVO> getIpLimit() {
		Object obj = ipLimitCache.get(Constants.IP_LIMIT_LOCAL_KEY);
		if (obj == null) {
			return null;
		}

		@SuppressWarnings("unchecked")
		Map<String, IpLimitVO> ipLimitMap = (Map<String, IpLimitVO>) obj;
		return ipLimitMap;
	}

	public MinuteIpCacheHelper getMinuteIpCacheHelper() {
		return minuteIpCacheHelper;
	}

	public SecondIpCacheHelper getSecondIpCacheHelper() {
		return secondIpCacheHelper;
	}

	public Queue<Map<Long, Map<String, HashMap<Long, AccessVO>>>> getMinuteAccessQueue() {
		return minuteAccessQueue;
	}

	public Queue<Map<Long, Map<String, HashMap<Long, AccessVO>>>> getSecondAccessQueue() {
		return secondAccessQueue;
	}

}
