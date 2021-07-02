package com.eeeffff.limiter.dashboard.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.cache.Cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eeeffff.limiter.common.constant.Constants;
import com.eeeffff.limiter.common.util.IpHelper;
import com.eeeffff.limiter.common.vo.AccessVO;
import com.eeeffff.limiter.common.vo.BlackIpVO;
import com.eeeffff.limiter.common.vo.IpLimitVO;
import com.eeeffff.limiter.common.vo.WhiteIpVO;
import com.eeeffff.limiter.dashboard.config.IpLimiterDashboardConfigurationProperties;
import com.eeeffff.limiter.dashboard.config.LimiterResource;

@Controller
@RequestMapping(value = "/global-limiter", produces = MediaType.APPLICATION_JSON_VALUE)
public class GlobalIpAccessController {
	@Autowired
	private IpLimiterDashboardConfigurationProperties ipLimiterConfigurationProperties;

	@Resource(name = "ipLimitCache")
	private Cache<String, Object> ipLimitCache;

	@Autowired
	private LimiterResource limiterResource;

	/**
	 * 首页
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "", method = { RequestMethod.GET })
	public String index_0(Model model) {
		return getMinutesDataPretty(model, -1);
	}

	/**
	 * 首页
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/", method = { RequestMethod.GET })
	public String index_1(Model model) {
		return getMinutesDataPretty(model, -1);
	}

	/**
	 * 设置或获取所有ＩＰ默认的ＱＰＳ
	 * 
	 * @param permitsPerSecondEachIp
	 * @param save
	 * @param model
	 */
	private void setOrGetCommonLimit(@RequestParam(required = true) int permitsPerSecondEachIp,
			@RequestParam(required = false, defaultValue = "false") boolean save, Model model) {
		if (save) {
			limiterResource.getIpQpsLimiter().setDefaultIpMaxQps(Constants.EMPTY_STRING, permitsPerSecondEachIp);
			model.addAttribute("permitsPerSecondEachIp", permitsPerSecondEachIp);
			model.addAttribute("save", "true");
		} else {
			Object val = limiterResource.getIpQpsLimiter().getDefaultIpMaxQps(Constants.EMPTY_STRING);
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
	private void getAllQpsLimit(Model model) {
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
	public String resetLimitPretty(@RequestParam(required = false, defaultValue = "50") int permitsPerSecondEachIp,
			@RequestParam(required = false, defaultValue = "false") boolean save, Model model) {
		setOrGetCommonLimit(permitsPerSecondEachIp, save, model);
		getAllQpsLimit(model);
		return "global/secondsAccessLimits";
	}

	/**
	 * 设置指定ＩＰ的ＱＰＳ
	 * 
	 * @param ip     IP
	 * @param limit  ＱＰＳ
	 * @param opType 操作类型
	 * @param model
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/setIpLimitPretty")
	public String setIpLimitPretty(@RequestParam(required = true) String ip, @RequestParam(required = true) int limit,
			@RequestParam(required = true) String opType, Model model) {
		setOrGetCommonLimit(-1, false, model);
		IpLimitVO ipLimitVO = limiterResource.getIpQpsLimiter().addIpLimit(Constants.EMPTY_STRING, ip, limit);
		getAllQpsLimit(model);
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
		String noWildcardIp = IpHelper.removeIpWildcard(ip);
		// 这是设置全局IP，如果当前应用已经包含了该ＩＰ的设备，则优先使用当前应用的设备，忽略全局设置
		if (ipLimitMap.get(noWildcardIp) == null) {
			ipLimitMap.put(noWildcardIp, ipLimitVO);
		}
		return "global/secondsAccessLimits";
	}

	/**
	 * 删除指定ＩＰ的QPS设置
	 * 
	 * @param ip     IP
	 * @param opType 操作类型
	 * @param model
	 * @return
	 */
	@RequestMapping("/delIpLimitPretty")
	public String delIpLimitPretty(@RequestParam(required = true) String ip,
			@RequestParam(required = true) String opType, Model model) {
		setOrGetCommonLimit(-1, false, model);
		limiterResource.getIpQpsLimiter().delIpLimit(Constants.EMPTY_STRING, ip);
		getAllQpsLimit(model);
		model.addAttribute("opType", opType);
		model.addAttribute("ip", ip);
		return "global/secondsAccessLimits";
	}

	/**
	 * 获取所有以分钟为统计纬度、以分钟为hashkey的这些hashkey的列表
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getMinuteKeys")
	public List<Object> getMinuteKeys() {
		return limiterResource.getMetric().getMinuteDataKeys(Constants.EMPTY_STRING);
	}

	/**
	 * 获取指定分钟的统计数据
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getOneMinuteData")
	public Object getOneMinuteData(String minute) {
		return limiterResource.getMetric().getOneMinuteData(Constants.EMPTY_STRING, minute);
	}

	/**
	 * 获取多个指定分钟的统计数据
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getMultiMinutesData")
	public List<List<AccessVO>> getMultiMinutesData(List<Object> minutes) {
		return limiterResource.getMetric().getMultiMinutesData(Constants.EMPTY_STRING, minutes);
	}

	/**
	 * 查看当前应用集群以分钟为纬度统计的TOP访问ＩＰ的数据
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping("/getMinutesDataPretty")
	public String getMinutesDataPretty(Model model,
			@RequestParam(required = false, defaultValue = "10") int lastMinutes) {
		String page = "global/minutesData";
		List<List<AccessVO>> result = limiterResource.getMetric().getMinutesData(Constants.EMPTY_STRING,
				Constants.EMPTY_STRING, lastMinutes);
		model.addAttribute("minutesDatas", result);
		model.addAttribute("lastMinutes", lastMinutes);
		return page;
	}

	/**
	 * 获取单个的黑名单IP
	 * 
	 * @param ip
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getBlackIp")
	public BlackIpVO getBlackIp(String ip) {
		return limiterResource.getBlackIpLimiter().getBlackIp(Constants.EMPTY_STRING, ip);
	}

	@RequestMapping(value = "/getAllBlackIpsPretty", method = { RequestMethod.GET })
	public String getAllBlackIpsPretty(Model model) {
		model.addAttribute("blackIpList", limiterResource.getBlackIpLimiter().getAllBlackIps(Constants.EMPTY_STRING));
		model.addAttribute("globalBlackIpList",
				limiterResource.getBlackIpLimiter().getAllBlackIps(Constants.EMPTY_STRING));
		return "global/blackIps";
	}

	/**
	 * 将指定的IP增加到黑名单中
	 * 
	 * @param ip
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/addBlackIpPretty")
	public String addBlackIpPretty(String ip, Model model) {
		// 远程写入
		BlackIpVO blackIpVO = limiterResource.getBlackIpLimiter().addBlackIp(Constants.EMPTY_STRING, ip);
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
		getAllBlackIpsPretty(model);
		return "global/blackIps";
	}

	/**
	 * 从黑名单中删除指定的IP，并返回其原来加入黑名单中的信息
	 * 
	 * @param ip
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/removeOneIpFromBlackIps")
	public Object removeOneIpFromBlackIps(String ip) {
		// 远程删除
		BlackIpVO blackIpVO = limiterResource.getBlackIpLimiter().delBlackIp(Constants.EMPTY_STRING, ip);
		return blackIpVO;
	}

	@RequestMapping("/removeOneIpFromBlackIpsPretty")
	public String removeOneIpFromBlackIpsPretty(String ip, Model model) {
		// 远程删除
		limiterResource.getBlackIpLimiter().delBlackIp(Constants.EMPTY_STRING, ip);
		model.addAttribute("delIp", ip);
		getAllBlackIpsPretty(model);
		return "global/blackIps";
	}

	@ResponseBody
	@RequestMapping("/cleanBlackIps")
	public boolean cleanBlackIps() {
		// 远程删除
		limiterResource.getBlackIpLimiter().cleanAllBlackIps(Constants.EMPTY_STRING);
		return true;
	}

	/**
	 * 获取单个的白名单IP
	 * 
	 * @param ip
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getWhiteIp")
	public WhiteIpVO getWhiteIp(String ip) {
		return limiterResource.getWhiteIpLimiter().getWhiteIp(Constants.EMPTY_STRING, ip);
	}

	@RequestMapping(value = "/getAllWhiteIpsPretty", method = { RequestMethod.GET })
	public String getAllWhiteIpsPretty(Model model) {
		model.addAttribute("whiteIpList", limiterResource.getWhiteIpLimiter().getAllWhiteIps(Constants.EMPTY_STRING));
		model.addAttribute("globalWhiteIpList",
				limiterResource.getWhiteIpLimiter().getAllWhiteIps(Constants.EMPTY_STRING));
		return "global/whiteIps";
	}

	/**
	 * 将指定的IP增加到白名单中
	 * 
	 * @param ip
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/addWhiteIpPretty")
	public String addWhiteIpPretty(String ip, Model model) {
		// 远程写入
		WhiteIpVO whiteIpVO = limiterResource.getWhiteIpLimiter().addWhiteIp(Constants.EMPTY_STRING, ip);
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
		getAllWhiteIpsPretty(model);
		return "global/whiteIps";
	}

	/**
	 * 从白名单中删除指定的IP，并返回其原来加入白名单中的信息
	 * 
	 * @param ip
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/removeOneIpFromWhiteIps")
	public Object removeOneIpFromWhiteIps(String ip) {
		// 远程删除
		WhiteIpVO whiteIpVO = limiterResource.getWhiteIpLimiter().delWhiteIp(Constants.EMPTY_STRING, ip);
		return whiteIpVO;
	}

	@RequestMapping("/removeOneIpFromWhiteIpsPretty")
	public String removeOneIpFromWhiteIpsPretty(String ip, Model model) {
		// 远程删除
		limiterResource.getWhiteIpLimiter().delWhiteIp(Constants.EMPTY_STRING, ip);
		model.addAttribute("delIp", ip);
		getAllWhiteIpsPretty(model);
		return "global/whiteIps";
	}

	@ResponseBody
	@RequestMapping("/cleanWhiteIps")
	public boolean cleanWhiteIps() {
		// 远程删除
		limiterResource.getWhiteIpLimiter().cleanAllWhiteIps(Constants.EMPTY_STRING);
		return true;
	}
}
