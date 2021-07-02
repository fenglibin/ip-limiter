package com.eeeffff.limiter.core.web.config;

import javax.annotation.Priority;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.eeeffff.limiter.common.constant.Constants;
import com.eeeffff.limiter.core.cache.IpCacheHelper;
import com.eeeffff.limiter.core.config.IpLimiterConfigurationProperties;
import com.eeeffff.limiter.core.config.SystemEnv;
import com.eeeffff.limiter.core.service.BlackIpService;
import com.eeeffff.limiter.core.service.WhiteIpService;
import com.eeeffff.limiter.core.web.Application;
import com.eeeffff.limiter.core.web.interceptor.IpLimiterInterceptor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@ComponentScan(basePackageClasses = { Application.class })
@EnableConfigurationProperties({ IpLimiterConfigurationProperties.class })
@Qualifier
@Primary
@Priority(0)
public class IpLimiterWebConfiguration implements WebMvcConfigurer {

	@Autowired
	private IpCacheHelper ipCacheHelper;

	@Autowired
	private BlackIpService blackIpService;

	@Autowired
	private WhiteIpService whiteIpService;

	/**
	 * 定义的该Bean用于支持Spring MVC框架引入，因为通常Spring MVC都会配置<mvc:annotation-driven/>，<br>
	 * 当有了该配置后，注入的Intecepter本身是不会生效的，因为该注解会自动配置DefaultAnnotationHandlerMapping<br>
	 * 与AnnotationMethodHandlerAdapter 两个bean，导致无法指定自定义拦截器。因而要使用IpLimiterInterceptor拦截器，<br>
	 * 就需求手动在配置文件中引入，引入的步骤如下：<br>
	 * １、增加IP限流平台包的扫描：<context:component-scan base-package="com.eeeffff.limiter"></context:component-scan><br>
	 * ２、手动配置拦截器：<mvc:interceptors><ref bean="ipLimiterInterceptor" /></mvc:interceptors><br>
	 * 这样就可以了
	 * 
	 * @return
	 */
	@Bean("ipLimiterInterceptor")
	@Order(-9999)
	public HandlerInterceptor handlerInterceptor() {
		return new IpLimiterInterceptor(SystemEnv.getAppName(), ipCacheHelper, Constants.DEFAULT_IP_MAX_QPS,
				blackIpService, whiteIpService);
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		log.info("Add IpLimiter interceptor:" + this.getClass() + ", current appName is:" + SystemEnv.getAppName());
		// Add IpLimiter interceptor
		addSpringMvcInterceptor(registry);
	}

	private void addSpringMvcInterceptor(InterceptorRegistry registry) {
		registry.addInterceptor(new IpLimiterInterceptor(SystemEnv.getAppName(), ipCacheHelper,
				Constants.DEFAULT_IP_MAX_QPS, blackIpService, whiteIpService)).addPathPatterns("/**");
	}

}
