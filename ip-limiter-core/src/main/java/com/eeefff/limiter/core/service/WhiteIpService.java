package com.eeefff.limiter.core.service;

import java.util.Map;

import javax.annotation.Resource;
import javax.cache.Cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eeefff.limiter.common.constant.Constants;
import com.eeefff.limiter.common.util.HttpClientUtil;
import com.eeefff.limiter.common.util.IpHelper;
import com.eeefff.limiter.common.util.RetryHelper;
import com.eeefff.limiter.common.vo.Result;
import com.eeefff.limiter.common.vo.WhiteIpVO;
import com.eeefff.limiter.core.config.IpLimiterConfigurationProperties;
import com.eeefff.limiter.core.config.SystemEnv;

import lombok.extern.slf4j.Slf4j;

/**
 * ＩＰ白名单服务
 * 
 * @author fenglibin
 *
 */
@Slf4j
@Service
public class WhiteIpService {
	@Resource(name = "ipLimitCache")
	private Cache<String,Object> ipLimitCache;
	@Autowired
	private IpLimiterConfigurationProperties ipLimiterConfigurationProperties;

	/**
	 * 判断IP是否在白名单中，会同时判断该IP是否处在白名单中的特定的IP段中
	 * 
	 * @param ip
	 * @return
	 */
	public WhiteIpVO checkWhiteIp(String ip) {
		Object obj = ipLimitCache.get(Constants.IP_WHITE_LOCAL_KEY);
		if (obj == null) {
			return null;
		}
		@SuppressWarnings("unchecked")
		Map<String, WhiteIpVO> whiteIp = (Map<String, WhiteIpVO>) obj;
		WhiteIpVO ipLimit = whiteIp.get(ip);
		if (ipLimit != null) {
			return ipLimit;
		} else {
			String threeLevelIp = IpHelper.getThreeLevelIpAddress(ip);
			ipLimit = whiteIp.get(threeLevelIp);
			if (ipLimit != null) {
				return ipLimit;
			} else {
				String twoLevelIp = IpHelper.getTwoLevelIpAddress(ip);
				ipLimit = whiteIp.get(twoLevelIp);
				if (ipLimit != null) {
					return ipLimit;
				} else {
					String oneLevelIp = IpHelper.getOneLevelIpAddress(ip);
					ipLimit = whiteIp.get(oneLevelIp);
					if (ipLimit != null) {
						return ipLimit;
					}
				}
			}
		}
		return null;
	}

	/**
	 * 将访问当前应用的指定ＩＰ增加到白名单中，只针对当前应用有效
	 * 
	 * @param ip
	 * @return 是否加入到白名单中成功
	 */
	public boolean addWhiteIp(String ip) {
		boolean result = addWhiteIpRemote(ip);
		if (result) {
			addWhiteIpLocal(ip);
		}
		return result;
	}

	private boolean addWhiteIpRemote(String ip) {
		String url = new StringBuilder("http://").append(ipLimiterConfigurationProperties.getDashboardAddress())
				.append("/limiter/addClientIpToWhiteIp?appName=").append(SystemEnv.getAppName()).append("&ip=")
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
			log.warn("应用{}往服务控制台增加白名单IP{}失败!", SystemEnv.getAppName(), ip);
		} else {
			log.info("应用{}往服务控制台增加白名单IP{}成功", SystemEnv.getAppName(), ip);
		}
		return httpResult;
	}

	/**
	 * 将ＩＰ增加到本地白名单中
	 * 
	 * @param ip
	 * @return
	 */
	private boolean addWhiteIpLocal(String ip) {
		Object obj = ipLimitCache.get(Constants.IP_WHITE_LOCAL_KEY);
		if (obj == null) {
			return false;
		}
		@SuppressWarnings("unchecked")
		Map<String, WhiteIpVO> whiteIp = (Map<String, WhiteIpVO>) obj;
		whiteIp.put(ip, WhiteIpVO.builder().ip(ip).build());
		return true;
	}
}
