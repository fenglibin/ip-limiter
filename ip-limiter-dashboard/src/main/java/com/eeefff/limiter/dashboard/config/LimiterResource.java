package com.eeefff.limiter.dashboard.config;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.eeefff.limiter.dashboard.client.IClientService;
import com.eeefff.limiter.dashboard.iplimit.BlackIpLimiter;
import com.eeefff.limiter.dashboard.iplimit.IpQpsLimiter;
import com.eeefff.limiter.dashboard.iplimit.OverLimitAccessHandler;
import com.eeefff.limiter.dashboard.iplimit.WhiteIpLimiter;
import com.eeefff.limiter.dashboard.metric.AccessMetricHandler;
import com.eeefff.limiter.dashboard.metric.IMetric;

import lombok.Getter;

/**
 * 用于存放存储实现，方便于统一处理。 目前的存储都是基于Redis实现的，后续要将Redis替换为其它的存储，<br>
 * 只需要根据接口实现相应的存储，然后将实现在这里替换即可。
 * 
 * @author fenglibin
 *
 */
@Service(value = "limiterResource")
@Getter
public class LimiterResource {

	@Resource(name = "redisBlackIpLimiter")
	private BlackIpLimiter blackIpLimiter;
	
	@Resource(name = "redisWhiteIpLimiter")
	private WhiteIpLimiter whiteIpLimiter;

	@Resource(name = "redisIpQpsLimiter")
	private IpQpsLimiter ipQpsLimiter;

	@Resource(name = "redisMinuteMetric")
	private IMetric metric;

	@Resource(name = "secondsAccessMetricHandler")
	private AccessMetricHandler secondsAccessMetricHandler;
	@Resource(name = "globalSecondsAccessMetricHandler")
	private AccessMetricHandler globalSecondsAccessMetricHandler;

	@Resource(name = "minutesAccessMetricHandler")
	private AccessMetricHandler minutesAccessMetricHandler;
	@Resource(name = "globalMinutesAccessMetricHandler")
	private AccessMetricHandler globalMinutesAccessMetricHandler;

	@Resource(name = "redisClientService")
	private IClientService clientService;
	
	@Resource(name="limitTypeUpgradeHandler")
	private OverLimitAccessHandler overLimitAccessHandler;

}
