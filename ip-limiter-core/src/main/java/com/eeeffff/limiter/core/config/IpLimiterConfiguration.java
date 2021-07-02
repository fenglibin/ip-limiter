package com.eeeffff.limiter.core.config;

import javax.annotation.Priority;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.eeeffff.limiter.core.Application;

@Configuration
@ComponentScan(basePackageClasses = { Application.class })
@EnableConfigurationProperties({ IpLimiterConfigurationProperties.class })
@Qualifier
@Primary
@Priority(0)
public class IpLimiterConfiguration {

}
