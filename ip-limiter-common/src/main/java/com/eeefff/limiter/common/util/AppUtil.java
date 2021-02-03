package com.eeefff.limiter.common.util;

import org.apache.commons.lang3.StringUtils;

public class AppUtil {

	/**
	 * 根据appName的值，返回其对应的应用Key前缀，如果为空则直接返回，则不为空则补充"-"在其名称后
	 * 
	 * @param appName
	 * @return
	 */
	public static String getAppRedisKeyPrefix(String appName) {
		if (!StringUtils.isEmpty(appName)) {
			return new StringBuilder(appName).append("-").toString();
		}
		return appName;
	}

	/**
	 * 获取指定的appName与其个节点组成的redis key
	 * 
	 * @param appName
	 * @param ip
	 * @return
	 */
	public static String getAppNameWithIpRedisKey(String appName, String ip) {
		if (StringUtils.isEmpty(ip)) {
			return appName;
		}
		return new StringBuilder(appName).append("-").append(ip).toString();
	}
}
