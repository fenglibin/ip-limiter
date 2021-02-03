package com.eeefff.limiter.common.vo;

import java.util.Date;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

@Data
@Builder
public class Client {
	@Tolerate
	public Client() {

	}

	// 应用的ＩＰ
	private String ip;

	// 应用的端口
	private int port;
	// 注册上来的日期及时间

	@Builder.Default
	private Date registeredDate = new Date();

	// 断开的日期及时间（注：为健康检查失败的时间）
	private Date offDate;

	// 当前是否在线
	@Builder.Default
	private boolean isOnline = true;
}
