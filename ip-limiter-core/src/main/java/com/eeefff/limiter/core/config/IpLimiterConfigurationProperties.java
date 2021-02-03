package com.eeefff.limiter.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.eeefff.limiter.common.util.NetUtil;

import lombok.Data;

@ConfigurationProperties(prefix = "ip.limiter.core")
@Data
public class IpLimiterConfigurationProperties {
	// 每秒每个IP最大允许的ＱＰＳ
	// private int permitsPerSecondEachIp = 50;
	// 以秒为统计纬度的上报频率，单位为秒。注：目前没有自动上报以秒纬度统计的数据，目前用于清理以秒纬度统计的超期数据
	private int secondsMetricReportInterval = 1;
	// 本地缓存中保留多少份以秒纬度统计的数据，单位为秒
	private int secondsMetricLocalKeeped = 60;
	// 本地应用最多保留多少份以分钟为单位的统计，如值为60时，则表示本地会保留60份以分钟为统计纬度统计的访问最多的Ip，也可以理解为保留60分钟每分钟访问最多的Ip的统计
	private int maxTopAccessMinutes = 60;

	// IP Limiter控制台的地址
	private String dashboardAddress = "127.0.0.1:8080";
	// 与Dashboard建立连接的超时时间，单位为毫秒
	private int connectTimeout = 5000;
	// 与Dashboard通信时Socket的超时时间，单位为毫秒
	private int soTimeout = 5000;
	// 最大可建立的连接数
	private int maxConnTotal = 10;
	// 针对同一个域名同时间正在使用的最多的连接数
	private int maxConnPerRoute = 10;
	// 当前服务器的IP地址
	private String serverAddress = null;
	// 当前服务器的IP地址
	private String serverAddressDefault = NetUtil.getLinuxLocalIp();

	// IP的QPS限制配置及IP黑名单更新时间间隔，单位为毫秒
	private int ipQpsLimitAndBlackIpUpdateTimeInterval = 10000;

	/**
	 * 获取远程控制台的地址
	 * 
	 * @return
	 */
	public String getDashboardAddress() {
		if (dashboardAddress == null) {
			throw new RuntimeException("Ip limiter dashboard address must be configed, for example 127.0.0.1:8080");
		}
		return dashboardAddress;
	}

	/**
	 * 获取本机的ＩＰ地址
	 * 
	 * @return
	 */
	public String getServerAddress() {
		if (serverAddress == null) {
			serverAddress = serverAddressDefault;
		}
		return serverAddress;
	}
}
