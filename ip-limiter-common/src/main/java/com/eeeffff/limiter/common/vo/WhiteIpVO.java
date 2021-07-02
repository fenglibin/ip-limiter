package com.eeeffff.limiter.common.vo;

import java.io.Serializable;
import java.util.Date;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.experimental.Tolerate;

@Data
@Builder
public class WhiteIpVO implements Serializable {
	private static final long serialVersionUID = 2861018198734663143L;

	@Tolerate
	public WhiteIpVO() {

	}

	private String ip;
	@Default
	private Date addDate = new Date();
}
