package com.eeeffff.limiter.dashboard.iplimit.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.eeeffff.limiter.common.util.AppUtil;
import com.eeeffff.limiter.common.vo.WhiteIpVO;
import com.eeeffff.limiter.dashboard.constants.RedisKey;
import com.eeeffff.limiter.dashboard.iplimit.WhiteIpLimiter;
import com.eeeffff.limiter.dashboard.redis.RedisTemplateWrapper;

/**
 * ＩＰ白名单操作服务类
 * 
 * @author fenglibin
 *
 */
@Service("redisWhiteIpLimiter")
public class RedisWhiteIpLimiter implements WhiteIpLimiter {

	@Override
	public WhiteIpVO addWhiteIp(String appName, String ip) {
		WhiteIpVO vo = WhiteIpVO.builder().ip(ip).build();
		RedisTemplateWrapper.hSet(AppUtil.getAppRedisKeyPrefix(appName) + RedisKey.WHITE_IPS_KEY, ip, vo);
		return vo;
	}

	@Override
	public WhiteIpVO getWhiteIp(String appName, String ip) {
		return (WhiteIpVO) RedisTemplateWrapper
				.hGet(AppUtil.getAppRedisKeyPrefix(appName) + RedisKey.WHITE_IPS_KEY, ip);
	}

	@Override
	public WhiteIpVO delWhiteIp(String appName, String ip) {
		Object value = RedisTemplateWrapper.hGet(AppUtil.getAppRedisKeyPrefix(appName) + RedisKey.WHITE_IPS_KEY,
				ip);
		RedisTemplateWrapper.hDel(AppUtil.getAppRedisKeyPrefix(appName) + RedisKey.WHITE_IPS_KEY, ip);
		return (WhiteIpVO) value;
	}

	@Override
	public void cleanAllWhiteIps(String appName) {
		RedisTemplateWrapper.del(AppUtil.getAppRedisKeyPrefix(appName) + RedisKey.WHITE_IPS_KEY);
	}

	@Override
	public List<WhiteIpVO> getAllWhiteIps(String appName) {
		List<WhiteIpVO> list = new ArrayList<WhiteIpVO>();
		List<Object> whiteIpKeysList = RedisTemplateWrapper
				.hGetHashKeys(AppUtil.getAppRedisKeyPrefix(appName) + RedisKey.WHITE_IPS_KEY);
		if (CollectionUtils.isEmpty(whiteIpKeysList)) {
			return list;
		}
		List<Object> whiteIpList = RedisTemplateWrapper
				.hGets(AppUtil.getAppRedisKeyPrefix(appName) + RedisKey.WHITE_IPS_KEY, whiteIpKeysList);
		if (CollectionUtils.isEmpty(whiteIpList)) {
			return list;
		}
		whiteIpList.forEach(e -> {
			list.add((WhiteIpVO) e);
		});
		return list;
	}

}
