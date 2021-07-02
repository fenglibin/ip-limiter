package com.eeeffff.limiter.dashboard.metric.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.eeeffff.limiter.common.util.AppUtil;
import com.eeeffff.limiter.common.util.DateUtil;
import com.eeeffff.limiter.common.util.TimeUtil;
import com.eeeffff.limiter.common.vo.AccessVO;
import com.eeeffff.limiter.dashboard.constants.RedisKey;
import com.eeeffff.limiter.dashboard.metric.IMetric;
import com.eeeffff.limiter.dashboard.redis.RedisTemplateWrapper;

/**
 * 从Redis中获取当前应用集群以分钟为纬度的统计数据
 * 
 * @author fenglibin
 *
 */
@Service(value = "redisMinuteMetric")
public class RedisMinuteMetric implements IMetric {

	@SuppressWarnings("unchecked")
	@Override
	public List<AccessVO> getOneMinuteData(String appName, String minute) {
		List<AccessVO> result = (List<AccessVO>) RedisTemplateWrapper
				.hGet(AppUtil.getAppRedisKeyPrefix(appName) + RedisKey.MINUTE_ACCESS_KEY, minute);
		if (CollectionUtils.isEmpty(result)) {
			return null;
		}
		result.forEach(o -> {
			AccessVO vo = (AccessVO) o;
			// 修改时间的展示
			vo.setCurrentDate(DateUtil.formatDate(vo.getCurrentMinutes() * 60 * 1000, DateUtil.ISO_DATE_TIME_FORMAT));
		});
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<List<AccessVO>> getMultiMinutesData(String appName, List<Object> minutes) {
		// List<Object>实际是List<List<AccessVO>>
		List<Object> list = (List<Object>) RedisTemplateWrapper
				.hGets(AppUtil.getAppRedisKeyPrefix(appName) + RedisKey.MINUTE_ACCESS_KEY, minutes);
		if (CollectionUtils.isEmpty(list)) {
			return null;
		}
		List<List<AccessVO>> result = new ArrayList<List<AccessVO>>();
		list.forEach(o -> {
			List<AccessVO> accessVOList = (List<AccessVO>) o;
			accessVOList.forEach(vo -> {
				// 修改时间的展示
				vo.setCurrentDate(
						DateUtil.formatDate(vo.getCurrentMinutes() * 60 * 1000, DateUtil.ISO_DATE_TIME_FORMAT));
			});
			result.add(accessVOList);
		});
		return result;
	}

	@Override
	public List<List<AccessVO>> getMinutesData(String appName, String ip, int lastMinutes) {
		long currentTimeMinutes = TimeUtil.currentTimeMinutes();
		appName = AppUtil.getAppNameWithIpRedisKey(appName, ip);
		List<Object> minuteKeys = getMinuteDataKeys(appName);
		if (CollectionUtils.isEmpty(minuteKeys)) {
			return null;
		}
		// 如果指定要查看的时间范围，需要对展示的时间进行筛选
		if (lastMinutes > 0) {
			minuteKeys = minuteKeys.stream().map(m -> {
				return Long.parseLong(String.valueOf(m));
			}).filter(m -> {
				if (m >= (currentTimeMinutes - lastMinutes) && m < currentTimeMinutes) {
					return true;
				}
				return false;
			}).sorted((m1, m2) -> {
				if (m1 > m2) {
					return -1;
				} else if (m1 < m2) {
					return 1;
				}
				return 0;
			}).map(m -> {
				return String.valueOf(m);
			}).collect(Collectors.toList());
		}
		return getMultiMinutesData(appName, minuteKeys);
	}

	@Override
	public List<Object> getMinuteDataKeys(String appName) {
		List<Object> hashKeyList = RedisTemplateWrapper
				.hGetHashKeys(AppUtil.getAppRedisKeyPrefix(appName) + RedisKey.MINUTE_ACCESS_KEY);
		if (CollectionUtils.isEmpty(hashKeyList)) {
			return null;
		}
		return hashKeyList.stream().map(o -> {
			return Long.parseLong(String.valueOf(o));
		}).sorted((o1, o2) -> {
			if (o1 > o2) {
				return -1;
			} else if (o1 < o2) {
				return 1;
			}
			return 0;
		}).map(o -> {
			return String.valueOf(o);
		}).collect(Collectors.toList());
	}
}
