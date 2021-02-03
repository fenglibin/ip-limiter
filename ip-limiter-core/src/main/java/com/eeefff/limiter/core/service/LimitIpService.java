package com.eeefff.limiter.core.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.cache.Cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eeefff.limiter.common.constant.Constants;
import com.eeefff.limiter.common.util.HttpClientUtil;
import com.eeefff.limiter.common.util.IpHelper;
import com.eeefff.limiter.common.util.RetryHelper;
import com.eeefff.limiter.common.vo.IpLimitVO;
import com.eeefff.limiter.common.vo.Result;
import com.eeefff.limiter.core.config.IpLimiterConfigurationProperties;
import com.eeefff.limiter.core.config.SystemEnv;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LimitIpService {
	@Resource(name = "ipLimitCache")
	private Cache<String,Object> ipLimitCache;
	@Autowired
	private IpLimiterConfigurationProperties ipLimiterConfigurationProperties;

	/**
	 * 设置应用访问的ＱＰＳ限制
	 * 
	 * @param ip
	 * @param limit
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean addLimitIp(String ip, int limit) {
		String url = new StringBuilder("http://").append(ipLimiterConfigurationProperties.getDashboardAddress())
				.append("/limiter/qpsLimit/setClientIpLimit?appName=").append(SystemEnv.getAppName()).append("&ip=").append(ip)
				.append("&limit=").append(limit).toString();
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
			log.warn("应用{}往服务控制台设置IP{}访问限制为{}失败!", SystemEnv.getAppName(), ip, limit);
		} else {
			log.info("应用{}往服务控制台设置IP{}访问限制为{}成功，现将其加入到本地缓存中。", SystemEnv.getAppName(), ip, limit);
			// 设置IP的限流本地缓存，使其可以即时生效
			Map<String, IpLimitVO> ipLimitMap;
			Object obj = ipLimitCache.get(Constants.IP_LIMIT_LOCAL_KEY);
			if (obj == null) {
				ipLimitMap = new HashMap<String, IpLimitVO>();
			} else {
				ipLimitMap = (Map<String, IpLimitVO>) ipLimitCache.get(Constants.IP_LIMIT_LOCAL_KEY);
			}
			ipLimitMap.put(IpHelper.removeIpWildcard(ip),
					IpLimitVO.builder().ip(ip).limit(limit).addDate(new Date()).build());
		}
		return httpResult;
	}
}
