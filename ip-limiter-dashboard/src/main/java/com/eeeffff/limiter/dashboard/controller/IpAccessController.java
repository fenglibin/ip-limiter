package com.eeeffff.limiter.dashboard.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.eeeffff.limiter.common.constant.Constants;
import com.eeeffff.limiter.common.util.DateUtil;
import com.eeeffff.limiter.common.util.HttpClientUtil;
import com.eeeffff.limiter.common.vo.AccessVO;
import com.eeeffff.limiter.core.cache.IpCacheHelper;
import com.eeeffff.limiter.core.config.SystemEnv;
import com.eeeffff.limiter.dashboard.client.IClientService;
import com.eeeffff.limiter.dashboard.config.LimiterResource;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping(value = "/limiter", produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class IpAccessController {

	@Autowired
	private IpCacheHelper ipCacheHelper;

	@Autowired
	private LimiterResource limiterResource;

	@Autowired
	private IClientService clientService;

	/**
	 * 首页
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "", method = { RequestMethod.GET })
	public String index_0(Model model) {
		return index_1(model);
	}

	/**
	 * 首页
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/", method = { RequestMethod.GET })
	public String index_1(Model model) {
		return getMinutesDataPretty(Constants.EMPTY_STRING, Constants.EMPTY_STRING, 10, model);
	}

	@ResponseBody
	@RequestMapping("/getIpSecondLongAccess")
	public Map<Long, AccessVO> getIpSecondAccess(@RequestParam(required = true) String ip) {
		return ipCacheHelper.getSecondIpCacheHelper().getVisit(ip);
	}

	@ResponseBody
	@RequestMapping("/getAllIpSecondAccess")
	public Map<String, TreeMap<String, AccessVO>> getAllIpSecondAccess() {
		Map<String, TreeMap<String, AccessVO>> result = new HashMap<String, TreeMap<String, AccessVO>>();
		Map<String, HashMap<Long, AccessVO>> map = ipCacheHelper.getSecondIpCacheHelper().getAllVisit();
		map.forEach((k, v) -> {
			TreeMap<String, AccessVO> treeMap = new TreeMap<String, AccessVO>();
			v.forEach((k1, v1) -> {
				treeMap.put(String.valueOf(k1), v1);
			});
			result.put(k, treeMap);
		});
		return result;
	}

	/**
	 * 获取指定服务器最近一段时间秒纬度的访问统计数据
	 * 
	 * @param model
	 * @param appName         需要获取访问统计的应用名称
	 * @param ip              应用的ＩＰ
	 * @param ts
	 * @param lastSeconds     展示最新的多少秒钟的数据，不传则展示最近１分钟的全部秒维度的访问数据
	 * @param refreshInterval 网页上页面的刷新频率
	 * @param displayType     展示的样式，s表示以秒做为主要统计展示维度，ip表示以ip做为主要统计展示维度，默认值为s
	 * @return
	 */
	@RequestMapping("/getIpSecondAccessPretty")
	public String getAllIpSecondAccessPretty(Model model, @RequestParam(required = false) String appName,
			@RequestParam(required = false) String ip, @RequestParam(required = false) String ts,
			@RequestParam(required = false, defaultValue = "10") Integer lastSeconds,
			@RequestParam(required = false, defaultValue = "5") Integer refreshInterval,
			@RequestParam(required = false, defaultValue = "s") String displayType) {
		log.debug("调用接口getIpSecondAccessPretty的参数,appName={},ip={},ts={},lastSeconds={}", appName, ip, ts, lastSeconds);
		// 最外层Key为获取资源的IP及端口，第二层Key为IP，内层Key为时间秒
		Map<String, Map<String, TreeMap<String, AccessVO>>> serverSencondsAccessMetric = new HashMap<String, Map<String, TreeMap<String, AccessVO>>>();
		// 外层Key为IP，内层Key为时间秒
		Map<String, TreeMap<String, AccessVO>> sencondsAccessMetric = new HashMap<String, TreeMap<String, AccessVO>>();
		if (StringUtils.isEmpty(appName)) {
			sencondsAccessMetric.putAll(getAllIpSecondAccess());
		} else {
			List<String> serverList = new ArrayList<String>();
			ip = StringUtils.isEmpty(ip) ? "" : ip;
			String server = ip;
			if (StringUtils.isEmpty(server)) {
				//没有指定要查看的服务节点，则获取所有的服务节点
				List<String> appNameList = clientService.getAppRegisteredIps(appName);
				if (!CollectionUtils.isEmpty(appNameList)) {
					serverList.addAll(appNameList);
				}
			} else {
				serverList.add(server);
			}
			if (!CollectionUtils.isEmpty(serverList)) {
				serverList.parallelStream().forEach(s -> {
					StringBuilder url = new StringBuilder("http://");
					url.append(s).append("/ip-limiter/metric/getIpSecondAccess?lastSeconds=").append(lastSeconds);
					HttpClientUtil.doGet(url.toString(), r -> {
						if (r != null && r.getCode() == 0) {
							Map<String, TreeMap<String, AccessVO>> map = JSON.parseObject(r.getData().toString(),
									new TypeReference<Map<String, TreeMap<String, AccessVO>>>() {
									});
							if (CollectionUtils.isEmpty(map)) {
								log.warn("获取远程秒访问纬度的响应内容为空，url为" + url);
							}
							// sencondsAccessMetric.putAll(map);
							serverSencondsAccessMetric.put(s, map);
							log.info("节点:"+s+"的响应数据："+map.toString());
						} else {
							log.warn("获取远程秒访问纬度发生异常，响应code：" + r.getCode() + "，响应Msg：" + r.getMsg() + "，URL:" + url);
						}
					});
				});

			}
		}

		serverSencondsAccessMetric.forEach((server, v) -> {// key为服务器的IP及端口，value为从该服务器获取的秒维度访问数据
			v.forEach((ipKey, v1) -> {// key为ip，value为该ip对应不同秒的访问统计数据
				Optional.ofNullable(sencondsAccessMetric.get(ipKey)).ifPresent((treeMapValue) -> {
					// 能够进入下面的处理逻辑，则表示结果中已经包含了该IP对应的数据，则对相同的IP访问数据进行合并
					v1.forEach((secondKey, v2) -> {
						AccessVO vo = treeMapValue.get(secondKey);
						if (vo == null) {// 现有的结果中没有包含当前k2代表的秒的数据，那就直接加上
							treeMapValue.put(secondKey, v2);
						} else {// 现有结果包含了当前k2代表的秒的数据，则需要对数据进行合并
							vo.getBlock().addAndGet(v2.getBlock().intValue());
							vo.getNormal().addAndGet(v2.getNormal().intValue());
							vo.getTotal().addAndGet(v2.getTotal().intValue());
							v2.getUrlsAccess().forEach((url, v3) -> {// key为访问的url，对访问的url进行统计合并
								AtomicInteger urlAccess = vo.getUrlsAccess().get(url);
								if (urlAccess == null) {
									vo.getUrlsAccess().put(url, v3);
								} else {
									urlAccess.addAndGet(v3.intValue());
								}
							});
						}
					});

				});
				Optional.ofNullable(sencondsAccessMetric.get(ipKey)).orElseGet(() -> {
					TreeMap<String, AccessVO> m = new TreeMap<String, AccessVO>();
					m.putAll(v1);
					sencondsAccessMetric.put(ipKey, m);
					return m;
				});

			});
		});

		// 根据不同的展示类型，进行数据的数据，默认为以秒为统计维度进行展示
		if ("s".equals(displayType)) {
			// 外层Key为时间秒，内层Key为IP
			TreeMap<String, HashMap<String, AccessVO>> sMap = new TreeMap<String, HashMap<String, AccessVO>>(
					new Comparator<String>() {
						@Override
						public int compare(String o1, String o2) {
							if (o1.compareTo(o2) > 0) {
								return -1;
							} else if (o1.compareTo(o2) < 0) {
								return 1;
							}
							return 0;
						}

					});
			sencondsAccessMetric.forEach((k, v) -> {// key为IP
				v.forEach((k1, v1) -> {// key为统计的秒
					HashMap<String, AccessVO> map = Optional.ofNullable(sMap.get(k1)).orElseGet(() -> {
						HashMap<String, AccessVO> m = new HashMap<String, AccessVO>();
						sMap.put(k1, m);
						return m;
					});
					map.put(k, v1);
				});
			});
			model.addAttribute("secondsAccess", sMap);
		} else {
			model.addAttribute("secondsAccess", sencondsAccessMetric);
		}
		model.addAttribute("ip", ip);
		model.addAttribute("lastSeconds", lastSeconds);
		model.addAttribute("refreshInterval", refreshInterval);
		model.addAttribute("appName", appName);
		model.addAttribute("displayType", displayType);
		if ("s".equals(displayType)) {
			return "localSecondsDataS";
		} else {
			return "localSecondsData";
		}
	}

	@ResponseBody
	@RequestMapping("/cleanIpSecondAccess")
	public boolean cleanIpSecondAccess(@RequestParam(required = true) String ip) {
		return ipCacheHelper.getSecondIpCacheHelper().cleanVisit(ip);
	}

	@ResponseBody
	@RequestMapping("/getIpMinuteAccess")
	public Map<Long, AccessVO> getIpMinuteAccess(@RequestParam(required = true) String ip) {
		HashMap<Long, AccessVO> result = ipCacheHelper.getMinuteIpCacheHelper().getVisit(ip);
		result.forEach((k, v) -> {
			v.setCurrentDate(DateUtil.formatDate(v.getCurrentMinutes() * 60 * 1000, DateUtil.ISO_DATE_TIME_FORMAT));
		});
		return result;
	}

	@ResponseBody
	@RequestMapping("/getAllIpMinuteAccess")
	public Map<String, HashMap<Long, AccessVO>> getAllIpMinuteAccess() {
		Map<String, HashMap<Long, AccessVO>> result = ipCacheHelper.getMinuteIpCacheHelper().getAllVisit();
		result.forEach((k, v) -> {
			v.forEach((k1, v1) -> {
				v1.setCurrentDate(
						DateUtil.formatDate(v1.getCurrentMinutes() * 60 * 1000, DateUtil.ISO_DATE_TIME_FORMAT));
			});
		});
		return result;
	}

	@ResponseBody
	@RequestMapping("/cleanIpMinuteAccess")
	public boolean cleanIpMinuteAccess(@RequestParam(required = true) String ip) {
		return ipCacheHelper.getMinuteIpCacheHelper().cleanVisit(ip);
	}

	/**
	 * 获取所有以分钟为统计纬度、以分钟为hashkey的这些hashkey的列表
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getMinuteKeys")
	public List<Object> getMinuteKeys(String appName) {
		if (StringUtils.isEmpty(appName)) {
			appName = SystemEnv.getAppName();
		}
		return limiterResource.getMetric().getMinuteDataKeys(appName);
	}

	/**
	 * 获取指定分钟的统计数据
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getOneMinuteData")
	public List<AccessVO> getOneMinuteData(String appName, String minute) {
		if (StringUtils.isEmpty(appName)) {
			appName = SystemEnv.getAppName();
		}
		List<AccessVO> result = limiterResource.getMetric().getOneMinuteData(appName, minute);
		if (CollectionUtils.isEmpty(result)) {
			return result;
		}
		return result;
	}

	/**
	 * 获取多个指定分钟的统计数据
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getMultiMinutesData")
	public List<List<AccessVO>> getMultiMinutesData(String appName, List<Object> minutes) {
		if (StringUtils.isEmpty(appName)) {
			appName = SystemEnv.getAppName();
		}
		return limiterResource.getMetric().getMultiMinutesData(appName, minutes);
	}

	/**
	 * 查看当前应用集群以分钟为纬度统计的TOP访问ＩＰ的数据
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping("/getMinutesDataPretty")
	public String getMinutesDataPretty(String appName, String ip,
			@RequestParam(required = false, defaultValue = "10") int lastMinutes, Model model) {
		if (StringUtils.isEmpty(appName)) {
			appName = SystemEnv.getAppName();
		}
		List<List<AccessVO>> result = limiterResource.getMetric().getMinutesData(appName, ip, lastMinutes);
		model.addAttribute("minutesDatas", result);
		model.addAttribute("appName", appName);
		model.addAttribute("ip", ip);
		model.addAttribute("lastMinutes", lastMinutes);
		return "minutesData";
	}

	/**
	 * 获取所有注册的客户端应用的名称列表
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getAllAppNames")
	public List<Object> getAllAppNames() {
		return limiterResource.getClientService().getAllAppNames();
	}

	/**
	 * 获取指定的客户端应用的所有节点ＩＰ及端口
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getAppRegisteredIps")
	public List<String> getAppRegisteredIps(@RequestParam(required = true) String appName) {
		return limiterResource.getClientService().getAppRegisteredIps(appName);
	}
}
