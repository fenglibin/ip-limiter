package com.eeefff.limiter.core.timer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.cache.Cache;
import javax.cache.Cache.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.eeefff.limiter.common.constant.Constants;
import com.eeefff.limiter.common.util.HttpAsyncClientUtil;
import com.eeefff.limiter.common.util.HttpClientUtil;
import com.eeefff.limiter.common.util.TimeUtil;
import com.eeefff.limiter.common.vo.AccessVO;
import com.eeefff.limiter.core.cache.IpCacheHelper;
import com.eeefff.limiter.core.cache.MinuteIpCacheHelper;
import com.eeefff.limiter.core.config.IpLimiterConfigurationProperties;
import com.eeefff.limiter.core.config.SystemEnv;
import com.eeefff.limiter.core.service.ClientService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class IpAccessReporter {

	@Value("${server.port:#{8080}}")
	private int serverPort;

	@Autowired
	private IpLimiterConfigurationProperties ipLimiterConfigurationProperties;
	@Autowired
	private ClientService clientRegisterService;

	@Resource(name = "ipCache")
	private Cache<String, Object> cache;

	@Resource(name = "commonCache")
	private Cache<String, Object> commonCache;

	@Resource(name = "ipCacheHelper")
	private IpCacheHelper ipCacheHelper;

	@PostConstruct
	public void init() {
		initHttpClient();
		registerToServer();
		secondDataHandleThread();
		minuteDataHandleThread();
	}

	private void initHttpClient() {
		HttpAsyncClientUtil.init(ipLimiterConfigurationProperties.getConnectTimeout(),
				ipLimiterConfigurationProperties.getSoTimeout(), ipLimiterConfigurationProperties.getMaxConnTotal(),
				ipLimiterConfigurationProperties.getMaxConnPerRoute());
		HttpClientUtil.init(ipLimiterConfigurationProperties.getConnectTimeout(),
				ipLimiterConfigurationProperties.getSoTimeout(), ipLimiterConfigurationProperties.getMaxConnTotal(),
				ipLimiterConfigurationProperties.getMaxConnPerRoute());
	}

	/**
	 * 往控制台注册
	 */
	private void registerToServer() {
		Thread thread = new Thread(new Runnable() {
			@SuppressWarnings("synthetic-access")
			public void run() {
				try {
					// 等10秒再往服务器注册
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				clientRegisterService.registerToServer();
			}
		});
		thread.setDaemon(true);
		thread.setName("register-to-server-thread");
		thread.start();

	}

	private void secondDataHandleThread() {
		Thread secondAccessHandleThread = new Thread(new Runnable() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void run() {
				while (true) {
					try {
						log.debug("（秒纬度）暂停清理超期数据：" + ipLimiterConfigurationProperties.getSecondsMetricReportInterval()
								+ "秒");
						TimeUnit.SECONDS.sleep(ipLimiterConfigurationProperties.getSecondsMetricReportInterval());
						log.debug("（秒纬度）开始清理超期数据");
						// secondReport();
						secondsAccessHandler();
					} catch (Throwable e) {
						log.error("秒纬度清理超期数据发生异常：" + e.getMessage(), e);
					}
				}
			}
		});
		secondAccessHandleThread.setDaemon(true);
		secondAccessHandleThread.setName("ip-limiter-second-access-handler-thread");
		secondAccessHandleThread.start();
	}

	private void minuteDataHandleThread() {
		Thread minuteAccessHandleThread = new Thread(new Runnable() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void run() {
				while (true) {
					try {
						log.debug("（分纬度）暂停汇报数据１分钟");
						TimeUnit.MINUTES.sleep(1);
						log.debug("（分纬度）开始报告数据");
						minuteReport();
					} catch (Throwable e) {
						log.error(e.getMessage(), e);
					}
				}
			}
		});
		minuteAccessHandleThread.setDaemon(true);
		minuteAccessHandleThread.setName("ip-limiter-minute-access-reporter-thread");
		minuteAccessHandleThread.start();
	}

	/**
	 * 清理本地超过最大缓存时长(secondsMetricLocalKeeped)的、以秒为统计纬度的访问行为数据
	 */
	private void secondsAccessHandler() {
		long currentTimeSeconds = TimeUtil.currentTimeSeconds();
		// 获取本地缓存中记录的ＩＰ访问行为的ＩＰ列表
		Iterator<Entry<String, Object>> ipList = cache.iterator();
		if (!ipList.hasNext()) {
			return;
		}
		// 外层Map的Key为IP，内层Map的Key为代表访问时间的秒
		Map<String, HashMap<Long, AccessVO>> ipsAccessMap = new HashMap<String, HashMap<Long, AccessVO>>();
		ipList.forEachRemaining(e -> {
			// 获取当前IP的访问行为记录
			@SuppressWarnings("unchecked")
			HashMap<Long, AccessVO> ipAccessMap = (HashMap<Long, AccessVO>) e.getValue();
			// 如果该IP没有访问行为，则将该IP从缓存中删除
			if (ipAccessMap.isEmpty()) {
				cache.remove(e.getKey());
			}
			if(CollectionUtils.isEmpty(ipAccessMap.keySet())) {
				return;
			}
			Set<Long> seconds = new TreeSet<Long>();
			// 保存该ＩＰ访问对应的所有秒
			seconds.addAll(ipAccessMap.keySet());
			seconds.forEach(second -> {
				// 处理最多保留超过了最大保留时间(secondsMetricLocalKeeped)的数据
				if (second < currentTimeSeconds - ipLimiterConfigurationProperties.getSecondsMetricLocalKeeped()) {

					if (currentTimeSeconds - second == 1) {// 最后一秒的数据
						HashMap<Long, AccessVO> _ipMap = ipsAccessMap.get(e.getKey());
						if (_ipMap == null) {
							_ipMap = new HashMap<Long, AccessVO>();
							ipsAccessMap.put(e.getKey(), _ipMap);
						}
						_ipMap.put(second, ipAccessMap.get(second));
					}

					log.debug("清理秒[{}]对应的时间，当前时间对应的秒[{}]", second, currentTimeSeconds);
					// 从本地缓存中删除该ＩＰ的这一秒的数据
					ipAccessMap.remove(second);
				}
			});
		});
		// 将访问情况保存到本地缓存中
		Map<Long, Map<String, HashMap<Long, AccessVO>>> secondAccess = new HashMap<Long, Map<String, HashMap<Long, AccessVO>>>();
		secondAccess.put(currentTimeSeconds, ipsAccessMap);
		ipCacheHelper.addSecondAccessQueue(secondAccess);
	}

	/**
	 * 将以秒钟为统计维度的数据进行上报处理
	 */
	@Deprecated
	private void secondReport() {
		long currentTimeSeconds = TimeUtil.currentTimeSeconds();
		// 外层Map的Key为IP，内层Map的Key为代表访问时间的秒
		Map<String, HashMap<Long, AccessVO>> ipsAccessMap = new HashMap<String, HashMap<Long, AccessVO>>();
		Map<String, HashMap<Long, AccessVO>> lastSecondIpsAccessMap = new HashMap<String, HashMap<Long, AccessVO>>();
		// 获取本地缓存中记录的ＩＰ访问行为的ＩＰ列表
		Iterator<Entry<String, Object>> ipList = cache.iterator();
		if (!ipList.hasNext()) {
			return;
		}
		ipList.forEachRemaining(e -> {
			// 获取当前ＩＰ的访问行为记录
			@SuppressWarnings("unchecked")
			HashMap<Long, AccessVO> ipAccessMap = (HashMap<Long, AccessVO>) e.getValue();
			Set<Long> seconds = new TreeSet<Long>();
			// 保存该ＩＰ访问对应的所有秒
			seconds.addAll(ipAccessMap.keySet());
			seconds.forEach(second -> {
				// 处理小于当前系统时间秒之前的数据
				if (second < currentTimeSeconds) {
					HashMap<Long, AccessVO> _ipMap = ipsAccessMap.get(e.getKey());
					if (_ipMap == null) {
						_ipMap = new HashMap<Long, AccessVO>();
						ipsAccessMap.put(e.getKey(), _ipMap);
					}
					_ipMap.put(second, ipAccessMap.get(second));

					// 最后一秒钟的访问请求(开始)
					if (currentTimeSeconds - second == 1) {
						HashMap<Long, AccessVO> _lastSecondIpMap = lastSecondIpsAccessMap.get(e.getKey());
						if (_lastSecondIpMap == null) {
							_lastSecondIpMap = new HashMap<Long, AccessVO>();
							lastSecondIpsAccessMap.put(e.getKey(), _lastSecondIpMap);
						}
						_lastSecondIpMap.put(second, ipAccessMap.get(second));
						lastSecondIpsAccessMap.put(e.getKey(), _lastSecondIpMap);
					}
					// 最后一秒钟的访问请求(结束)

					// 从本地缓存中删除该ＩＰ的这一秒的数据
					ipAccessMap.remove(second);
				}
			});
		});
		if (ipsAccessMap.isEmpty()) {
			return;
		}
		// 将访问情况保存到本地缓存中
		Map<Long, Map<String, HashMap<Long, AccessVO>>> minuteAccess = new HashMap<Long, Map<String, HashMap<Long, AccessVO>>>();
		minuteAccess.put(currentTimeSeconds, ipsAccessMap);
		ipCacheHelper.addMinuteAccessQueue(minuteAccess);
		// 保存最后一秒钟的访问数据
		if (!lastSecondIpsAccessMap.isEmpty()) {
			commonCache.put(Constants.LocalCacheKey.LAST_SECOND_IPS_ACCESSMAP, lastSecondIpsAccessMap);
		}
		String url = new StringBuilder("http://").append(ipLimiterConfigurationProperties.getDashboardAddress())
				.append("/limiter/client/saveSecondAccessMetric?appName=" + SystemEnv.getAppName()).append("&ip=")
				.append(ipLimiterConfigurationProperties.getServerAddress()).append("&port=").append(serverPort)
				.toString();
		HttpAsyncClientUtil.doPost(JSON.toJSONString(ipsAccessMap), url, r -> {
			if (r != null && r.getCode() == 0) {
				log.debug("往控制台上报「秒纬度」的数据成功。");
			} else {
				log.error("往控制台上报「秒纬度」的数据失败：" + (r == null ? "" : r.getMsg()));
			}
		});
	}

	/**
	 * 将以分钟为统计维度的数据进行上报处理，并将上报的数据从记录缓存中删除
	 */
	void minuteReport() {
		long currentTimeMinutes = TimeUtil.currentTimeMinutes();
		final List<AccessVO> accessList = new ArrayList<AccessVO>();
		// 组装当前最后一分钟的ＩＰ访问数据
		Map<String, HashMap<Long, AccessVO>> ipsAccessReportMap = new HashMap<String, HashMap<Long, AccessVO>>();
		// 外层Map的Key为IP，内层Map的Key为代表访问时间的分
		Map<String, HashMap<Long, AccessVO>> ipsAccessMap = MinuteIpCacheHelper.getMinuteAccessCache();
		Set<String> keys = ipsAccessMap.keySet();
		keys.forEach(key -> {
			HashMap<Long, AccessVO> accessMap = ipsAccessMap.get(key);
			if (accessMap == null || accessMap.isEmpty()) {
				// 清除最近没有访问的IP，减少IP的内存占用
				ipsAccessMap.remove(key);
			} else {
				HashMap<Long, AccessVO> minuteMap = new HashMap<Long, AccessVO>();
				Set<Long> set = new TreeSet<Long>();
				set.addAll(accessMap.keySet());
				set.forEach(k -> {
					// 获取当前ＩＰ上一分钟的访问数据
					if (k < currentTimeMinutes) {
						AccessVO accessVO = accessMap.get(k);
						accessList.add(accessVO);
						minuteMap.put(k, accessVO);
						// 从内存中删除，避免内存溢出
						accessMap.remove(k);
					}
				});
				if (!CollectionUtils.isEmpty(minuteMap)) {
					ipsAccessReportMap.put(key, minuteMap);
				}
			}
		});
		if (CollectionUtils.isEmpty(ipsAccessReportMap)) {
			return;
		}
		// 将访问情况保存到本地缓存中
		Map<Long, Map<String, HashMap<Long, AccessVO>>> minuteAccess = new HashMap<Long, Map<String, HashMap<Long, AccessVO>>>();
		minuteAccess.put(currentTimeMinutes, ipsAccessReportMap);
		ipCacheHelper.addMinuteAccessQueue(minuteAccess);

		String url = new StringBuilder("http://").append(ipLimiterConfigurationProperties.getDashboardAddress())
				.append("/limiter/client/saveMinuteAccessMetric?appName=" + SystemEnv.getAppName()).append("&ip=")
				.append(ipLimiterConfigurationProperties.getServerAddress()).append("&port=").append(serverPort)
				.toString();
		log.debug("往控制台上报「分纬度」的数据:" + JSON.toJSONString(ipsAccessReportMap));
		HttpAsyncClientUtil.doPost(JSON.toJSONString(ipsAccessReportMap), url, r -> {
			if (r != null && r.getCode() == 0) {
				log.debug("往控制台上报「分纬度」的数据成功。");
			} else {
				log.error("往控制台上报「分纬度」的数据失败：" + (r == null ? "" : r.getMsg()));
			}
		});
	}
}
