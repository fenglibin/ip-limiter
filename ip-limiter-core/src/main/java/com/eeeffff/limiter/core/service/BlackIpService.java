package com.eeeffff.limiter.core.service;

import java.util.Map;

import javax.annotation.Resource;
import javax.cache.Cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eeeffff.limiter.common.constant.Constants;
import com.eeeffff.limiter.common.util.HttpClientUtil;
import com.eeeffff.limiter.common.util.IpHelper;
import com.eeeffff.limiter.common.util.RetryHelper;
import com.eeeffff.limiter.common.vo.BlackIpVO;
import com.eeeffff.limiter.common.vo.Result;
import com.eeeffff.limiter.core.config.IpLimiterConfigurationProperties;
import com.eeeffff.limiter.core.config.SystemEnv;

import lombok.extern.slf4j.Slf4j;

/**
 * ＩＰ黑名单服务
 * 
 * @author fenglibin
 *
 */
@Slf4j
@Service
public class BlackIpService {
	@Resource(name = "ipLimitCache")
	private Cache<String,Object> ipLimitCache;
	@Autowired
	private IpLimiterConfigurationProperties ipLimiterConfigurationProperties;

	/**
	 * 判断IP是否在黑名单中，会同时判断该IP是否处在黑名单中的特定的IP段中
	 * 
	 * @param ip
	 * @return
	 */
	public BlackIpVO checkBlackIp(String ip) {
		Object obj = ipLimitCache.get(Constants.IP_BLACK_LOCAL_KEY);
		if (obj == null) {
			return null;
		}
		@SuppressWarnings("unchecked")
		Map<String, BlackIpVO> blackIp = (Map<String, BlackIpVO>) obj;
		BlackIpVO ipLimit = blackIp.get(ip);
		if (ipLimit != null) {
			return ipLimit;
		} else {
			String threeLevelIp = IpHelper.getThreeLevelIpAddress(ip);
			ipLimit = blackIp.get(threeLevelIp);
			if (ipLimit != null) {
				return ipLimit;
			} else {
				String twoLevelIp = IpHelper.getTwoLevelIpAddress(ip);
				ipLimit = blackIp.get(twoLevelIp);
				if (ipLimit != null) {
					return ipLimit;
				} else {
					String oneLevelIp = IpHelper.getOneLevelIpAddress(ip);
					ipLimit = blackIp.get(oneLevelIp);
					if (ipLimit != null) {
						return ipLimit;
					}
				}
			}
		}
		return null;
	}

	/**
	 * 将访问当前应用的指定ＩＰ增加到黑名单中，会影响到该ＩＰ对该应用的访问，但是不影响到该ＩＰ对其它应用的访问
	 * 
	 * @param ip
	 * @return 是否加入到黑名单中成功
	 */
	public boolean addBlackIp(String ip) {
		boolean result = addBlackIpRemote(ip);
		if (result) {
			addBlackIpLocal(ip);
		}
		return result;
	}

	private boolean addBlackIpRemote(String ip) {
		String url = new StringBuilder("http://").append(ipLimiterConfigurationProperties.getDashboardAddress())
				.append("/limiter/addClientIpToBlackIp?appName=").append(SystemEnv.getAppName()).append("&ip=")
				.append(ip).toString();
		boolean httpResult = RetryHelper.doRetryAction(() -> {
			Result<?> result = Result.empty();
			HttpClientUtil.doGet(url, r -> {
				if (r != null && r.getCode() == 0) {
					result.setSuccess(true);
				}
			});
			return result.isSuccess();
		});
		if (!httpResult) {
			log.warn("应用{}往服务控制台增加黑名单IP{}失败!", SystemEnv.getAppName(), ip);
		} else {
			log.info("应用{}往服务控制台增加黑名单IP{}成功", SystemEnv.getAppName(), ip);
		}
		return httpResult;
	}

	/**
	 * 将ＩＰ增加到本地黑名单中
	 * 
	 * @param ip
	 * @return
	 */
	private boolean addBlackIpLocal(String ip) {
		Object obj = ipLimitCache.get(Constants.IP_BLACK_LOCAL_KEY);
		if (obj == null) {
			return false;
		}
		@SuppressWarnings("unchecked")
		Map<String, BlackIpVO> blackIp = (Map<String, BlackIpVO>) obj;
		blackIp.put(ip, BlackIpVO.builder().ip(ip).build());
		return true;
	}
}
