package com.eeeffff.limiter.dashboard.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.eeeffff.limiter.common.constant.Constants;
import com.eeeffff.limiter.common.util.IpHelper;
import com.eeeffff.limiter.common.vo.AccessVO;
import com.eeeffff.limiter.common.vo.BlackIpVO;
import com.eeeffff.limiter.common.vo.ControlDataVO;
import com.eeeffff.limiter.common.vo.IpLimitVO;
import com.eeeffff.limiter.common.vo.Result;
import com.eeeffff.limiter.common.vo.WhiteIpVO;
import com.eeeffff.limiter.dashboard.config.IpLimiterDashboardConfigurationProperties;
import com.eeeffff.limiter.dashboard.config.LimiterResource;

import lombok.extern.slf4j.Slf4j;

/**
 * 处理应用客户端与控制台交互的请求
 * 
 * @author fenglibin
 *
 */
@Slf4j
@Controller
@RequestMapping(value = "/limiter/client", produces = MediaType.APPLICATION_JSON_VALUE)
public class ClientController {
	@Autowired
	private LimiterResource limiterResource;
	@Autowired
	private IpLimiterDashboardConfigurationProperties ipLimiterConfigurationProperties;

	/**
	 * 应用服务注册
	 * 
	 * @param appName 上报数据的应用名称
	 * @param ip      上报数据的应用当前节点的ＩＰ
	 * @param port    上报数据的应用对外提供服务在端口
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/register")
	public Result<String> register(@RequestParam(required = true) String appName,
			@RequestParam(required = true) String ip, @RequestParam(required = true) int port) {
		log.info("服务注册，appName:" + appName + ",ip:" + ip + ", port:" + port);
		boolean result = limiterResource.getClientService().saveClient(appName, ip, port);
		if (result) {
			return Result.ofSuccessMsg("SUCC");
		}
		return Result.ofFail("FAIL");
	}

	/**
	 * 对访问流量进行排序
	 * 
	 * @param metrics
	 * @return
	 */
	private Stream<AccessVO> sortMetric(Map<String, HashMap<Long, AccessVO>> metrics) {
		if (metrics == null || metrics.isEmpty()) {
			return null;
		}
		List<AccessVO> accessList = new ArrayList<AccessVO>();
		metrics.values().stream().collect(Collectors.toList()).forEach(e -> {
			accessList.addAll(e.values().stream().collect(Collectors.toList()));
		});
		// 按访问量进行排序，并取访问量最多的指定个IP进行上报，需要上报的访问量最多的IP数，可以在配置文件中的maxTopAccessIps属性中指定
		return accessList.stream().sorted((o1, o2) -> {
			if (o1.getTotal().longValue() < o2.getTotal().longValue()) {
				return 1;
			} else if (o1.getTotal().longValue() > o2.getTotal().longValue()) {
				return -1;
			} else {
				return 0;
			}
		});

	}

