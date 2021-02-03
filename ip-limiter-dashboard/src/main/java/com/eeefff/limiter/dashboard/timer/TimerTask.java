package com.eeefff.limiter.dashboard.timer;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.eeefff.limiter.common.constant.Constants;
import com.eeefff.limiter.common.enumeration.BlackIpAddType;
import com.eeefff.limiter.common.enumeration.BlackIpLimitType;
import com.eeefff.limiter.common.util.HttpClientUtil;
import com.eeefff.limiter.common.util.RetryHelper;
import com.eeefff.limiter.common.vo.BlackIpVO;
import com.eeefff.limiter.common.vo.Result;
import com.eeefff.limiter.dashboard.client.IClientService;
import com.eeefff.limiter.dashboard.config.IpLimiterDashboardConfigurationProperties;
import com.eeefff.limiter.dashboard.config.LimiterResource;
import com.eeefff.limiter.dashboard.master.IMasterService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TimerTask {

	@Autowired
	private IClientService clientService;
	@Autowired
	private IMasterService masterService;
	@Autowired
	private LimiterResource limiterResource;
	@Autowired
	IpLimiterDashboardConfigurationProperties ipLimiterDashboardConfigurationProperties;
	@Value("${server.port:#{8080}}")
	private int serverPort;
	private String ipPort;

	@PostConstruct
	public void init() {
		ipPort = ipLimiterDashboardConfigurationProperties.getIpAddress() + ":" + serverPort;
		log.info("当前节点的ＩＰ及端口：" + ipPort);
		healthCheckThread();
		systemAddBlackIpCheckThread();
	}

	void healthCheckThread() {
		Thread thread = new Thread() {
			@SuppressWarnings("synthetic-access")
			public void run() {
				while (true) {
					try {
						TimeUnit.MINUTES.sleep(ipLimiterDashboardConfigurationProperties.getAppClientHealthCheckRate());
						if (isMaster()) {
							healthCheck();
						}
					} catch (Exception e) {
						log.error("执行应用节点的健康检查发生异常:" + e.getMessage(), e);
					}
				}
			}
		};
		thread.setDaemon(true);
		thread.setName("App client health check");
		thread.start();
	}

	/**
	 * 判断当前节点是否master节点，并同时会对master节点进行检查，如果健康检查不通过，
	 * 则会竟争为master节点，如果竟争成功则自己为master节点，可以执行master节点需要执行的定时任务， 否则不执行。
	 * 
	 * @return
	 */
	private boolean isMaster() {
		String masterAddress = masterService.getMasterAddress();
		boolean health = masterService.checkMasterHealth(masterAddress);
		if (!health) {
			masterService.registerMasterAddress(ipPort);
			masterAddress = masterService.getMasterAddress();
		}
		if (ipPort.equals(masterAddress)) {
			return true;
		}
		return false;
	}

	void systemAddBlackIpCheckThread() {
		Thread thread = new Thread() {
			@SuppressWarnings("synthetic-access")
			public void run() {
				while (true) {
					try {
						TimeUnit.MINUTES
								.sleep(ipLimiterDashboardConfigurationProperties.getSystemAddBlackIpCheckRate());
						if (isMaster()) {
							systemAddBlackIpCheck();
						}
					} catch (Exception e) {
						log.error("执行应用节点的健康检查发生异常:" + e.getMessage(), e);
					}
				}
			}
		};
		thread.setDaemon(true);
		thread.setName("App client health check");
		thread.start();
	}

	/**
	 * 对所有应用的节点进行健康检查，应用没有节点的会被删除，应用的节点健康检查不通过的，该节点也会被删除
	 */
	private void healthCheck() {
		List<Object> appNameList = clientService.getAllAppNames();
		log.info("开始对应用节点[{}]执行健康检查",appNameList);
		appNameList.forEach(o -> {
			String appName = String.valueOf(o);
			List<String> appIpList = clientService.getAppRegisteredIps(appName);
			if (CollectionUtils.isEmpty(appIpList)) {
				log.warn("应用[{}]没有对应的应用客户端连接注册，将会将其从应用注册中心删除！", appName);
				clientService.delApp(appName);
			} else {
				appIpList.forEach(server -> {
					log.debug("开始对应用{}的节点{}执行健康检查.", appName, server);
					String url = new StringBuilder("http://").append(server).append("/ip-limiter/health/check")
							.toString();
					boolean checkResult = RetryHelper.doRetryAction(() -> {
						Result<?> result = Result.empty();
						HttpClientUtil.doGet(url.toString(), r -> {
							if (r != null && r.getCode() == 0) {// 调用健康检查URL成功
								result.setSuccess(true);
							}
						});
						return result.isSuccess();
					}, ipLimiterDashboardConfigurationProperties.getMaxHttpRetryTimes(), 1000);
					if (!checkResult) {
						log.warn("应用{}的客户端{}经过了{}次尝试后，健康检查都是失败的，该客户端{}将会从该应用中删除，可重启应用或调用应用的接口重新注册该客户端", appName, server,
								ipLimiterDashboardConfigurationProperties.getMaxHttpRetryTimes(), server);
						clientService.delAppClient(appName, server);
					} else {
						log.debug("应用{}的节点{}健康检查结果为成功通过.", appName, server);
					}
				});
			}
		});
		log.info("对应用节点执行健康检查结束.");
	}

	/**
	 * 对超限访问被系统自动加入到黑名单中的IP进行处理
	 */
	private void systemAddBlackIpCheck() {
		List<Object> appNameList = limiterResource.getClientService().getAllAppNames();
		if (CollectionUtils.isEmpty(appNameList)) {
			return;
		}
		long now = System.currentTimeMillis();
		appNameList.forEach(o -> {
			String appName = String.valueOf(o);
			List<BlackIpVO> blackIpList = limiterResource.getBlackIpLimiter().getAllBlackIps(appName);
			if (CollectionUtils.isEmpty(blackIpList)) {
				return;
			}
			blackIpList.forEach(blackIp -> {
				if (BlackIpAddType.SYSTEM != blackIp.getAddType()) {// 只处理系统自动增加的IP
					return;
				}
				log.info("对由于超限访问系统[{}]在[{}]被加入到黑名单中的IP[{}]进行检测", appName, blackIp.getAddDate(), blackIp.getIp());
				if (BlackIpLimitType.MINUTE == blackIp.getLimitType()) {
					if (now - blackIp.getAddDate().getTime() > Constants.Time.MINUTE_MILLS) {
						if (blackIp.getCheckTimes() < 1) {
							log.info("当前IP[{}]的LimitType为[{}]，当前checkTimes为[{}]，本次不从IP黑名单中清除该IP，只将其checkTimes加１",
									blackIp.getIp(), blackIp.getLimitType(), blackIp.getCheckTimes());
							blackIp.setCheckTimes(blackIp.getCheckTimes() + 1);
							limiterResource.getBlackIpLimiter().updateBlackIp(appName, blackIp.getIp(), blackIp);
						} else {
							// 已经加入１分钟了，如果LimitType没有改变，说明后面无超限访问，可以将其从黑名单中移除了
							log.info("IP[{}]已经加入１分钟了，LimitType为[{}]且没有改变，说明后面无超限访问，可以将其从黑名单中移除了", blackIp.getIp(),
									BlackIpLimitType.MINUTE);
							limiterResource.getBlackIpLimiter().delBlackIp(appName, blackIp.getIp());
						}
					}
				} else if (BlackIpLimitType.HOUR == blackIp.getLimitType()) {
					if (now - blackIp.getAddDate().getTime() > Constants.Time.HOUR_MILLS) {
						// 已经加入１小时了，如果LimitType没有改变，说明后面无超限访问，可以将其从黑名单中移除了
						log.info("IP[{}]已经加入１小时了，LimitType为[{}]且没有改变，说明后面无超限访问，可以将其从黑名单中移除了", blackIp.getIp(),
								BlackIpLimitType.HOUR);
						limiterResource.getBlackIpLimiter().delBlackIp(appName, blackIp.getIp());
					}
				} else if (BlackIpLimitType.DAY == blackIp.getLimitType()) {
					if (now - blackIp.getAddDate().getTime() > Constants.Time.DAY_MILLS) {
						// 已经加入１天了，如果LimitType没有改变，说明后面无超限访问，可以将其从黑名单中移除了
						log.info("IP[{}]已经加入１天了，LimitType为[{}]且没有改变，说明后面无超限访问，可以将其从黑名单中移除了", blackIp.getIp(),
								BlackIpLimitType.DAY);
						limiterResource.getBlackIpLimiter().delBlackIp(appName, blackIp.getIp());
					}
				}

			});
		});

	}
}
