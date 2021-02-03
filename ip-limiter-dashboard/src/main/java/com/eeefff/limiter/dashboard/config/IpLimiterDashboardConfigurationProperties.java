package com.eeefff.limiter.dashboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.eeefff.limiter.common.util.NetUtil;

import lombok.Data;

@ConfigurationProperties(prefix = "ip.limiter.dashboard")
@Data
public class IpLimiterDashboardConfigurationProperties {
	// 每秒每个IP最大允许的ＱＰＳ
	private int permitsPerSecondEachIp = 50;
	// 将应用上报上来的分/秒纬度的统计数据，根据其访问量做倒序排序，取指定数量的Top个IP的访问数据用于后续处理
	private int maxTopAccessIps = 50;
	// 以分钟为单位统计，并根据访问量做倒序排序，Redis中每分钟保留的访问最多的IP的数量，也即每分钟最多保留Top个IP的访问数据
	private int maxRedisTopAccessIps = 50;
	// (Global)以分钟为单位统计，并根据访问量做倒序排序，Redis中每分钟保留的访问最多的IP的数量，也即每分钟最多保留Top个IP的访问数据
	private int globalMaxRedisTopAccessIps = 50;

	// 单个应用Redis最多保留多少份以分钟为单位的统计，如值为60时，则表示Redis中会保留60份以分钟为统计纬度统计的访问最多的Ip，也可以理解为保留60分钟每分钟访问最多的Ip的统计
	private int maxRedisTopAccessMinutes = 60;
	// (Global)Redis最多保留多少份以分钟为单位的统计，如值为60时，则表示Redis中会保留60份以分钟为统计纬度统计的访问最多的Ip，也可以理解为保留60分钟每分钟访问最多的Ip的统计
	private int globalMaxRedisTopAccessMinutes = 60;
	// 从Redis中获取锁的最大等待时间，单位为毫秒
	private long redisLockMaxWaitMillis = 60000;

	// 与Dashboard建立连接的超时时间，单位为毫秒
	private int connectTimeout = 5000;
	// 与Dashboard通信时Socket的超时时间，单位为毫秒
	private int soTimeout = 5000;
	// 最大可建立的连接数
	private int maxConnTotal = 100;
	// 针对同一个域名同时间正在使用的最多的连接数
	private int maxConnPerRoute = 10;
	// HTTP请求的最大重试次数
	private int maxHttpRetryTimes = 5;
	// 每次HTTP请求重试的间隔时间，单位为毫秒
	private int httpRetryIntervalTime = 20;
	// 服务器的ＩＰ地址
	private String ipAddress = NetUtil.getLinuxLocalIp();

	// 应用客户端健康检查的频率，以分钟为单位
	private int appClientHealthCheckRate = 1;
	// 检查由于超限访问的IP被系统自动加入到黑名单中的IP，并对其进行释放操作的频率，以分钟为单位
	private int systemAddBlackIpCheckRate = 1;
	
	// 超限访问的请求　占比　正常访问请求的比例，如0.3表示占比超过30%，如达到该值或者超过该值，则将该ＩＰ加入到黑名单限制策略中
	private float overAccessLimitRate = 0.3f;
}
