package com.eeeffff.limiter.common.vo;

import java.io.Serializable;
import java.util.Date;

import lombok.Builder;
import lombok.experimental.Tolerate;

@lombok.Data
@Builder
public class IpLimitVO implements Serializable {
	private static final long serialVersionUID = 4848588688997436433L;
	private String ip;
	private int limit;
	private Date addDate;

	@Tolerate
	public IpLimitVO() {
	}

}
