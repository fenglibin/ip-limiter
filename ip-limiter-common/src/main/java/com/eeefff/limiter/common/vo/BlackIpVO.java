package com.eeefff.limiter.common.vo;

import java.io.Serializable;
import java.util.Date;

import com.eeefff.limiter.common.constant.Constants;
import com.eeefff.limiter.common.enumeration.BlackIpAddType;
import com.eeefff.limiter.common.enumeration.BlackIpLimitType;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.experimental.Tolerate;

@Data
@Builder
public class BlackIpVO implements Serializable {
	private static final long serialVersionUID = 7454354034265731858L;

	@Tolerate
	public BlackIpVO() {

	}

	// 加入黑名单的ＩＰ
	private String ip;
	@Default
	private Date addDate = new Date();

	@Default
	// 该IP加入到黑名单的类型，如：人工（Manual）、系统自动(System)
	private BlackIpAddType addType = BlackIpAddType.MANUAL;

	@Default
	// 限制的类型：一分钟、一小时、一天、永远
	private BlackIpLimitType limitType = BlackIpLimitType.EVER;

	// 对其进行说明，怎么样加入的，什么时候自动撤消等
	private String addReason;

	@Default
	// 针对系统自动加入到黑名单中的IP，该字段用于记录系统自检该IP的次数，
	// 为了避免自检出错，记录一下自检的次数，每检查一次该值加１，用于判断是否清除该自动增加的IP
	private int checkTimes = 0;
	@Default
	private boolean avaiable = true;

	public boolean isAvaiable() {
		boolean avaiable = true;
		if (BlackIpAddType.MANUAL == addType) {
			return avaiable;
		}
		long now = System.currentTimeMillis();
		if (BlackIpLimitType.MINUTE == limitType) {
			if (now - addDate.getTime() > Constants.Time.MINUTE_MILLS) {
				avaiable = false;
			}
		} else if (BlackIpLimitType.HOUR == limitType) {
			if (now - addDate.getTime() > Constants.Time.HOUR_MILLS) {
				avaiable = false;
			}
		} else if (BlackIpLimitType.DAY == limitType) {
			if (now - addDate.getTime() > Constants.Time.DAY_MILLS) {
				avaiable = false;
			}
		}
		return avaiable;
	}
}
