package com.eeeffff.limiter.common.constant;

import java.nio.charset.Charset;

/**
 * 
 * @author fenglibin
 *
 */
public class Constants {
	// 用于存放指定IP设置QPS的EHCache Key
	public static final String IP_LIMIT_LOCAL_KEY = "ip-limits-normal-ip";
	// ＩＰ默认黑名单
	public static final String IP_BLACK_LOCAL_KEY = "ip-limits-black-ip";
	// ＩＰ默认白名单
	public static final String IP_WHITE_LOCAL_KEY = "ip-limits-white-ip";
	public static final String EMPTY_STRING = "";
	// 单个ＩＰ每秒默认的QPS
	public static final Integer DEFAULT_IP_MAX_QPS = 50;

	// 本地缓存Key
	public static final class LocalCacheKey {
		// 本地应用当前最后一秒钟所有IP的访问信息，存放于Local cache EHCache中，该字段表示其Key值
		public static final String LAST_SECOND_IPS_ACCESSMAP = "lastSecondIpsAccessMap";
	}

	public static final class HTTP {
		public static final int HTTP_OK = 200;
		public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
	}

	public static final class Time {
		// 每秒的毫秒数据
		public static final long SECOND_MILLS = 1000;
		// 每分钟的毫秒数
		public static final long MINUTE_MILLS = 60 * SECOND_MILLS;
		// 每小时的毫秒数
		public static final long HOUR_MILLS = 60 * MINUTE_MILLS;
		// 每天的毫秒数
		public static final long DAY_MILLS = 24 * HOUR_MILLS;
	}
}
