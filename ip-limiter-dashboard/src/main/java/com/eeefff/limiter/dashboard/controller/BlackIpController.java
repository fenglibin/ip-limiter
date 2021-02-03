package com.eeefff.limiter.dashboard.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.cache.Cache;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eeefff.limiter.common.constant.Constants;
import com.eeefff.limiter.common.util.IpHelper;
import com.eeefff.limiter.common.vo.BlackIpVO;
import com.eeefff.limiter.common.vo.Result;
import com.eeefff.limiter.core.config.SystemEnv;
import com.eeefff.limiter.dashboard.config.LimiterResource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping(value = "/limiter/blackIp", produces = MediaType.APPLICATION_JSON_VALUE)
public class BlackIpController {
	@Autowired
	private LimiterResource limiterResource;

	@Resource(name = "ipLimitCache")
	private Cache<String, Object> ipLimitCache;

	/**
	 * 获取单个的黑名单IP
	 * 
	 * @param ip
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getBlackIp")
	public BlackIpVO getBlackIp(String appName, String ip) {
		if (StringUtils.isEmpty(appName)) {
			appName = SystemEnv.getAppName();
		}
		return limiterResource.getBlackIpLimiter().getBlackIp(appName, ip);
	}

	/**
	 * 获取所有的黑名单ＩＰ，并展示在页面上
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/getAllBlackIpsPretty", method = { RequestMethod.GET })
	public String getAllBlackIpsPretty(String appName, Model model) {
		if (StringUtils.isEmpty(appName)) {
			appName = SystemEnv.getAppName();
		}
		model.addAttribute("blackIpList", limiterResource.getBlackIpLimiter().getAllBlackIps(appName));
		model.addAttribute("globalBlackIpList",
				limiterResource.getBlackIpLimiter().getAllBlackIps(Constants.EMPTY_STRING));
		return "blackIps";
	}

	/**
	 * 将指定的IP增加到黑名单中
	 * 
	 * @param ip
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/addClientIpToBlackIp")
	public Result<String> addClientIpToBlackIp(String appName, String ip) {
		if (StringUtils.isEmpty(appName) || StringUtils.isEmpty(ip)) {
			return Result.ofFail("appName或ip的值为空！");
		}
		try {
			// 远程写入
			limiterResource.getBlackIpLimiter().addBlackIp(appName, ip);
			return Result.ofSuccess("SUCC");
		} catch (Exception e) {
			log.error("增加应用[{}]的ip[{}]到黑名单发生异常[{}]", appName, ip, e.toString());
			return Result.ofFail("增加黑名单发生异常：" + e.getMessage());
		}
	}

	/**
	 * 将指定的IP增加到黑名单中
	 * 
	 * @param ip
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping("/addBlackIp")
	public BlackIpVO addBlackIp(String appName, String ip) {
		if (StringUtils.isEmpty(appName)) {
			appName = SystemEnv.getAppName();
		}
		// 远程写入
		BlackIpVO blackIpVO = limiterResource.getBlackIpLimiter().addBlackIp(appName, ip);
		// 写入本地缓存，当前应用即时生效
		Map<String, BlackIpVO> blackIp = null;
		Object obj = ipLimitCache.get(Constants.IP_BLACK_LOCAL_KEY);
		if (obj == null) {
			blackIp = new HashMap<String, BlackIpVO>();
			ipLimitCache.put(Constants.IP_BLACK_LOCAL_KEY, blackIp);
		} else {
			blackIp = (Map<String, BlackIpVO>) obj;
		}
		blackIp.put(IpHelper.removeIpWildcard(ip), BlackIpVO.builder().ip(ip).build());
		return blackIpVO;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping("/addBlackIpPretty")
	public String addBlackIpPretty(String appName, String ip, Model model) {
		if (StringUtils.isEmpty(appName)) {
			appName = SystemEnv.getAppName();
		}
		// 远程写入
		BlackIpVO blackIpVO = limiterResource.getBlackIpLimiter().addBlackIp(appName, ip);
		Map<String, BlackIpVO> blackIp = null;
		Object obj = ipLimitCache.get(Constants.IP_BLACK_LOCAL_KEY);
		if (obj == null) {
			blackIp = new HashMap<String, BlackIpVO>();
			ipLimitCache.put(Constants.IP_BLACK_LOCAL_KEY, blackIp);
		} else {
			blackIp = (Map<String, BlackIpVO>) obj;
		}
		blackIp.put(IpHelper.removeIpWildcard(ip), blackIpVO);
		model.addAttribute("addIp", ip);
		getAllBlackIpsPretty(appName, model);
		return "blackIps";
	}

	/**
	 * 从黑名单中删除指定的IP，并返回其原来加入黑名单中的信息
	 * 
	 * @param ip
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping("/removeOneIpFromBlackIps")
	public Object removeOneIpFromBlackIps(String appName, String ip) {
		if (StringUtils.isEmpty(appName)) {
			appName = SystemEnv.getAppName();
		}
		// 远程删除
		BlackIpVO blackIpVO = limiterResource.getBlackIpLimiter().delBlackIp(appName, ip);
		// 获取全局设置中的黑名单
		BlackIpVO globalBlackIpVO = limiterResource.getBlackIpLimiter().getBlackIp(Constants.EMPTY_STRING, ip);
		// 如果全局中没有设置，则同时删除本地的缓存
		if (globalBlackIpVO == null) {
			// 本地缓存中删除，当前应用即时生效
			Map<String, BlackIpVO> blackIp = null;
			Object obj = ipLimitCache.get(Constants.IP_BLACK_LOCAL_KEY);
			if (obj == null) {
				blackIp = new HashMap<String, BlackIpVO>();
				ipLimitCache.put(Constants.IP_BLACK_LOCAL_KEY, blackIp);
			} else {
				blackIp = (Map<String, BlackIpVO>) obj;
			}
			blackIp.remove(IpHelper.removeIpWildcard(ip));
		}
		return blackIpVO;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping("/removeOneIpFromBlackIpsPretty")
	public String removeOneIpFromBlackIpsPretty(String appName, String ip, Model model) {
		if (StringUtils.isEmpty(appName)) {
			appName = SystemEnv.getAppName();
		}
		// 远程删除
		limiterResource.getBlackIpLimiter().delBlackIp(appName, ip);
		// 获取全局设置中的黑名单
		BlackIpVO globalBlackIpVO = limiterResource.getBlackIpLimiter().getBlackIp(Constants.EMPTY_STRING, ip);
		// 如果全局中没有设置，则同时删除本地的缓存
		if (globalBlackIpVO == null) {
			// 本地缓存中删除，当前应用即时生效
			Map<String, BlackIpVO> blackIp = null;
			Object obj = ipLimitCache.get(Constants.IP_BLACK_LOCAL_KEY);
			if (obj == null) {
				blackIp = new HashMap<String, BlackIpVO>();
				ipLimitCache.put(Constants.IP_BLACK_LOCAL_KEY, blackIp);
			} else {
				blackIp = (Map<String, BlackIpVO>) obj;
			}
			blackIp.remove(IpHelper.removeIpWildcard(ip));
		}
		model.addAttribute("delIp", ip);
		getAllBlackIpsPretty(appName, model);
		return "blackIps";
	}

	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping("/cleanBlackIps")
	public boolean cleanBlackIps(String appName) {
		if (StringUtils.isEmpty(appName)) {
			appName = SystemEnv.getAppName();
		}
		List<BlackIpVO> blackIpList = limiterResource.getBlackIpLimiter().getAllBlackIps(appName);
		if (CollectionUtils.isEmpty(blackIpList)) {
			return true;
		}
		// 远程删除
		limiterResource.getBlackIpLimiter().cleanAllBlackIps(appName);
		// 本地缓存中删除
		Map<String, BlackIpVO> blackIp = (Map<String, BlackIpVO>) ipLimitCache.get(Constants.IP_BLACK_LOCAL_KEY);
		blackIpList.forEach(e -> {
			// 获取全局设置中的黑名单
			BlackIpVO globalBlackIpVO = limiterResource.getBlackIpLimiter().getBlackIp(Constants.EMPTY_STRING,
					e.getIp());
			// 不存在于全局IP黑名单的ＩＰ，才可以从本地缓存中删除
			if (globalBlackIpVO == null) {
				blackIp.remove(IpHelper.removeIpWildcard(e.getIp()));
			}
		});
		return true;
	}
}
