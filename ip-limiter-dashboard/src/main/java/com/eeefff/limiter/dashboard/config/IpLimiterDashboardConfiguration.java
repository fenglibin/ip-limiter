package com.eeefff.limiter.dashboard.config;

import java.util.Comparator;

import javax.annotation.Priority;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.eeefff.limiter.common.vo.AccessVO;
import com.eeefff.limiter.core.Application;

@EnableApolloConfig
@Configuration
@ComponentScan(basePackageClasses = { Application.class })
@EnableConfigurationProperties({ IpLimiterDashboardConfigurationProperties.class })
@Qualifier
@Primary
@Priority(0)
public class IpLimiterDashboardConfiguration {

	@Bean("accessVOComparator")
	public Comparator<AccessVO> getComparator() {
		return new Comparator<AccessVO>() {

			@Override
			public int compare(AccessVO o1, AccessVO o2) {
				if (o1.getTotal().longValue() < o2.getTotal().longValue()) {
					return 1;
				} else if (o1.getTotal().longValue() > o2.getTotal().longValue()) {
					return -1;
				} else {
					return 0;
				}
			}

		};
	}

}
