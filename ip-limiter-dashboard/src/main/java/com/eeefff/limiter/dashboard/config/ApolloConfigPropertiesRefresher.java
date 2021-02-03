package com.eeefff.limiter.dashboard.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author fenglibin
 *
 */
@Component
@Slf4j
public class ApolloConfigPropertiesRefresher implements ApplicationContextAware {

	private ApplicationContext applicationContext;
	@Autowired
	RefreshScope refreshScope;

	@ApolloConfigChangeListener
	public void onChange(ConfigChangeEvent changeEvent) {
		refreshTaskScheduleProperties(changeEvent);
	}

	private void refreshTaskScheduleProperties(ConfigChangeEvent changeEvent) {
		log.info("Refreshing TaskSchedule properties!");
		for (String changedKey : changeEvent.changedKeys()) {
            log.info("apollo changed namespace:{} Key:{} value:{}", changeEvent.getNamespace(), changedKey, changeEvent.getChange(changedKey));
        }
		// 更新相应的bean的属性值，主要是存在@ConfigurationProperties注解的bean
		this.applicationContext.publishEvent(new EnvironmentChangeEvent(changeEvent.changedKeys()));
		refreshScope.refreshAll();

		log.info("TaskSchedule properties refreshed!");

	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		log.info("@@@@@@@@@@@@@TaskSchedulePropertiesRefresher loaded.");
		this.applicationContext = applicationContext;
	}

}