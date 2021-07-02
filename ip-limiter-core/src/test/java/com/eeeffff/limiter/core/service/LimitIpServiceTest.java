package com.eeeffff.limiter.core.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@SpringBootConfiguration
@ComponentScan(basePackages = { "com.eeeffff.limiter" })
@Slf4j
public class LimitIpServiceTest {

	@Autowired
	private LimitIpService limitIpService;

	@Test
	public void addLimitIpTest() throws Exception {
		log.info("do addLimitIpTest");
		boolean result = limitIpService.addLimitIp("123.444.444.444", 444);
		System.out.println(result);
	}
}
