package com.eeefff.limiter.common.vo;

import java.util.Map;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

@Data
@Builder
public class ControlDataVO {
	@Tolerate
	public ControlDataVO() {

	}

	private int defaultIpMaxQps;
	private Map<String, IpLimitVO> qpsLimit;
	private Map<String, BlackIpVO> blackIp;
	private Map<String, WhiteIpVO> whiteIp;
}
