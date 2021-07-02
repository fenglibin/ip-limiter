package com.eeeffff.limiter.common.config;

import javax.annotation.Priority;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.eeeffff.limiter.common.Application;

@Configuration
@ComponentScan(basePackageClasses = { Application.class })
@Qualifier
@Primary
@Priority(0)
public class IpLimiterCommonConfiguration {


}
