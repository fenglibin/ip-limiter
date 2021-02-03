package com.eeefff.limiter.dashboard.metric.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.eeefff.limiter.common.vo.AccessVO;
import com.eeefff.limiter.dashboard.config.IpLimiterDashboardConfigurationProperties;
import com.eeefff.limiter.dashboard.constants.RedisKey;
import com.eeefff.limiter.dashboard.metric.AccessMetricHandler;
import com.eeefff.limiter.dashboard.redis.RedisTemplateWrapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 处理所有以分为纬度统计的统计，与所有应用的以分为统计纬度的数据进行汇总统计
 * 
 * @author fenglibin
 *
 */
@Slf4j
@Service(value = "globalMinutesAccessMetricHandler")
public class GlobalMinutesAccessMetricHandler extends AbstraceMinutesAccessMetricHandler
		implements AccessMetricHandler {
	@Autowired
	private IpLimiterDashboardConfigurationProperties ipLimiterConfigurationProperties;
	@Resource(name = "accessVOComparator")
	private Comparator<AccessVO> comparator;

	@Override
	@Async
	public void handleAccessMetric(String appName, String ipPort, List<AccessVO> topAccessMetricList) {
		incrRedisAccess(topAccessMetricList);
	}

	/**
	 * 增加Redis中的访问
	 * 
	 * @param minuteAccessList
	 */
	private void incrRedisAccess(List<AccessVO> minuteAccessList) {
		if (CollectionUtils.isEmpty(minuteAccessList)) {
			return;
		}
		// 同一批次的当前分钟都是一样的，随便取一个都OK
		long currentMinuteVal = minuteAccessList.get(0).getCurrentMinutes();
		String currentMinute = String.valueOf(currentMinuteVal);
		// 从Redis中获取所有保存的以分钟为纬度的访问数据
		List<Object> minuteKeys = RedisTemplateWrapper.hGetHashKeys(RedisKey.MINUTE_ACCESS_KEY);
		if (!CollectionUtils.isEmpty(minuteKeys)) {
			List<String> expiredMinuteKeys = minuteKeys.stream().map(o -> {
				return Long.valueOf(String.valueOf(o));
			}).filter(o -> {
				// 超过${ipLimiterConfigurationProperties.getMaxRedisTopAccessMinutes()}分钟的数据会被删除
				return o < (currentMinuteVal - ipLimiterConfigurationProperties.getGlobalMaxRedisTopAccessMinutes());
			}).map(o -> {
				return String.valueOf(o);
			}).collect(Collectors.toList());
			if (!CollectionUtils.isEmpty(expiredMinuteKeys)) {
				// 执行删除过期的数据
				RedisTemplateWrapper.hDel(RedisKey.MINUTE_ACCESS_KEY, expiredMinuteKeys.toArray());
			}
		}
		boolean lock = RedisTemplateWrapper.getLock(currentMinute,
				ipLimiterConfigurationProperties.getRedisLockMaxWaitMillis());
		if (!lock) {
			log.warn("（全局）获取分钟为:" + currentMinute + "的Redis分布式锁失败，当前这1分钟的IP访问统计数据会被丢弃掉，等待的时间为："
					+ (ipLimiterConfigurationProperties.getRedisLockMaxWaitMillis() / 1000) + "秒");
			return;
		}
		try {
			@SuppressWarnings("unchecked")
			List<AccessVO> redisMinuteAccessList = (List<AccessVO>) RedisTemplateWrapper
					.hGet(RedisKey.MINUTE_ACCESS_KEY, currentMinute);
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
						.limit(ipLimiterConfigurationProperties.getGlobalMaxRedisTopAccessIps())
						.collect(Collectors.toList());
				RedisTemplateWrapper.hSet(RedisKey.MINUTE_ACCESS_KEY, currentMinute, redisMinuteAccessList);
			} else {
				RedisTemplateWrapper.hSet(RedisKey.MINUTE_ACCESS_KEY, currentMinute, minuteAccessList);
			}
		} finally {
			RedisTemplateWrapper.delLock(currentMinute);
		}

	}

}