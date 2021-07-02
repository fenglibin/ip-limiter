package com.eeeffff.limiter.core.web.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import com.eeeffff.limiter.common.enumeration.AccessType;
import com.eeeffff.limiter.common.util.IpHelper;
import com.eeeffff.limiter.common.vo.BlackIpVO;
import com.eeeffff.limiter.common.vo.WhiteIpVO;
import com.eeeffff.limiter.core.cache.IpCacheHelper;
import com.eeeffff.limiter.core.interceptor.IpQpsRateLimiter;
import com.eeeffff.limiter.core.service.BlackIpService;
import com.eeeffff.limiter.core.service.WhiteIpService;
import com.eeeffff.limiter.core.web.handler.OverLimitAccessClientHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IpLimiterInterceptor implements HandlerInterceptor {

	private String appName;
	private static IpCacheHelper ipCacheHelper;
	private static int permitsPerSecondEachIp = 50;

	private static BlackIpService blackIpService;

	private static WhiteIpService whiteIpService;

	public IpLimiterInterceptor(String appName, int permitsPerSecondEachIp) {
		this.appName = appName;
		IpLimiterInterceptor.permitsPerSecondEachIp = permitsPerSecondEachIp;
		IpQpsRateLimiter.initIpQpsRateLimiter(IpLimiterInterceptor.permitsPerSecondEachIp, ipCacheHelper);
	}

	public IpLimiterInterceptor(String appName, IpCacheHelper ipCacheHelper, int permitsPerSecondEachIp,
			BlackIpService blackIpService, WhiteIpService whiteIpService) {
		this.appName = appName;
		IpLimiterInterceptor.ipCacheHelper = ipCacheHelper;
		IpLimiterInterceptor.permitsPerSecondEachIp = permitsPerSecondEachIp;
		IpLimiterInterceptor.blackIpService = blackIpService;
		IpLimiterInterceptor.whiteIpService = whiteIpService;
		IpQpsRateLimiter.initIpQpsRateLimiter(IpLimiterInterceptor.permitsPerSecondEachIp, ipCacheHelper);
	}

	/**
	 * 获取请求的接口地址
	 * 
	 * @param request
	 * @return
	 */
	protected String getResourceName(HttpServletRequest request) {
		// Resolve the Spring Web URL pattern from the request attribute.
		Object resourceNameObject = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
		if (resourceNameObject == null || !(resourceNameObject instanceof String)) {
			return null;
		}
		return (String) resourceNameObject;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		try {
			String url = request.getRequestURI();
			String _appName = request.getParameter("appName");
			if (StringUtils.isEmpty(_appName)) {
				_appName = this.appName;
			}
			// 这两个属性是给FreeMarker在页面上引用的属性
			request.setAttribute("appName", _appName);
			request.setAttribute("ip", request.getParameter("ip"));
			String ip = IpHelper.getIpAddress(request);
			// 先判断是否白名单ＩＰ，如果是则直接允许访问
			WhiteIpVO whiteIp = whiteIpService.checkWhiteIp(ip);
			if (whiteIp != null) {
				// 白名单IP不受QPS设备的限制
				ipCacheHelper.incrVisit(ip, url, AccessType.Normal, Integer.MAX_VALUE);
				return true;
			}
			// 先判断是否黑名单ＩＰ，如果是则直接不允许访问
			BlackIpVO blackIp = blackIpService.checkBlackIp(ip);
			if (blackIp != null && blackIp.isAvaiable()) {
				log.warn("访问IP:" + ip + "为黑名单中的IP，不可访问当前系统，其加入黑名单的时间为：" + blackIp.getAddDate());
				response.setStatus(401);
				ipCacheHelper.incrVisit(ip, url, AccessType.Block, -1);
				return false;
			}
			/*
			 * 注：下面判断当前访问ＩＰ是否超过该ＩＰ访问ＱＰＳ，然后再确定是将该ＩＰ的访问行为增加到正常的访问ＩＰ记数，<br>
			 * 还是增加到拒绝的访问记数，由于判断与增加记数的过程是非同步的过程，因为会造成实际的访问行为ＱＰＳ数据会超 <br>
			 * 过设置的最大的访问行为ＱＰＳ数据，如最大ＱＰＳ为５０，有时会看到为实际记录的为５１、５１等，<br>
			 * 这么做的目的也是为了提升访问的效率，后续也可以对该处理进行优化。<br>
			 */
			if (IpQpsRateLimiter.getIntance().tryAquire(ip, url)) {
				return true;
			} else {
				try {
					return OverLimitAccessClientHandler.getOverLimitAccessHandler().handleOverLimitAccess(request,
							response, ip, url);
				} catch (Exception e) {
					log.error("对超过访问量的ＩＰ处理时发生异常：" + e.getMessage(), e);
					return false;
				}
			}
		} catch (Exception e) {
			log.error("IP访问纬度数据的收集发生了异常：" + e.getMessage(), e);
			return true;
		}
	}

}
