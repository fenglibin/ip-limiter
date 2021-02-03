package com.eeefff.limiter.core.config;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SystemEnv {
	// 项目的名称
	@Value("${spring.application.name:#{null}}")
	private String appName;
	@Value("${server.port:#{8080}}")
	private int serverPort;

	private static String appNameStatic = null;
	private static int serverPortStatic = 0;

	@PostConstruct
	public void init() {
		if (StringUtils.isEmpty(appName) || "null".equals(appName)) {
			appName = "default_app_" + ((int) (Math.random() * 100000));
		}
		log.info("当前应用的名称为={}，应用端口为={}", appName, serverPort);
		appNameStatic = appName;
		serverPortStatic = serverPort;
	}

	/**
	 * 获取应用名称
	 * 
	 * @return
	 */
	public static String getAppName() {
		return appNameStatic;
	}

	/**
	 * 获取应用端口
	 * 
	 * @return
	 */
	public static int getServerPort() {
		return serverPortStatic;
	}

}
