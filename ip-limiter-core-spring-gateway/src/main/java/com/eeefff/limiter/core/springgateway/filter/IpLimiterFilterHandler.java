package com.eeefff.limiter.core.springgateway.filter;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;

import com.eeefff.limiter.common.enumeration.AccessType;
import com.eeefff.limiter.common.util.IpHelper;
import com.eeefff.limiter.common.vo.BlackIpVO;
import com.eeefff.limiter.common.vo.WhiteIpVO;
import com.eeefff.limiter.core.cache.IpCacheHelper;
import com.eeefff.limiter.core.interceptor.IpQpsRateLimiter;
import com.eeefff.limiter.core.service.BlackIpService;
import com.eeefff.limiter.core.service.WhiteIpService;
import com.eeefff.limiter.core.springgateway.handler.OverLimitAccessClientHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IpLimiterFilterHandler {

	private static IpCacheHelper ipCacheHelper;
	private static int permitsPerSecondEachIp = 50;

	private static BlackIpService blackIpService;

	private static WhiteIpService whiteIpService;

	public IpLimiterFilterHandler(int permitsPerSecondEachIp) {
		IpLimiterFilterHandler.permitsPerSecondEachIp = permitsPerSecondEachIp;
		IpQpsRateLimiter.initIpQpsRateLimiter(IpLimiterFilterHandler.permitsPerSecondEachIp, ipCacheHelper);
	}

	public IpLimiterFilterHandler(IpCacheHelper ipCacheHelper, int permitsPerSecondEachIp,
			BlackIpService blackIpService, WhiteIpService whiteIpService) {
		IpLimiterFilterHandler.ipCacheHelper = ipCacheHelper;
		IpLimiterFilterHandler.permitsPerSecondEachIp = permitsPerSecondEachIp;
		IpLimiterFilterHandler.blackIpService = blackIpService;
		IpLimiterFilterHandler.whiteIpService = whiteIpService;
		IpQpsRateLimiter.initIpQpsRateLimiter(IpLimiterFilterHandler.permitsPerSecondEachIp, ipCacheHelper);
	}

	public boolean preHandle(ServerHttpRequest request, ServerHttpResponse response) {
		try {
			String url = request.getURI().getPath();
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
				// response.setStatus(401);
				response.setStatusCode(HttpStatus.FORBIDDEN);
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
