package com.eeeffff.limiter.dashboard.constants;

public class RedisKey {
	// Redis中应用的Key的前缀，用于
	public final static String MINUTE_ACCESS_KEY_PREFIX = "ip-limiter";
	// Redis中存放以分钟为纬度的统计数据的Key，存储结果为Hash
	public final static String MINUTE_ACCESS_KEY = "minute-access";
	// Redis中存放黑名单IP的Key，存储结果为Hash
	public final static String BLACK_IPS_KEY = "black-ips";
	// Redis中存放黑名单IP的Key，存储结果为Hash
	public final static String WHITE_IPS_KEY = "white-ips";
	// 每个应用每秒最多的请求数的key
	public final static String PERMITS_PER_SECOND_EACH_IP_KEY = "permitsPerSecondEachIp";
	// IP及IP段的QPS设置的Redis Key
	public final static String IP_LIMIT_KEY = "ip-limit";
	// 注册的上来的客户端
	public final static String REGISTERED_CLIENT_KEY = "registered-clients";
	// 存放ip-limiter-dashboard master的ＩＰ及端口
	public final static String MASTER_KEY = "ip-limiter-dashboard-master";
}