	/**
	 * 保存这一段时间的记录的不同IP的访问数据
	 * 
	 * @param appName 上报数据的应用名称
	 * @param ip      上报数据的应用当前节点的ＩＰ
	 * @param port    上报数据的应用对外提供服务在端口
	 * @param metric  上报的这一段时间的记录的不同IP的访问数据
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/saveSecondAccessMetric")
	public Result<String> saveSecondAccessMetric(@RequestParam(required = true) String appName,
			@RequestParam(required = true) String ip, @RequestParam(required = true) int port,
			@RequestParam(required = true) String metric) {
		log.info("（秒纬度）应用 " + appName + " 上报的数据：" + metric.toString());
		try {
			Map<String, HashMap<Long, AccessVO>> metricMap = JSON.parseObject(metric,
					new TypeReference<Map<String, HashMap<Long, AccessVO>>>() {
					});
			if (CollectionUtils.isEmpty(metricMap)) {
				return Result.ofSuccessMsg("本次没有需要保存的秒纬度访问数据！");
			}
			String ipPort = new StringBuilder(ip).append(":").append(port).toString();
			Stream<AccessVO> sortedMetricStream = sortMetric(metricMap);
			// 按访问量进行排序，并取访问量最多的指定个IP进行上报，需要上报的访问量最多的IP数，可以在配置文件中的maxTopAccessIps属性中指定
			List<AccessVO> topAccessMetricList = sortedMetricStream
					.limit(ipLimiterConfigurationProperties.getMaxTopAccessIps()).collect(Collectors.toList());
			// 当给前应用增加访问流量
			limiterResource.getSecondsAccessMetricHandler().handleAccessMetric(appName, ipPort, topAccessMetricList);
			// 给总的流量增加访问流量
			limiterResource.getGlobalSecondsAccessMetricHandler().handleAccessMetric(appName, ipPort,
					topAccessMetricList);
			return Result.ofSuccess("SUCC");
		} catch (Exception e) {
			log.error("保存秒纬度访问数据失败:" + e.getMessage(), e);
			return Result.ofFail("保存秒纬度访问数据失败！");
		}
	}

	/**
	 * 保存这一段时间的记录的不同IP的访问数据
	 * 
	 * @param appName 上报数据的应用名称
	 * @param ip      上报数据的应用当前节点的ＩＰ
	 * @param port    上报数据的应用对外提供服务在端口
	 * @param metric  上报的这一段时间的记录的不同IP的访问数据
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/saveMinuteAccessMetric")
	public Result<String> saveMinuteAccessMetric(@RequestParam(required = true) String appName,
			@RequestParam(required = true) String ip, @RequestParam(required = true) int port,
			@RequestParam(required = true) String metric) {
		log.info("（分纬度）应用 " + appName + " 上报的访问数据.");
		log.debug("（分纬度）应用 " + appName + " 上报的数据：" + metric.toString());
		try {
			// 外层Map的Key为IP，内层Map的Key为代表访问时间的分
			Map<String, HashMap<Long, AccessVO>> metricMap = JSON.parseObject(metric,
					new TypeReference<Map<String, HashMap<Long, AccessVO>>>() {
					});
			if (CollectionUtils.isEmpty(metricMap)) {
				return Result.ofSuccessMsg("本次没有需要保存的分纬度访问数据！");
			}
			String ipPort = new StringBuilder(ip).append(":").append(port).toString();
			Stream<AccessVO> sortedMetricStream = sortMetric(metricMap);
			// 按访问量进行排序，并取访问量最多的指定个IP进行上报，需要上报的访问量最多的IP数，可以在配置文件中的maxTopAccessIps属性中指定
			List<AccessVO> topAccessMetricList = sortedMetricStream
					.limit(ipLimiterConfigurationProperties.getMaxTopAccessIps()).collect(Collectors.toList());
			// 当给前应用增加访问流量
			limiterResource.getMinutesAccessMetricHandler().handleAccessMetric(appName, ipPort, topAccessMetricList);
			// 给总的流量增加访问流量
			limiterResource.getGlobalMinutesAccessMetricHandler().handleAccessMetric(appName, ipPort,
					topAccessMetricList);
			// 对超限访问的ＩＰ进行处理
			limiterResource.getOverLimitAccessHandler().handleOverLimitAccess(appName, topAccessMetricList);
			return Result.ofSuccess("SUCC");
		} catch (Exception e) {
			log.error("保存分纬度访问数据失败:" + e.getMessage(), e);
			return Result.ofFail("保存分纬度访问数据失败！");
		}
	}

	/**
	 * 根据应用名称，获取所有该应该所有的限流ＩＰ配置
	 * 
	 * @param appName 应用名称
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getQpsLimit")
	public Result<Map<String, IpLimitVO>> getQpsLimitByApp(@RequestParam(required = true) String appName) {
		try {
			log.debug("开始更新IP及IP段的QPS...");
			List<IpLimitVO> ipQpsLimitList = limiterResource.getIpQpsLimiter().updateIpQpsLimit(appName);
			List<IpLimitVO> globalIpQpsLimitList = limiterResource.getIpQpsLimiter()
					.updateIpQpsLimit(Constants.EMPTY_STRING);
			Map<String, IpLimitVO> map = new HashMap<String, IpLimitVO>();
			// 全局的ＩＰ限流配置
			if (!CollectionUtils.isEmpty(globalIpQpsLimitList)) {
				globalIpQpsLimitList.forEach(e -> {
					IpLimitVO o = (IpLimitVO) e;
					/*
					 * 去掉通配置符，将其变成单独的ip进行比较，提升比较的效率。<br>
					 * 如：将127.0.0.*修改为127.0.0，或将127.0.*.*修改为127.0。<br>
					 * 被比较的IP将会按照全IP比较，三段IP及二段IP比较，如IP127.0.0.1会首先获取127.0.0.1的
					 * QPS限制设置，如果取到则用，没有取到则取127.0.0的QPS限制设置，如果取到则用，没有取到则
					 * 取127.0的QPS限制设置，如果取到则用，没有取到则使用默认的限流规则
					 */
					map.put(IpHelper.removeIpWildcard(o.getIp()), o);
				});
			}
			// 当前应用的ＩＰ限流配置
			if (!CollectionUtils.isEmpty(ipQpsLimitList)) {
				ipQpsLimitList.forEach(e -> {
					IpLimitVO o = (IpLimitVO) e;
					/*
					 * 去掉通配置符，将其变成单独的ip进行比较，提升比较的效率。<br>
					 * 如：将127.0.0.*修改为127.0.0，或将127.0.*.*修改为127.0。<br>
					 * 被比较的IP将会按照全IP比较，三段IP及二段IP比较，如IP127.0.0.1会首先获取127.0.0.1的
					 * QPS限制设置，如果取到则用，没有取到则取127.0.0的QPS限制设置，如果取到则用，没有取到则
					 * 取127.0的QPS限制设置，如果取到则用，没有取到则使用默认的限流规则
					 */
					// 当前应用的配置高于全局的配置
					map.put(IpHelper.removeIpWildcard(o.getIp()), o);
				});
			}
			log.debug("完成更新IP及IP段的QPS");
			return Result.ofSuccess(map);
		} catch (Exception e) {
			log.error("更新IP及IP段的QPS发生异常:" + e.getMessage(), e);
			return Result.ofThrowable(-1, e);
		}
	}

	/**
	 * 获取指定应用所有的白名单，包括针对该应用设置的，以及全局的白名单
	 * 
	 * @param appName
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getWhiteIp")
	public Result<Map<String, WhiteIpVO>> getWhiteIpByApp(@RequestParam(required = true) String appName) {
		try {
			log.debug("开始获取IP白名单...");
			List<WhiteIpVO> whiteIpList = limiterResource.getWhiteIpLimiter().getAllWhiteIps(appName);
			List<WhiteIpVO> globalWhiteIpList = limiterResource.getWhiteIpLimiter()
					.getAllWhiteIps(Constants.EMPTY_STRING);
			Map<String, WhiteIpVO> map = new HashMap<String, WhiteIpVO>();
			// 当前应用的白名单设备
			if (!CollectionUtils.isEmpty(whiteIpList)) {
				whiteIpList.forEach(e -> {
					WhiteIpVO o = (WhiteIpVO) e;
					/*
					 * 去掉通配置符，将其变成单独的ip进行比较，提升比较的效率。<br>
					 * 如：将127.0.0.*修改为127.0.0，或将127.0.*.*修改为127.0。<br>
					 * 被比较的IP将会按照全IP比较，三段IP及二段IP比较，如IP127.0.0.1会首先获取127.0.0.1的
					 * QPS限制设置，如果取到则用，没有取到则取127.0.0的QPS限制设置，如果取到则用，没有取到则
					 * 取127.0的QPS限制设置，如果取到则用，没有取到则使用默认的限流规则
					 */
					map.put(IpHelper.removeIpWildcard(o.getIp()), o);
				});
			}
			// 全局的白名单设备
			if (!CollectionUtils.isEmpty(globalWhiteIpList)) {
				globalWhiteIpList.forEach(e -> {
					WhiteIpVO o = (WhiteIpVO) e;
					/*
					 * 去掉通配置符，将其变成单独的ip进行比较，提升比较的效率。<br>
					 * 如：将127.0.0.*修改为127.0.0，或将127.0.*.*修改为127.0。<br>
					 * 被比较的IP将会按照全IP比较，三段IP及二段IP比较，如IP127.0.0.1会首先获取127.0.0.1的
					 * QPS限制设置，如果取到则用，没有取到则取127.0.0的QPS限制设置，如果取到则用，没有取到则
					 * 取127.0的QPS限制设置，如果取到则用，没有取到则使用默认的限流规则
					 */
					map.put(IpHelper.removeIpWildcard(o.getIp()), o);
				});
			}
			return Result.ofSuccess(map);
		} catch (Exception e) {
			log.error("获取IP白名单发生异常:" + e.getMessage(), e);
			return Result.ofThrowable(-1, e);
		}
	}

	/**
	 * 获取指定应用所有的黑名单，包括针对该应用设置的，以及全局的黑名单
	 * 
	 * @param appName
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getBlackIp")
	public Result<Map<String, BlackIpVO>> getBlackIpByApp(@RequestParam(required = true) String appName) {
		try {
			log.debug("开始获取IP黑名单...");
			List<BlackIpVO> blackIpList = limiterResource.getBlackIpLimiter().getAllBlackIps(appName);
			List<BlackIpVO> globalBlackIpList = limiterResource.getBlackIpLimiter()
					.getAllBlackIps(Constants.EMPTY_STRING);
			Map<String, BlackIpVO> map = new HashMap<String, BlackIpVO>();
			// 当前应用的黑名单设备
			if (!CollectionUtils.isEmpty(blackIpList)) {
				blackIpList.forEach(e -> {
					BlackIpVO o = (BlackIpVO) e;
					/*
					 * 去掉通配置符，将其变成单独的ip进行比较，提升比较的效率。<br>
					 * 如：将127.0.0.*修改为127.0.0，或将127.0.*.*修改为127.0。<br>
					 * 被比较的IP将会按照全IP比较，三段IP及二段IP比较，如IP127.0.0.1会首先获取127.0.0.1的
					 * QPS限制设置，如果取到则用，没有取到则取127.0.0的QPS限制设置，如果取到则用，没有取到则
					 * 取127.0的QPS限制设置，如果取到则用，没有取到则使用默认的限流规则
					 */
					map.put(IpHelper.removeIpWildcard(o.getIp()), o);
				});
			}
			// 全局的黑名单设备
			if (!CollectionUtils.isEmpty(globalBlackIpList)) {
				globalBlackIpList.forEach(e -> {
					BlackIpVO o = (BlackIpVO) e;
					/*
					 * 去掉通配置符，将其变成单独的ip进行比较，提升比较的效率。<br>
					 * 如：将127.0.0.*修改为127.0.0，或将127.0.*.*修改为127.0。<br>
					 * 被比较的IP将会按照全IP比较，三段IP及二段IP比较，如IP127.0.0.1会首先获取127.0.0.1的
					 * QPS限制设置，如果取到则用，没有取到则取127.0.0的QPS限制设置，如果取到则用，没有取到则
					 * 取127.0的QPS限制设置，如果取到则用，没有取到则使用默认的限流规则
					 */
					map.put(IpHelper.removeIpWildcard(o.getIp()), o);
				});
			}
			return Result.ofSuccess(map);
		} catch (Exception e) {
			log.error("获取IP黑名单发生异常:" + e.getMessage(), e);
			return Result.ofThrowable(-1, e);
		}
	}

	@ResponseBody
	@RequestMapping("/getDefaultIpMaxQps")
	public Result<Integer> getDefaultIpMaxQps(@RequestParam(required = true) String appName) {
		// 获取指定应用IP默认的最大QPS设置
		Integer ipDefaultMaxQps = limiterResource.getIpQpsLimiter().getDefaultIpMaxQps(appName);
		if (ipDefaultMaxQps == null) {
			// 获取全局应用IP默认的最大QPS设置
			ipDefaultMaxQps = limiterResource.getIpQpsLimiter().getDefaultIpMaxQps(Constants.EMPTY_STRING);
		}
		if (ipDefaultMaxQps == null) {
			ipDefaultMaxQps = Constants.DEFAULT_IP_MAX_QPS;
		}
		return Result.ofSuccess(ipDefaultMaxQps);
	}

	/**
	 * 获取该应用的所有相关的配置：IP白名单、IP黑名单、IP显示QPS限制、应用IP默认的最大QPS设置
	 * 
	 * @param appName
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getIpConfig")
	public Result<ControlDataVO> getIpConfig(@RequestParam(required = true) String appName) {
		try {
			Result<Map<String, IpLimitVO>> qpsLimit = getQpsLimitByApp(appName);
			Result<Map<String, WhiteIpVO>> whiteIp = getWhiteIpByApp(appName);
			Result<Map<String, BlackIpVO>> blackIp = getBlackIpByApp(appName);
			Result<Integer> defaultMaxQps = getDefaultIpMaxQps(appName);

			return Result.ofSuccess(ControlDataVO.builder().qpsLimit(qpsLimit.getData()).whiteIp(whiteIp.getData())
					.blackIp(blackIp.getData()).defaultIpMaxQps(defaultMaxQps.getData()).build());
		} catch (Exception e) {
			log.error("获取该应用的所有相关的配置：IP白名单、IP黑名单、IP显示QPS限制、应用IP默认的最大QPS设置发生异常:" + e.getMessage(), e);
			return Result.ofFail("获取该应用的所有相关的配置：IP白名单、IP黑名单、IP显示QPS限制、应用IP默认的最大QPS设置失败！");

		}
	}
}
