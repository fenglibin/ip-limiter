package com.eeeffff.limiter.common.vo;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Tolerate;

@Getter
@Setter
@Builder
public class AccessVO implements Serializable {
	private static final long serialVersionUID = -6511890807467334080L;

	@Tolerate
	public AccessVO() {
	}

	@Default
	// 全部访问的次数
	private AtomicInteger total = new AtomicInteger(0);
	// 正常访问的次数
	@Default
	private AtomicInteger normal = new AtomicInteger(0);
	// 被拒绝的次数
	@Default
	private AtomicInteger block = new AtomicInteger(0);
	// 当前统计的分钟
	private long currentMinutes;
	// 当前分钟对应的日期
	private String currentDate;
	// 当前统计的IP
	private String ip;
	// 访问的Url的情况，每个Url在这个时间段之内被访问的次数
	@Default
	private Map<String, AtomicInteger> urlsAccess = new ConcurrentHashMap<String, AtomicInteger>();
	@JsonIgnoreProperties(ignoreUnknown = true)
	private String urlsAccessStr;
	
	public void setCurrentDate(String currentDate) {
		this.currentDate = currentDate;
	}
	
	public String getUrlsAccessStr() {
		return urlsAccess.toString();
	}

	public String toString() {
		return JSON.toJSONString(this);
	}
}
