package com.eeefff.limiter.dashboard.master.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eeefff.limiter.common.util.HttpClientUtil;
import com.eeefff.limiter.common.util.RetryHelper;
import com.eeefff.limiter.common.vo.Result;
import com.eeefff.limiter.dashboard.config.IpLimiterDashboardConfigurationProperties;
import com.eeefff.limiter.dashboard.constants.RedisKey;
import com.eeefff.limiter.dashboard.master.IMasterService;
import com.eeefff.limiter.dashboard.redis.RedisTemplateWrapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RedisMasterService implements IMasterService {

	@Autowired
	private IpLimiterDashboardConfigurationProperties ipLimiterDashboardConfigurationProperties;

	@Override
	public String getMasterAddress() {
		return (String) RedisTemplateWrapper.get(RedisKey.MASTER_KEY);
	}

	@Override
	public boolean checkMasterHealth() {
		String masterAddress = getMasterAddress();
		return checkMasterHealth(masterAddress);
	}

	@Override
	public boolean checkMasterHealth(String masterAddress) {
		if (StringUtils.isEmpty(masterAddress)) {
			return false;
		}
		String url = new StringBuilder("http://").append(masterAddress).append("/ip-limiter/health/check").toString();
		boolean checkResult = RetryHelper.doRetryAction(() -> {
			Result<?> result = Result.empty();
			HttpClientUtil.doGet(url.toString(), r -> {
				if (r != null && r.getCode() == 0) {// 调用健康检查URL成功
					result.setSuccess(true);
				}
			});
			return result.isSuccess();
		}, ipLimiterDashboardConfigurationProperties.getMaxHttpRetryTimes(),
				ipLimiterDashboardConfigurationProperties.getHttpRetryIntervalTime());
		if (!checkResult) {
			log.warn("对ip-limiter-dashboard的master节点{}经过了{}次尝试后，健康检查都失败!", masterAddress,
					ipLimiterDashboardConfigurationProperties.getMaxHttpRetryTimes());
		}
		return checkResult;

	}

	@Override
	public boolean registerMasterAddress(String ipPort) {
		String lockKey = new StringBuilder(RedisKey.MASTER_KEY).toString();
		boolean isGetLock = RetryHelper.doRetryAction(() -> {
			return RedisTemplateWrapper.getLock(lockKey);
		});
		if (isGetLock) {
			try {
				RedisTemplateWrapper.set(RedisKey.MASTER_KEY, ipPort);
			} finally {
				RedisTemplateWrapper.delLock(lockKey);
			}
		}
		return false;
	}

}
