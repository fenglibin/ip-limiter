package com.eeeffff.limiter.dashboard.iplimit.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.eeeffff.limiter.common.constant.Constants;
import com.eeeffff.limiter.common.enumeration.BlackIpAddType;
import com.eeeffff.limiter.common.enumeration.BlackIpLimitType;
import com.eeeffff.limiter.common.vo.AccessVO;
import com.eeeffff.limiter.common.vo.BlackIpVO;
import com.eeeffff.limiter.dashboard.config.IpLimiterDashboardConfigurationProperties;
import com.eeeffff.limiter.dashboard.iplimit.BlackIpLimiter;
import com.eeeffff.limiter.dashboard.iplimit.OverLimitAccessHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author fenglibin
 *
 */
@Slf4j
@Service(value = "limitTypeUpgradeHandler")
public class LimitTypeUpgradeHandler extends OverLimitAccessHandler {
	@Autowired
	private BlackIpLimiter blackIpLimiter;
	@Autowired
	private IpLimiterDashboardConfigurationProperties ipLimiterDashboardConfigurationProperties;

	@Override
	@Async
	public void handleOverLimitAccess(String appName, List<AccessVO> topAccessMetricList) {
		if (CollectionUtils.isEmpty(topAccessMetricList)) {
			return;
		}
		Set<String> blockAccessIpList = new HashSet<String>();
		topAccessMetricList.forEach(v -> {
			if (v.getBlock().intValue() > 0 && (v.getNormal().intValue() == 0 || (v.getNormal().intValue() > 0
					&& v.getBlock().intValue() / v.getNormal().intValue() > ipLimiterDashboardConfigurationProperties
							.getOverAccessLimitRate()))) {// 判断ＩＰ是否有超限访问
				log.warn("IP：" + v.getIp() + "超限访问超过正常流量的:"
						+ (100 * ipLimiterDashboardConfigurationProperties.getOverAccessLimitRate())
						+ "%，现将其加入到限制访问策略中.");
				blockAccessIpList.add(v.getIp());
			}
		});
		if (CollectionUtils.isEmpty(blockAccessIpList)) {
			return;
		}
		blockAccessIpList.forEach(ip -> {
			Date nowDate = new Date();
			long now = nowDate.getTime();
			Date limitStartDate = DateUtils.addSeconds(nowDate, 10);
			BlackIpVO blackIp = blackIpLimiter.getBlackIp(appName, ip);
			if (blackIp == null) {
				log.warn("IP[{}]首次超限访问应用{}，限制其访问1分钟", ip, appName);
				blackIp = BlackIpVO.builder().ip(ip).addReason("首次超期访问，限制访问1分钟").addType(BlackIpAddType.SYSTEM)
						.limitType(BlackIpLimitType.MINUTE).addDate(limitStartDate).build();
				blackIpLimiter.updateBlackIp(appName, ip, blackIp);
			} else {
				if (BlackIpAddType.SYSTEM != blackIp.getAddType()) {
					return;
				}
				long timeInterview = now - blackIp.getAddDate().getTime();
				if (BlackIpLimitType.MINUTE == blackIp.getLimitType()) {
					if (timeInterview > Constants.Time.MINUTE_MILLS) {
						log.warn("IP[{}]再次超限访问应用{}，限制其访问1小时，其限制访问级别由MINUTE升级为HOUR", ip, appName);
						blackIp.setLimitType(BlackIpLimitType.HOUR);
						blackIp.setAddDate(limitStartDate);
						blackIp.setAddReason("限制类型由MINUTE升为HOUR");
						blackIpLimiter.updateBlackIp(appName, ip, blackIp);
					}
				} else if (BlackIpLimitType.HOUR == blackIp.getLimitType()) {
					if (timeInterview > Constants.Time.HOUR_MILLS) {
						log.warn("IP[{}]第三次超限访问应用{}，限制其访问1天，其限制访问级别由HOUR升级为DAY", ip, appName);
						blackIp.setLimitType(BlackIpLimitType.DAY);
						blackIp.setAddDate(limitStartDate);
						blackIp.setAddReason("限制类型由HOUR升为DAY");
						blackIpLimiter.updateBlackIp(appName, ip, blackIp);
					}
				} else if (BlackIpLimitType.DAY == blackIp.getLimitType()) {
					if (timeInterview > Constants.Time.DAY_MILLS) {
						log.warn("IP[{}]第四次超限访问应用{}，将永远限制其访问，其限制访问级别由DAY升级为EVER", ip, appName);
						blackIp.setLimitType(BlackIpLimitType.EVER);
						blackIp.setAddDate(limitStartDate);
						blackIp.setAddReason("限制类型由DAY升为EVER");
						blackIpLimiter.updateBlackIp(appName, ip, blackIp);
					}
				}
			}
		});
	}

}
