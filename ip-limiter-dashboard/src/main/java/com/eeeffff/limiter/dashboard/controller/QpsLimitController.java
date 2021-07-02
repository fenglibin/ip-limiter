package com.eeeffff.limiter.dashboard.controller;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.cache.Cache;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eeeffff.limiter.common.constant.Constants;
import com.eeeffff.limiter.common.util.IpHelper;
import com.eeeffff.limiter.common.vo.IpLimitVO;
import com.eeeffff.limiter.common.vo.Result;
import com.eeeffff.limiter.core.config.SystemEnv;
import com.eeeffff.limiter.dashboard.config.IpLimiterDashboardConfigurationProperties;
import com.eeeffff.limiter.dashboard.config.LimiterResource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping(value = "/limiter/qpsLimit", produces = MediaType.APPLICATION_JSON_VALUE)
public class QpsLimitController {
	@Autowired
	private IpLimiterDashboardConfigurationProperties ipLimiterConfigurationProperties;
	@Autowired
	private LimiterResource limiterResource;
	@Resource(name = "ipLimitCache")
	private Cache<String, Object> ipLimitCache;

	/**
	 * 设置或获取所有ＩＰ默认的ＱＰＳ
	 * 
	 * @param permitsPerSecondEachIp
	 * @param save
	 * @param model
	 */
	private void setOrGetCommonLimit(String appName, int permitsPerSecondEachIp,
			@RequestParam(required = false, defaultValue = "false") boolean save, Model model) {
		if (StringUtils.isEmpty(appName)) {
			appName = SystemEnv.getAppName();
		}
		if (save) {
			limiterResource.getIpQpsLimiter().setDefaultIpMaxQps(appName, permitsPerSecondEachIp);
			model.addAttribute("permitsPerSecondEachIp", permitsPerSecondEachIp);
			model.addAttribute("save", "true");
		} else {
			Object val = limiterResource.getIpQpsLimiter().getDefaultIpMaxQps(appName);
			if (val == null) {
				permitsPerSecondEachIp = ipLimiterConfigurationProperties.getPermitsPerSecondEachIp();
			} else {
				permitsPerSecondEachIp = Integer.parseInt(String.valueOf(val));
			}
			model.addAttribute("permitsPerSecondEachIp", permitsPerSecondEachIp);
		}
	}

	/**
	 * 获取所有IP的QPS设置
	 * 
	 * @param model
	 */
	private void getAllQpsLimit(String appName, Model model) {
		if (StringUtils.isEmpty(appName)) {
			appName = SystemEnv.getAppName();
		}
		model.addAttribute("ipQpsList", limiterResource.getIpQpsLimiter().getAllIpLimits(appName));
		model.addAttribute("globalIpQpsList", limiterResource.getIpQpsLimiter().getAllIpLimits(Constants.EMPTY_STRING));
	}

	/**
	 * 设置所有ＩＰ默认的ＱＰＳ
	 * 
	 * @param permitsPerSecondEachIp
	 * @param save
	 * @param model
	 */
	@RequestMapping("/resetLimitPretty")
	public String resetLimitPretty(String appName,
			@RequestParam(required = false, defaultValue = "50") int permitsPerSecondEachIp,
			@RequestParam(required = false, defaultValue = "false") boolean save, Model model) {
		if (StringUtils.isEmpty(appName)) {
			appName = SystemEnv.getAppName();
		}
		setOrGetCommonLimit(appName, permitsPerSecondEachIp, save, model);
		getAllQpsLimit(appName, model);
		return "secondsAccessLimits";
	}

	/**
	 * 设置指定ＩＰ的ＱＰＳ
	 * 
	 * @param appName 应用名称
	 * @param ip      IP地址
	 * @param limit   限制的QPS
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/setClientIpLimit")
	public Result<String> setClientIpLimit(@RequestParam(required = true) String appName,
			@RequestParam(required = true) String ip, @RequestParam(required = true) int limit) {
		try {
			limiterResource.getIpQpsLimiter().addIpLimit(appName, ip, limit);
			return Result.ofSuccess("SUCC");
		} catch (Exception e) {
			log.error("设置应用" + appName + "的ip" + ip + "的QPS限制为" + limit + "发生异常:" + e.getMessage(), e);
			return Result.ofFail("设置应用" + appName + "的ip" + ip + "的QPS限制为" + limit + "发生异常!");
		}

	}

	/**
	 * 设置指定ＩＰ的ＱＰＳ
	 * 
	 * @param appName 应用名称
	 * @param ip      IP地址
	 * @param limit   限制的QPS
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/setIpLimitPretty")
	public String setIpLimitPretty(String appName, @RequestParam(required = true) String ip,
			@RequestParam(required = true) int limit, @RequestParam(required = true) String opType, Model model) {
		if (StringUtils.isEmpty(appName)) {
			appName = SystemEnv.getAppName();
		}
		setOrGetCommonLimit(appName, -1, false, model);
		IpLimitVO ipLimitVO = limiterResource.getIpQpsLimiter().addIpLimit(appName, ip, limit);
		getAllQpsLimit(appName, model);
		model.addAttribute("opType", opType);
		model.addAttribute("ip", ip);
		// 设置IP的限流本地缓存，使其可以即时生效
		Map<String, IpLimitVO> ipLimitMap = null;
		Object obj = ipLimitCache.get(Constants.IP_LIMIT_LOCAL_KEY);
		if (obj == null) {
			ipLimitMap = new HashMap<String, IpLimitVO>();
			ipLimitCache.put(Constants.IP_LIMIT_LOCAL_KEY, ipLimitMap);
		} else {
			ipLimitMap = (Map<String, IpLimitVO>) obj;
		}
		ipLimitMap.put(IpHelper.removeIpWildcard(ip), ipLimitVO);
		return "secondsAccessLimits";
	}

	/**
	 * 删除指定ＩＰ的QPS设置
	 * 
	 * @param ip     IP
	 * @param opType 操作类型
	 * @param model
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/delIpLimitPretty")
	public String delIpLimitPretty(String appName, @RequestParam(required = true) String ip,
			@RequestParam(required = true) String opType, Model model) {
		if (StringUtils.isEmpty(appName)) {
			appName = SystemEnv.getAppName();
		}
		setOrGetCommonLimit(appName, -1, false, model);
		limiterResource.getIpQpsLimiter().delIpLimit(appName, ip);
		getAllQpsLimit(appName, model);
		model.addAttribute("opType", opType);
		model.addAttribute("ip", ip);

		// 检查该ＩＰ是不是配置有全局的QPS设置
		IpLimitVO globalIpLimitVO = limiterResource.getIpQpsLimiter().getIpLimit(Constants.EMPTY_STRING, ip);
		// 从本地缓存中删除，如果ip地址中包含了通配符，需要先去除通配符再删除
		Map<String, IpLimitVO> ipLimitMap = null;
		Object obj = ipLimitCache.get(Constants.IP_LIMIT_LOCAL_KEY);
		if (obj == null) {
			ipLimitMap = new HashMap<String, IpLimitVO>();
			ipLimitCache.put(Constants.IP_LIMIT_LOCAL_KEY, ipLimitMap);
		} else {
			ipLimitMap = (Map<String, IpLimitVO>) obj;
		}
		if (globalIpLimitVO == null) {
			ipLimitMap.remove(IpHelper.removeIpWildcard(ip));
		} else {
			// 如果有全局的QPS设置，则设置全局的QPS
			ipLimitMap.put(IpHelper.removeIpWildcard(ip), globalIpLimitVO);
		}
		return "secondsAccessLimits";
	}
}
