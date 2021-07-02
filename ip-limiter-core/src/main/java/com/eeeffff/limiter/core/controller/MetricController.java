package com.eeeffff.limiter.core.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Resource;
import javax.cache.Cache;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eeeffff.limiter.common.constant.Constants;
import com.eeeffff.limiter.common.util.TimeUtil;
import com.eeeffff.limiter.common.vo.AccessVO;
import com.eeeffff.limiter.common.vo.BlackIpVO;
import com.eeeffff.limiter.common.vo.ControlDataVO;
import com.eeeffff.limiter.common.vo.IpLimitVO;
import com.eeeffff.limiter.common.vo.Result;
import com.eeeffff.limiter.common.vo.WhiteIpVO;
import com.eeeffff.limiter.core.cache.IpCacheHelper;
import com.eeeffff.limiter.core.interceptor.IpQpsRateLimiter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping(value = "/ip-limiter/metric", produces = MediaType.APPLICATION_JSON_VALUE)
public class MetricController {
	@Resource(name = "ipLimitCache")
	private Cache<String,Object> ipLimitCache;
	@Resource(name = "commonCache")
	private Cache<String,Object> commonCache;
	@Autowired
	private IpCacheHelper ipCacheHelper;

	/**
	 * 获取最后一秒钟的访问情况
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping("/getLastSecondIpSecondAccess")
	public Result<Map<String, TreeMap<String, AccessVO>>> getLastSecondIpSecondAccess() {
		log.debug("getLastSecondIpSecondAccess is accessed.");
		Map<String, TreeMap<String, AccessVO>> result = new HashMap<String, TreeMap<String, AccessVO>>();
		Map<String, HashMap<Long, AccessVO>> map;
		Object obj = commonCache.get(Constants.LocalCacheKey.LAST_SECOND_IPS_ACCESSMAP);
		if (obj == null) {
			map = new HashMap<String, HashMap<Long, AccessVO>>();
		} else {
			map = (Map<String, HashMap<Long, AccessVO>>) obj;
		}
		if (CollectionUtils.isEmpty(map)) {
			return Result.ofSuccess(null);
		}
		map.forEach((k, v) -> {
			TreeMap<String, AccessVO> treeMap = new TreeMap<String, AccessVO>();
			v.forEach((k1, v1) -> {
				treeMap.put(String.valueOf(k1), v1);
			});
			result.put(k, treeMap);
		});
		return Result.ofSuccess(result);
	}

	/**
	 * 获取所有当前应用的业务缓存数据
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping("/getControlData")
	public ControlDataVO getControlData() {
		// 读取本地所有IP的QPS设置
		Map<String, IpLimitVO> ipLimitMap = null;
		Object ipLimitObj = ipLimitCache.get(Constants.IP_LIMIT_LOCAL_KEY);
		if (ipLimitObj == null) {
			ipLimitMap = new HashMap<String, IpLimitVO>();
		} else {
			ipLimitMap = (Map<String, IpLimitVO>) ipLimitObj;
		}

		// 读取本地所有IP的白名单设置
		Map<String, WhiteIpVO> whiteIp = null;
		Object whiteIpObj = ipLimitCache.get(Constants.IP_WHITE_LOCAL_KEY);
		if (whiteIpObj == null) {
			whiteIp = new HashMap<String, WhiteIpVO>();
		} else {
			whiteIp = (Map<String, WhiteIpVO>) whiteIpObj;
		}

		// 读取本地所有IP的黑名单设置
		Map<String, BlackIpVO> blackIp = null;
		Object balckIpObj = ipLimitCache.get(Constants.IP_BLACK_LOCAL_KEY);
		if (balckIpObj == null) {
			blackIp = new HashMap<String, BlackIpVO>();
		} else {
			blackIp = (Map<String, BlackIpVO>) balckIpObj;
		}

		ControlDataVO controlDataVO = ControlDataVO.builder()
				.defaultIpMaxQps(IpQpsRateLimiter.getPermitsPerSecondEachIp()).qpsLimit(ipLimitMap).whiteIp(whiteIp)
				.blackIp(blackIp).build();
		return controlDataVO;
	}

	@ResponseBody
	@RequestMapping("/cleanLocalControlData")
	public String cleanLocalControlData(String type) {
		if ("whiteIp".equals(type)) {
			ipLimitCache.remove(Constants.IP_WHITE_LOCAL_KEY);
		} else if ("blackIp".equals(type)) {
			ipLimitCache.remove(Constants.IP_BLACK_LOCAL_KEY);
		} else if ("ipQps".equals(type)) {
			ipLimitCache.remove(Constants.IP_LIMIT_LOCAL_KEY);
		} else if (StringUtils.isEmpty(type)) {
			ipLimitCache.remove(Constants.IP_WHITE_LOCAL_KEY);
			ipLimitCache.remove(Constants.IP_BLACK_LOCAL_KEY);
			ipLimitCache.remove(Constants.IP_LIMIT_LOCAL_KEY);
		}
		return "SUCC";
	}

	/**
	 * 获取最后多少秒的访问，如果不传则默认本地内存中所有以秒纬度的访问存储数据
	 * 
	 * @param lastSeconds
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getIpSecondAccess")
	public Result<Map<String, TreeMap<String, AccessVO>>> getAllIpSecondAccessPretty(
			@RequestParam(required = false, defaultValue = "0") Integer lastSeconds) {
		log.debug("获取秒纬度存储的访问数据,lastSeconds={}",lastSeconds);
		Map<String, TreeMap<String, AccessVO>> result = new HashMap<String, TreeMap<String, AccessVO>>();
		try {
			long currentTimeSeconds = TimeUtil.currentTimeSeconds();
			// 外层Key为ＩＰ，内层Key为当前的秒
			Map<String, HashMap<Long, AccessVO>> map = ipCacheHelper.getSecondIpCacheHelper().getAllVisit();
			map.forEach((k, v) -> {
				TreeMap<String, AccessVO> treeMap = new TreeMap<String, AccessVO>();
				if (!CollectionUtils.isEmpty(v)) {
					v.forEach((k1, v1) -> {
						if (lastSeconds > 0) {
							if (k1 >= currentTimeSeconds - lastSeconds && k1 < currentTimeSeconds) {
								treeMap.put(String.valueOf(k1), v1);
							}
						} else {
							treeMap.put(String.valueOf(k1), v1);
						}
					});
					if(!CollectionUtils.isEmpty(treeMap)) {
						result.put(k, treeMap);
					}
				}
			});
			log.debug("秒纬度存储的访问数据:{}",result);
			return Result.ofSuccess(result);
		} catch (Exception e) {
			log.error("获取秒纬度的统计数据发生异常:" + e.getMessage(), e);
			return Result.ofFail("获取秒纬度的统计数据发生异常");
		}

	}
}
