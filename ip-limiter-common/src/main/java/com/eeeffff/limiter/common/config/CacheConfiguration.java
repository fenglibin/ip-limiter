package com.eeeffff.limiter.common.config;

import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.cache.Cache;

import org.ehcache.jsr107.EhcacheCachingProvider;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.ResourceUtils;

@Configuration
public class CacheConfiguration {

	@Bean(name = "commonCache")
	public Cache<String,?> getCommonCache(javax.cache.CacheManager cacheManager) {
		Cache<String,?> cache = cacheManager.getCache("commonCache");
		return cache;
	}
	
	@Bean(name = "ipCache")
	public Cache<String,?> getIpCache(javax.cache.CacheManager cacheManager) {
		Cache<String,?> cache = cacheManager.getCache("ipCache");
		return cache;
	}
	
	@Bean(name = "ipLimitCache")
	public Cache<String,?> getIpLimitCache(javax.cache.CacheManager cacheManager) {
		Cache<String,?> cache = cacheManager.getCache("ipLimitCache");
		return cache;
	}
	
	@Bean(name = "lockCache")
	public Cache<String,?> getLockCache(javax.cache.CacheManager cacheManager) {
		Cache<String,?> cache = cacheManager.getCache("lockCache");
		return cache;
	}
	
	@Primary
	@Bean("ehCacheCacheManager") 
	public org.springframework.cache.CacheManager ehcache3CacheManager() throws URISyntaxException, FileNotFoundException {
		/* 创建springCacheManager接口的具体实现类，参数是javax下面的CacheManager实现类 */
		return new JCacheCacheManager(ehcache3JCacheManager());
	}

	@Bean
	public javax.cache.CacheManager ehcache3JCacheManager() throws URISyntaxException, FileNotFoundException {

		// ehcache实现了javax的CachingProvider接口的具体实现
		EhcacheCachingProvider ehcacheCachingProvider = new EhcacheCachingProvider();

		// 根据配置文件获取cachemanager
		URI uri = ResourceUtils.getURL("classpath:ehcache3.xml").toURI();

		javax.cache.CacheManager cacheManager = ehcacheCachingProvider.getCacheManager(uri,
				this.getClass().getClassLoader());

		return cacheManager;

	}

}
