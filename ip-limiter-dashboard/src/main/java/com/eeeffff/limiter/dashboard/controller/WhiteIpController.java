package com.eeeffff.limiter.dashboard.controller;

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

import com.eeeffff.limiter.common.constant.Constants;
import com.eeeffff.limiter.common.util.IpHelper;
import com.eeeffff.limiter.common.vo.Result;
import com.eeeffff.limiter.common.vo.WhiteIpVO;
import com.eeeffff.limiter.core.config.SystemEnv;
import com.eeeffff.limiter.dashboard.config.LimiterResource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping(value = "/limiter/whiteIp", produces = MediaType.APPLICATION_JSON_VALUE)
public class WhiteIpController {
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
	@RequestMapping("/getWhiteIp")
	public WhiteIpVO getWhiteIp(String appName, String ip) {
		if (StringUtils.isEmpty(appName)) {
			appName = SystemEnv.getAppName();
		}
		return limiterResource.getWhiteIpLimiter().getWhiteIp(appName, ip);
	}

	/**
	 * 获取所有的黑名单ＩＰ，并展示在页面上
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/getAllWhiteIpsPretty", method = { RequestMethod.GET })
	public String getAllWhiteIpsPretty(String appName, Model model) {
		if (StringUtils.isEmpty(appName)) {
			appName = SystemEnv.getAppName();
		}
		model.addAttribute("whiteIpList", limiterResource.getWhiteIpLimiter().getAllWhiteIps(appName));
		model.addAttribute("globalWhiteIpList",
				limiterResource.getWhiteIpLimiter().getAllWhiteIps(Constants.EMPTY_STRING));
		return "whiteIps";
	}

	/**
	 * 将指定的IP增加到黑名单中
	 * 
	 * @param ip
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/addClientIpToWhiteIp")
	public Result<String> addClientIpToWhiteIp(String appName, String ip) {
		if (StringUtils.isEmpty(appName) || StringUtils.isEmpty(ip)) {
			return Result.ofFail("appName或ip的值为空！");
		}
		try {
			// 远程写入
			limiterResource.getWhiteIpLimiter().addWhiteIp(appName, ip);
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
	@RequestMapping("/addWhiteIp")
	public WhiteIpVO addWhiteIp(String appName, String ip) {
		if (StringUtils.isEmpty(appName)) {
			appName = SystemEnv.getAppName();
		}
		// 远程写入
		WhiteIpVO whiteIpVO = limiterResource.getWhiteIpLimiter().addWhiteIp(appName, ip);
		// 写入本地缓存，当前应用即时生效
		Map<String, WhiteIpVO> whiteIp = null;
		Object obj = ipLimitCache.get(Constants.IP_WHITE_LOCAL_KEY);
		if (obj == null) {
			whiteIp = new HashMap<String, WhiteIpVO>();
			ipLimitCache.put(Constants.IP_WHITE_LOCAL_KEY, whiteIp);
		} else {
			whiteIp = (Map<String, WhiteIpVO>) obj;
		}
		whiteIp.put(IpHelper.removeIpWildcard(ip), WhiteIpVO.builder().ip(ip).build());
		return whiteIpVO;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping("/addWhiteIpPretty")
	public String addWhiteIpPretty(String appName, String ip, Model model) {
		if (StringUtils.isEmpty(appName)) {
			appName = SystemEnv.getAppName();
		}
		// 远程写入
		WhiteIpVO whiteIpVO = limiterResource.getWhiteIpLimiter().addWhiteIp(appName, ip);
		Map<String, WhiteIpVO> whiteIp = null;
		Object obj = ipLimitCache.get(Constants.IP_WHITE_LOCAL_KEY);
		if (obj == null) {
			whiteIp = new HashMap<String, WhiteIpVO>();
			ipLimitCache.put(Constants.IP_WHITE_LOCAL_KEY, whiteIp);
		} else {
			whiteIp = (Map<String, WhiteIpVO>) obj;
		}
		whiteIp.put(IpHelper.removeIpWildcard(ip), whiteIpVO);
		model.addAttribute("addIp", ip);
		getAllWhiteIpsPretty(appName, model);
		return "whiteIps";
	}

	/**
	 * 从黑名单中删除指定的IP，并返回其原来加入黑名单中的信息
	 * 
	 * @param ip
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping("/removeOneIpFromWhiteIps")
	public Object removeOneIpFromWhiteIps(String appName, String ip) {
		if (StringUtils.isEmpty(appName)) {
			appName = SystemEnv.getAppName();
		}
		// 远程删除
		WhiteIpVO whiteIpVO = limiterResource.getWhiteIpLimiter().delWhiteIp(appName, ip);
		// 获取全局设置中的黑名单
		WhiteIpVO globalWhiteIpVO = limiterResource.getWhiteIpLimiter().getWhiteIp(Constants.EMPTY_STRING, ip);
		// 如果全局中没有设置，则同时删除本地的缓存
		if (globalWhiteIpVO == null) {
			// 本地缓存中删除，当前应用即时生效
			Map<String, WhiteIpVO> whiteIp = null;
			Object obj = ipLimitCache.get(Constants.IP_WHITE_LOCAL_KEY);
			if (obj == null) {
				whiteIp = new HashMap<String, WhiteIpVO>();
				ipLimitCache.put(Constants.IP_WHITE_LOCAL_KEY, whiteIp);
			} else {
				whiteIp = (Map<String, WhiteIpVO>) obj;
			}
			whiteIp.remove(IpHelper.removeIpWildcard(ip));
		}
		return whiteIpVO;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping("/removeOneIpFromWhiteIpsPretty")
	public String removeOneIpFromWhiteIpsPretty(String appName, String ip, Model model) {
		if (StringUtils.isEmpty(appName)) {
			appName = SystemEnv.getAppName();
		}
		// 远程删除
		limiterResource.getWhiteIpLimiter().delWhiteIp(appName, ip);
		// 获取全局设置中的黑名单
		WhiteIpVO globalWhiteIpVO = limiterResource.getWhiteIpLimiter().getWhiteIp(Constants.EMPTY_STRING, ip);
		// 如果全局中没有设置，则同时删除本地的缓存
		if (globalWhiteIpVO == null) {
			// 本地缓存中删除，当前应用即时生效
			Map<String, WhiteIpVO> whiteIp = null;
			Object obj = ipLimitCache.get(Constants.IP_WHITE_LOCAL_KEY);
			if (obj == null) {
				whiteIp = new HashMap<String, WhiteIpVO>();
				ipLimitCache.put(Constants.IP_WHITE_LOCAL_KEY, whiteIp);
			} else {
				whiteIp = (Map<String, WhiteIpVO>) obj;
			}
			whiteIp.remove(IpHelper.removeIpWildcard(ip));
		}
		model.addAttribute("delIp", ip);
		getAllWhiteIpsPretty(appName, model);
		return "whiteIps";
	}

	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping("/cleanWhiteIps")
	public boolean cleanWhiteIps(String appName) {
		if (StringUtils.isEmpty(appName)) {
			appName = SystemEnv.getAppName();
		}
		List<WhiteIpVO> whiteIpList = limiterResource.getWhiteIpLimiter().getAllWhiteIps(appName);
		if (CollectionUtils.isEmpty(whiteIpList)) {
			return true;
		}
		// 远程删除
		limiterResource.getWhiteIpLimiter().cleanAllWhiteIps(appName);
		// 本地缓存中删除
		Map<String, WhiteIpVO> whiteIp = (Map<String, WhiteIpVO>) ipLimitCache.get(Constants.IP_WHITE_LOCAL_KEY)
				;
		whiteIpList.forEach(e -> {
			// 获取全局设置中的黑名单
			WhiteIpVO globalWhiteIpVO = limiterResource.getWhiteIpLimiter().getWhiteIp(Constants.EMPTY_STRING,
					e.getIp());
			// 不存在于全局IP黑名单的ＩＰ，才可以从本地缓存中删除
			if (globalWhiteIpVO == null) {
				whiteIp.remove(IpHelper.removeIpWildcard(e.getIp()));
			}
		});
		return true;
	}
}
