package com.eeeffff.limiter.samples.spring.boot.controller;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test controller
 * 
 * @author fenglibin
 */
@RestController
public class TestController {
	/*
	 * @Autowired private TestBean testBean;
	 */

	@Value("${TEST_KEY:#{null}}")
	private String testKey;

	@GetMapping("/hello")
	public String apiHello(HttpServletRequest request) {
		int sleepTime = 100;
		String sleepTimeStr = request.getParameter("sleepTime");
		if (sleepTimeStr != null) {
			sleepTime = Integer.parseInt(sleepTimeStr);
		}
		doBusiness(sleepTime);
		return "Hello! test key is:" + testKey;
	}

	@GetMapping("/helloNoWait")
	public String helloNoWait(HttpServletRequest request) {
		return "Hello! test key is:" + testKey;
	}

	@GetMapping("/hello2")
	public String apiHello2(String str) {
		doBusiness();
		return "Hello!";
	}

	@GetMapping("/hello3")
	public String apiHello3(String str1, String str2) {
		doBusiness();
		return "Hello!";
	}

	@GetMapping("/hello4/{id}")
	public String apiHello4(String id, String str1, String str2) {
		doBusiness();
		return "Hello!";
	}

	@GetMapping("/err")
	public String apiError() {
		doBusiness();
		return "Oops...";
	}

	@GetMapping("/foo/{id}")
	public String apiFoo(@PathVariable("id") Long id) {
		doBusiness();
		return "Hello " + id;
	}

	@GetMapping("/exclude/{id}")
	public String apiExclude(@PathVariable("id") Long id) {
		doBusiness();
		return "Exclude " + id;
	}

	@GetMapping("/{hello}/isit.json")
	public String apiExclude2(@PathVariable("hello") String hello) {
		doBusiness();
		return "Exclude " + hello;
	}

	private void doBusiness(int sleepTime) {
		try {
			TimeUnit.MILLISECONDS.sleep(sleepTime);
		} catch (InterruptedException e) {
		}
	}

	private void doBusiness() {
		Random random = new Random(1);
		try {
			TimeUnit.MILLISECONDS.sleep(random.nextInt(100));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
