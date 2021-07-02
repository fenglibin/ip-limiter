package com.eeeffff.limiter.dashboard.metric.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.eeeffff.limiter.common.util.AppUtil;
import com.eeeffff.limiter.common.vo.AccessVO;
import com.eeeffff.limiter.dashboard.config.IpLimiterDashboardConfigurationProperties;
import com.eeeffff.limiter.dashboard.constants.RedisKey;
import com.eeeffff.limiter.dashboard.metric.AccessMetricHandler;
import com.eeeffff.limiter.dashboard.redis.RedisTemplateWrapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 处理当前应用以分为纬度统计的统计，与当前应用的其它节点的数据进行汇总统计
 * 
 * @author fenglibin
 *
 */
@Slf4j
@Service(value = "minutesAccessMetricHandler")
public class MinutesAccessMetricHandler extends AbstraceMinutesAccessMetricHandler implements AccessMetricHandler {
	@Autowired
	private IpLimiterDashboardConfigurationProperties ipLimiterConfigurationProperties;
	@Resource(name = "accessVOComparator")
	private Comparator<AccessVO> comparator;

	@Override
	@Async
	public void handleAccessMetric(String appName, String ipPort, List<AccessVO> topAccessMetricList) {
		// 给指定的应用增加访问量
		incrRedisAccess(appName, null, topAccessMetricList);
		// 给指定的应用的指定IP增加访问量
		incrRedisAccess(appName, ipPort, topAccessMetricList);
	}

	/**
	 * 增加Redis中的访问
	 * 
	 * @param minuteAccessList
	 */
	private void incrRedisAccess(String appName, String ipPort, List<AccessVO> minuteAccessList) {
		if (CollectionUtils.isEmpty(minuteAccessList)) {
			return;
		}
		if (StringUtils.isNotEmpty(ipPort)) {
			appName = AppUtil.getAppNameWithIpRedisKey(appName, ipPort);
		}
		// 同一批次的当前分钟都是一样的，随便取一个都OK
		long currentMinuteVal = minuteAccessList.get(0).getCurrentMinutes();
		String currentMinute = String.valueOf(currentMinuteVal);
		// 从Redis中获取所有保存的以分钟为纬度的访问数据
		List<Object> minuteKeys = RedisTemplateWrapper
				.hGetHashKeys(AppUtil.getAppRedisKeyPrefix(appName) + RedisKey.MINUTE_ACCESS_KEY);
		if (!CollectionUtils.isEmpty(minuteKeys)) {
			List<String> expiredMinuteKeys = minuteKeys.stream().map(o -> {
				return Long.valueOf(String.valueOf(o));
			}).filter(o -> {
				// 超过${ipLimiterConfigurationProperties.getMaxRedisTopAccessMinutes()}分钟的数据会被删除
				return o < (currentMinuteVal - ipLimiterConfigurationProperties.getMaxRedisTopAccessMinutes());
			}).map(o -> {
				return String.valueOf(o);
			}).collect(Collectors.toList());
			if (!CollectionUtils.isEmpty(expiredMinuteKeys)) {
				// 执行删除过期的数据
				RedisTemplateWrapper.hDel(AppUtil.getAppRedisKeyPrefix(appName) + RedisKey.MINUTE_ACCESS_KEY,
						expiredMinuteKeys.toArray());
			}
		}
		boolean lock = RedisTemplateWrapper.getLock(currentMinute,
				ipLimiterConfigurationProperties.getRedisLockMaxWaitMillis());
		if (!lock) {
			log.warn("获取应用:" + appName + ",ip:" + ipPort + "分钟为:" + currentMinute
					+ "的Redis分布式锁失败，当前这1分钟的IP访问统计数据会被丢弃掉，等待的时间为："
					+ (ipLimiterConfigurationProperties.getRedisLockMaxWaitMillis() / 1000) + "秒");
			return;
		}
		try {
			@SuppressWarnings("unchecked")
			List<AccessVO> redisMinuteAccessList = (List<AccessVO>) RedisTemplateWrapper
					.hGet(AppUtil.getAppRedisKeyPrefix(appName) + RedisKey.MINUTE_ACCESS_KEY, currentMinute);
			if (!CollectionUtils.isEmpty(redisMinuteAccessList)) {
				Map<String, AccessVO> redisVoMap = redisMinuteAccessList.stream()
						.collect(Collectors.toMap(AccessVO::getIp, accessVO -> accessVO, (v1,v2)->v1));
				minuteAccessList.forEach(o -> {
					AccessVO vo = redisVoMap.get(o.getIp());
					if (vo != null) {
						vo.getTotal().addAndGet(o.getTotal().get());
						vo.getNormal().addAndGet(o.getNormal().get());
						vo.getBlock().addAndGet(o.getBlock().get());
					} else {
						redisVoMap.put(o.getIp(), o);
					}
				});
				redisMinuteAccessList = redisVoMap.values().stream().sorted(comparator)
						.limit(ipLimiterConfigurationProperties.getMaxRedisTopAccessIps()).collect(Collectors.toList());
				RedisTemplateWrapper.hSet(AppUtil.getAppRedisKeyPrefix(appName) + RedisKey.MINUTE_ACCESS_KEY,
						currentMinute, redisMinuteAccessList);
			} else {
				RedisTemplateWrapper.hSet(AppUtil.getAppRedisKeyPrefix(appName) + RedisKey.MINUTE_ACCESS_KEY,
						currentMinute, minuteAccessList);
			}
		} finally {
			RedisTemplateWrapper.delLock(currentMinute);
		}

	}
}