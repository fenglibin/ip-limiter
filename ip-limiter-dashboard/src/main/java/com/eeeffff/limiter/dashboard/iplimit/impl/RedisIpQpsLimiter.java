package com.eeeffff.limiter.dashboard.iplimit.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.eeeffff.limiter.common.util.AppUtil;
import com.eeeffff.limiter.common.vo.IpLimitVO;
import com.eeeffff.limiter.dashboard.constants.RedisKey;
import com.eeeffff.limiter.dashboard.iplimit.IpQpsLimiter;
import com.eeeffff.limiter.dashboard.redis.RedisTemplateWrapper;

/**
 * 拉取全局的IP限流配置
 * 
 * @author fenglibin
 *
 */
@Service("redisIpQpsLimiter")
public class RedisIpQpsLimiter implements IpQpsLimiter {

	@Override
	public IpLimitVO addIpLimit(String appName, String ip, int limit) {
		IpLimitVO ipLimitVO = IpLimitVO.builder().ip(ip).limit(limit).addDate(new Date()).build();
		RedisTemplateWrapper.hSet(AppUtil.getAppRedisKeyPrefix(appName) + RedisKey.IP_LIMIT_KEY, ip, ipLimitVO);
		return ipLimitVO;
	}

	@Override
	public IpLimitVO getIpLimit(String appName, String ip) {
		return (IpLimitVO) RedisTemplateWrapper.hGet(AppUtil.getAppRedisKeyPrefix(appName) + RedisKey.IP_LIMIT_KEY,
				ip);
	}

	@Override
	public IpLimitVO delIpLimit(String appName, String ip) {
		Object value = RedisTemplateWrapper.hGet(AppUtil.getAppRedisKeyPrefix(appName) + RedisKey.IP_LIMIT_KEY, ip);
		if (value == null) {
			return null;
		}
		RedisTemplateWrapper.hDel(AppUtil.getAppRedisKeyPrefix(appName) + RedisKey.IP_LIMIT_KEY, ip);
		return (IpLimitVO) value;
	}

	@Override
	public void cleanAllIpLimits(String appName) {
		RedisTemplateWrapper.del(AppUtil.getAppRedisKeyPrefix(appName) + RedisKey.IP_LIMIT_KEY);
	}

	@Override
	public List<IpLimitVO> getAllIpLimits(String appName) {
		List<IpLimitVO> list = new ArrayList<IpLimitVO>();

		// 拉取当前应用的IP限流配置
		List<Object> ips = RedisTemplateWrapper
				.hGetHashKeys(AppUtil.getAppRedisKeyPrefix(appName) + RedisKey.IP_LIMIT_KEY);
		if (CollectionUtils.isEmpty(ips)) {
			return list;
		}
		List<Object> values = RedisTemplateWrapper
				.hGets(AppUtil.getAppRedisKeyPrefix(appName) + RedisKey.IP_LIMIT_KEY, ips);
		if (CollectionUtils.isEmpty(values)) {
			return list;
		}
		values.forEach(e -> {
			list.add((IpLimitVO) e);
		});
		return list;
	}

	@Override
	public List<IpLimitVO> updateIpQpsLimit(String appName) {
		return getAllIpLimits(appName);
	}

	@Override
	public void setDefaultIpMaxQps(String appName, int defaultIpMaxQps) {
		RedisTemplateWrapper.set(AppUtil.getAppRedisKeyPrefix(appName) + RedisKey.PERMITS_PER_SECOND_EACH_IP_KEY,
				defaultIpMaxQps);
	}

	@Override
	public Integer getDefaultIpMaxQps(String appName) {
		Object val = RedisTemplateWrapper
				.get(AppUtil.getAppRedisKeyPrefix(appName) + RedisKey.PERMITS_PER_SECOND_EACH_IP_KEY);
		if (val != null) {
			return (Integer) val;
		}
		return null;
	}

}
