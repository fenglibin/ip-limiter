package com.eeefff.limiter.common.util;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

import lombok.extern.slf4j.Slf4j;

/**
 * IP工具类
 * 
 * @author fenglibin
 *
 */
@Slf4j
public class IpHelper {
	/**
	 * 获取请求主机IP地址,如果通过代理进来，则透过防火墙获取真实IP地址
	 * 
	 * @param request
	 * @return
	 * @throws IOException
	 */
	public final static String getIpAddress(HttpServletRequest request) throws IOException {

		String ip = request.getHeader("x-forwarded-for");

		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("Proxy-Client-IP");
			}
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("WL-Proxy-Client-IP");
			}
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("HTTP_CLIENT_IP");
			}
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("HTTP_X_FORWARDED_FOR");
			}
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getRemoteAddr();
			}
		} else if (ip.length() > 15) {
			String[] ips = ip.split(",");
			for (int index = 0; index < ips.length; index++) {
				String strIp = (String) ips[index];
				if (!("unknown".equalsIgnoreCase(strIp))) {
					ip = strIp;
					break;
				}
			}
		}

		return ip;
	}
	/**
	 * 获取请求主机IP地址,如果通过代理进来，则透过防火墙获取真实IP地址
	 * 
	 * @param request
	 * @return
	 * @throws IOException
	 */
	public final static String getIpAddress(ServerHttpRequest request) throws IOException {
		HttpHeaders headers = request.getHeaders();
		String ip = headers.getFirst("x-forwarded-for");

		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = headers.getFirst("Proxy-Client-IP");
			}
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = headers.getFirst("WL-Proxy-Client-IP");
			}
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = headers.getFirst("HTTP_CLIENT_IP");
			}
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = headers.getFirst("HTTP_X_FORWARDED_FOR");
			}
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getRemoteAddress().getAddress().getHostAddress();
			}
		} else if (ip.length() > 15) {
			String[] ips = ip.split(",");
			for (int index = 0; index < ips.length; index++) {
				String strIp = (String) ips[index];
				if (!("unknown".equalsIgnoreCase(strIp))) {
					ip = strIp;
					break;
				}
			}
		}

		return ip;
	}

	/**
	 * 去掉IP中的通配符， 如：将127.0.0.*修改为127.0.0，或将127.0.*.*修改为127.0
	 * 
	 * @param ip
	 * @return
	 */
	public static String removeIpWildcard(String ip) {
		if (StringUtils.isEmpty(ip) || ip.indexOf("*") < 0) {
			return ip;
		}
		return ip.substring(0, ip.indexOf("*") - 1);
	}

	/**
	 * 获取一级IP地址，如传入参数127.0.0.1，返回值为127
	 * 
	 * @param ip
	 * @return
	 */
	public static String getOneLevelIpAddress(String ip) {
		try {
			if (StringUtils.isEmpty(ip) || ip.indexOf(".") <= 0) {
				return ip;
			}
			return ip = ip.substring(0, ip.indexOf("."));
		} catch (Exception e) {
			log.error("error ip address:" + ip, e);
		}
		return ip;
	}

	/**
	 * 获取二级IP地址，如传入参数127.0.0.1，返回值为127.0
	 * 
	 * @param ip
	 * @return
	 */
	public static String getTwoLevelIpAddress(String ip) {
		try {
			if (StringUtils.isEmpty(ip) || ip.indexOf(".") <= 0) {
				return ip;
			}
			int i = ip.indexOf(".") + 1;
			return ip = ip.substring(0, ip.indexOf(".", i));
		} catch (Exception e) {
			log.error("error ip address:" + ip, e);
		}
		return ip;
	}

	/**
	 * 获取三级IP地址，如传入参数127.0.0.1，返回值为127.0.0
	 * 
	 * @param ip
	 * @return
	 */
	public static String getThreeLevelIpAddress(String ip) {
		try {
			if (StringUtils.isEmpty(ip) || ip.indexOf(".") <= 0) {
				return ip;
			}
			int i = ip.indexOf(".") + 1;
			i = ip.indexOf(".", i) + 1;
			return ip = ip.substring(0, ip.indexOf(".", i));
		} catch (Exception e) {
			log.error("error ip address:" + ip, e);
		}
		return ip;
	}
}
