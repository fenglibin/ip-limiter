package com.eeeffff.limiter.core.timer;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.cache.Cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.eeeffff.limiter.common.constant.Constants;
import com.eeeffff.limiter.common.util.HttpClientUtil;
import com.eeeffff.limiter.common.vo.BlackIpVO;
import com.eeeffff.limiter.common.vo.ControlDataVO;
import com.eeeffff.limiter.common.vo.IpLimitVO;
import com.eeeffff.limiter.common.vo.WhiteIpVO;
import com.eeeffff.limiter.core.config.IpLimiterConfigurationProperties;
import com.eeeffff.limiter.core.config.SystemEnv;
import com.eeeffff.limiter.core.interceptor.IpQpsRateLimiter;

import lombok.extern.slf4j.Slf4j;

/**
 * 从服务定时同步当前集成服务的ＱＰＳ设置、黑名单设置
 * 
 * @author fenglibin
 *
 */
@Slf4j
@Service
public class IpLimitFetcher {
	@Autowired
	private IpLimiterConfigurationProperties ipLimiterConfigurationProperties;

	@Resource(name = "ipLimitCache")
	private Cache<String, Object> ipLimitCache;

	@PostConstruct
	public void init() {
		new Thread() {
			@SuppressWarnings({ "synthetic-access", "static-access" })
			public void run() {
				try {
					// 启动的时候等待１０秒，待系统所需要的资源初使化完成
					Thread.currentThread().sleep(10000);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				// 存放ＩＰ的QPS限制的配置
				ipLimitCache.put(Constants.IP_LIMIT_LOCAL_KEY, new HashMap<String, IpLimitVO>());
				ipLimitCache.put(Constants.IP_BLACK_LOCAL_KEY, new HashMap<String, BlackIpVO>());
				while (true) {
					updateConfig();
					/*
					 * updateIpQpsLimitFromRemote(); updateWhiteIpFromRemote();
					 * updateBlackIpFromRemote(); updatePerIpQpsLimit();
					 */
					// １分钟以后再次获取配置
					try {
						Thread.currentThread()
								.sleep(ipLimiterConfigurationProperties.getIpQpsLimitAndBlackIpUpdateTimeInterval());
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			}
		}.start();
	}

	/**
	 * 从远程更新IP及IP段的QPS设置
	 */
	private void updateIpQpsLimitFromRemote() {
		try {
			log.debug("开始更新IP及IP段的QPS...");
			String url = new StringBuilder("http://").append(ipLimiterConfigurationProperties.getDashboardAddress())
					.append("/limiter/client/getQpsLimit?appName=" + SystemEnv.getAppName()).toString();

			HttpClientUtil.doGet(url, r -> {
				if (r == null) {
					log.warn("获取IP及IP段的QPS设置的响应结果为空");
				}
				if (r.getCode() == 0 && r.getData() != null) {
					ipLimitCache.put(Constants.IP_LIMIT_LOCAL_KEY,
							JSON.parseObject(r.getData().toString(), new TypeReference<Map<String, IpLimitVO>>() {
							}));
				} else if (r.getCode() != 0) {
					log.warn("获取IP及IP段的QPS设置发生异常，响应code：" + r.getCode() + "，响应Msg：" + r.getMsg());
				}
			});
			log.debug("完成更新IP及IP段的QPS");
		} catch (Exception e) {
			log.error("更新IP及IP段的QPS发生异常:" + e.getMessage(), e);
		}
	}

	/**
	 * 从远程更新IP白名单设置
	 */
	private void updateWhiteIpFromRemote() {
		try {
			log.debug("开始更新IP白名单...");
			String url = new StringBuilder("http://").append(ipLimiterConfigurationProperties.getDashboardAddress())
					.append("/limiter/client/getWhiteIp?appName=" + SystemEnv.getAppName()).toString();

			HttpClientUtil.doGet(url, r -> {
				if (r == null) {
					log.warn("获取IP白名单的响应结果为空");
				}
				if (r.getCode() == 0 && r.getData() != null) {
					ipLimitCache.put(Constants.IP_WHITE_LOCAL_KEY,
							JSON.parseObject(r.getData().toString(), new TypeReference<Map<String, WhiteIpVO>>() {
							}));
				} else if (r.getCode() != 0) {
					log.warn("获取IP黑名单发生异常，响应code：" + r.getCode() + "，响应Msg：" + r.getMsg());
				}
			});
			log.debug("完成更新IP白名单");
		} catch (Exception e) {
			log.error("更新IP白名单发生异常:" + e.getMessage(), e);
		}
	}

	/**
	 * 从远程更新IP黑名单设置
	 */
	private void updateBlackIpFromRemote() {
		try {
			log.debug("开始更新IP黑名单...");
			String url = new StringBuilder("http://").append(ipLimiterConfigurationProperties.getDashboardAddress())
					.append("/limiter/client/getBlackIp?appName=" + SystemEnv.getAppName()).toString();

			HttpClientUtil.doGet(url, r -> {
				if (r == null) {
					log.warn("获取IP黑名单的响应结果为空");
				}
				if (r.getCode() == 0 && r.getData() != null) {
					ipLimitCache.put(Constants.IP_BLACK_LOCAL_KEY,
							JSON.parseObject(r.getData().toString(), new TypeReference<Map<String, BlackIpVO>>() {
							}));
				} else if (r.getCode() != 0) {
					log.warn("获取IP黑名单发生异常，响应code：" + r.getCode() + "，响应Msg：" + r.getMsg());
				}
			});
			log.debug("完成更新IP黑名单");
		} catch (Exception e) {
			log.error("更新IP黑名单发生异常:" + e.getMessage(), e);
		}
	}

	/**
	 * 从远程全局设置更新IP的全局QPS设置。<br>
	 * １、如果当前应用没有设置默认IP的QPS，则使用全局的默认IP的QPS；<br>
	 * ２、如果当前应用有设置默认IP的QPS，则使用当前应用的默认IP的QPS；<br>
	 * ３、如果没有设置全局默认IP的QPS，则使用应用配置文件中指定的默认IP的QPS;<br>
	 */
	private void updatePerIpQpsLimit() {
		try {
			log.debug("开始更新IP的默认最大ＱＰＳ...");
			String url = new StringBuilder("http://").append(ipLimiterConfigurationProperties.getDashboardAddress())
					.append("/limiter/client/getDefaultIpMaxQps?appName=" + SystemEnv.getAppName()).toString();

			HttpClientUtil.doGet(url, r -> {
				if (r == null) {
					log.warn("获取IP的默认最大ＱＰＳ结果为空");
				}
				if (r.getCode() == 0 && r.getData() != null) {
					int ipDefaultMaxQps = Integer.parseInt(String.valueOf(r.getData()));
					if (IpQpsRateLimiter.getPermitsPerSecondEachIp() != ipDefaultMaxQps) {
						IpQpsRateLimiter.resetLimit(ipDefaultMaxQps);
					}
				} else if (r.getCode() != 0) {
					log.warn("获取IP黑名单发生异常，响应code：" + r.getCode() + "，响应Msg：" + r.getMsg());
				}
			});
			log.debug("完成更新IP的默认最大ＱＰＳ");
		} catch (Exception e) {
			log.error("更新IP及IP段的QPS发生异常:" + e.getMessage(), e);
		}
	}

	/**
	 * 从控制台获取最新的配置
	 */
	private void updateConfig() {

		try {
			log.debug("开始从控制台获取最新的配置...");
			String url = new StringBuilder("http://").append(ipLimiterConfigurationProperties.getDashboardAddress())
					.append("/limiter/client/getIpConfig?appName=" + SystemEnv.getAppName()).toString();

			HttpClientUtil.doGet(url, r -> {
				if (r == null) {
					log.warn("从控制台获取最新的配置的响应结果为空.");
				}
				if (r.getCode() == 0 && r.getData() != null) {
					ControlDataVO ipConfig = JSON.parseObject(r.getData().toString(),
							new TypeReference<ControlDataVO>() {
							});

					if (!CollectionUtils.isEmpty(ipConfig.getQpsLimit())) {
						ipLimitCache.put(Constants.IP_LIMIT_LOCAL_KEY, ipConfig.getQpsLimit());
						log.debug("完成更新IP及IP段的QPS.");
					}

					if (!CollectionUtils.isEmpty(ipConfig.getWhiteIp())) {
						ipLimitCache.put(Constants.IP_WHITE_LOCAL_KEY, ipConfig.getWhiteIp());
						log.debug("完成更新IP白名单.");
					}

					if (!CollectionUtils.isEmpty(ipConfig.getBlackIp())) {
						ipLimitCache.put(Constants.IP_BLACK_LOCAL_KEY, ipConfig.getBlackIp());
						log.debug("完成更新IP黑名单.");
					}

					if (IpQpsRateLimiter.getPermitsPerSecondEachIp() != ipConfig.getDefaultIpMaxQps()) {
						IpQpsRateLimiter.resetLimit(ipConfig.getDefaultIpMaxQps());
						log.debug("完成更新IP的默认最大QPS.");
					}
				} else if (r.getCode() != 0) {
					log.warn("从控制台获取最新的配置，响应code：" + r.getCode() + "，响应Msg：" + r.getMsg());
				}
			});
			log.debug("完成从控制台获取最新的配置.");
		} catch (Exception e) {
			log.error("从控制台获取最新的配置发生异常:" + e.getMessage(), e);
		}

	}
}
