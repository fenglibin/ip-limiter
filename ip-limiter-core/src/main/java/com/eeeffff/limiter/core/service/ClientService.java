package com.eeeffff.limiter.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.eeeffff.limiter.common.util.HttpClientUtil;
import com.eeeffff.limiter.common.util.RetryHelper;
import com.eeeffff.limiter.common.vo.Result;
import com.eeeffff.limiter.core.config.IpLimiterConfigurationProperties;
import com.eeeffff.limiter.core.config.SystemEnv;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ClientService {

	@Value("${server.port:#{8080}}")
	private int serverPort;

	@Autowired
	private BlackIpService blackIpService;
	@Autowired
	private LimitIpService limitIpService;

	@Autowired
	private IpLimiterConfigurationProperties ipLimiterConfigurationProperties;

	/**
	 * 往ＩＰ控制台发起注册
	 * 
	 * @return
	 */
	public boolean registerToServer() {
		String url = new StringBuilder("http://").append(ipLimiterConfigurationProperties.getDashboardAddress())
				.append("/limiter/client/register?appName=").append(SystemEnv.getAppName()).append("&ip=")
				.append(ipLimiterConfigurationProperties.getServerAddress()).append("&port=").append(serverPort)
				.toString();
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
			log.warn("应用{}往服务控制台注册失败!", SystemEnv.getAppName());
		} else {
			log.info("应用{}往服务控制台注册成功.", SystemEnv.getAppName());
		}
		return httpResult;
	}

	/**
	 * 将访问当前应用的指定ＩＰ增加到黑名单中，会影响到该ＩＰ对该应用的访问，但是不影响到该ＩＰ对其它应用的访问
	 * 
	 * @param ip
	 * @return 是否加入到黑名单中成功
	 */
	public boolean addBlackIp(String ip) {
		return blackIpService.addBlackIp(ip);
	}

	/**
	 * 设置IP访问的限制ＱＰＳ
	 * 
	 * @param ip
	 * @param limit 限制ＱＰＳ
	 * @return 是否操作成功
	 */
	public boolean setIpLimit(String ip, int limit) {
		return limitIpService.addLimitIp(ip, limit);
	}
}
