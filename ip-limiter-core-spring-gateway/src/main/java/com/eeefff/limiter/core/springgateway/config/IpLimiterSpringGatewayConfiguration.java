package com.eeefff.limiter.core.springgateway.config;

import javax.annotation.Priority;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;

import com.eeefff.limiter.common.constant.Constants;
import com.eeefff.limiter.core.cache.IpCacheHelper;
import com.eeefff.limiter.core.config.IpLimiterConfigurationProperties;
import com.eeefff.limiter.core.service.BlackIpService;
import com.eeefff.limiter.core.service.WhiteIpService;
import com.eeefff.limiter.core.springgateway.filter.IpLimiterFilterHandler;
import com.eeefff.limiter.springgateway.core.Application;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@ComponentScan(basePackageClasses = { Application.class })
@EnableConfigurationProperties({ IpLimiterConfigurationProperties.class })
@Qualifier
@Primary
@Priority(-9999)
public class IpLimiterSpringGatewayConfiguration {

	@Autowired
	private IpCacheHelper ipCacheHelper;

	@Autowired
	private BlackIpService blackIpService;

	@Autowired
	private WhiteIpService whiteIpService;
	
	@Bean
	@Order(-999)
	public IpLimiterFilterHandler ipLimiterInterceptor() {
		log.info("Create IpLimiterFilter object");
		return new IpLimiterFilterHandler(ipCacheHelper,
				Constants.DEFAULT_IP_MAX_QPS, blackIpService, whiteIpService);
	}

}
