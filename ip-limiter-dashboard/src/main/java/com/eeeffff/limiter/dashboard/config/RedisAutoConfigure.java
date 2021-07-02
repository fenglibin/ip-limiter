/*
 * Copyright All Rights Reserved.
 * @Owner: jiang.chao
 * @Date:  2017-11-03 10:10
 */
package com.eeeffff.limiter.dashboard.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.eeeffff.limiter.dashboard.redis.JedisClientCluster;
import com.eeeffff.limiter.dashboard.redis.JedisClientSharded;
import com.eeeffff.limiter.dashboard.redis.JedisClientSingle;
import com.eeeffff.limiter.dashboard.redis.RedisProperties;
import com.eeeffff.limiter.dashboard.redis.RedisTemplateWrapper;
import com.eeeffff.limiter.dashboard.redis.RedisUtil;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedisPool;

/**
 * redis autoConfig
 *
 * @author: fenglibin
 * @Date: 2017-11-03 10:10
 */
@Slf4j
@Configuration
@ConditionalOnClass(RedisUtil.class)
@EnableConfigurationProperties(RedisProperties.class)
public class RedisAutoConfigure {

	@Autowired
	private RedisProperties redisProperties;

	@Bean
	RedisUtil redisUtil() {

		GenericObjectPoolConfig<?> poolConfig = jedisPoolConfig();

		// jedisShard 切片模式
		if (null != redisProperties.getHosts() && redisProperties.getHosts().indexOf(",") > 0) {
			/// 获取服务器数组
			String[] serverArray = redisProperties.getHost().split(",");
			List<JedisShardInfo> jdsInfoList = new ArrayList<JedisShardInfo>(serverArray.length);
			if (redisProperties.getPassword() == null || redisProperties.getPassword().equals("")) {
				redisProperties.setPassword(null);
			}
			for (String ipPort : serverArray) {
				String[] ipPortPair = ipPort.split(":");
				JedisShardInfo jedisShardInfo = new JedisShardInfo(ipPortPair[0].trim(),
						Integer.parseInt(ipPortPair[1].trim()));
				jedisShardInfo.setPassword(redisProperties.getPassword());
				jedisShardInfo.setConnectionTimeout(redisProperties.getConnectionTimeout());
				jedisShardInfo.setSoTimeout(redisProperties.getSoTimeout());
				jdsInfoList.add(jedisShardInfo);
			}
			ShardedJedisPool jedisPool = new ShardedJedisPool(poolConfig, jdsInfoList);
			return new RedisUtil(new JedisClientSharded(jedisPool));

			// 非切片模式
		} else if (null != redisProperties.getHost() && !redisProperties.getHosts().equals("")) {
			JedisPool jedisPool = new JedisPool(poolConfig, redisProperties.getHost(), redisProperties.getPort(), 3000,
					redisProperties.getPassword(), redisProperties.getDatabase());
			return new RedisUtil(new JedisClientSingle(jedisPool));
		} else {
			// JedisCluster 模式
			JedisCluster jedisCluster = null;
			// 获取服务器数组
			String[] serverArray = redisProperties.getHost().split(",");
			Set<HostAndPort> nodes = new HashSet<HostAndPort>(serverArray.length);
			for (String ipPort : serverArray) {
				String[] ipPortPair = ipPort.split(":");
				nodes.add(new HostAndPort(ipPortPair[0].trim(), Integer.parseInt(ipPortPair[1].trim())));
			}
			if (redisProperties.getPassword() == null || redisProperties.getPassword().equals("")) {
				jedisCluster = new JedisCluster(nodes, redisProperties.getConnectionTimeout(),
						redisProperties.getSoTimeout(), redisProperties.getMaxAttempts(), poolConfig);
			} else {
				jedisCluster = new JedisCluster(nodes, redisProperties.getConnectionTimeout(),
						redisProperties.getSoTimeout(), redisProperties.getMaxAttempts(), redisProperties.getPassword(),
						poolConfig);
			}

			return new RedisUtil(new JedisClientCluster(jedisCluster));
		}
	}

	@Bean
	public JedisPoolConfig jedisPoolConfig() {
		RedisProperties.Pool pool = this.redisProperties.getJedis().getPool();

		JedisPoolConfig poolConfig = new JedisPoolConfig();
		if (pool != null) {
			poolConfig.setMaxIdle(pool.getMaxIdle());
			poolConfig.setMinIdle(pool.getMinIdle());
			poolConfig.setMaxTotal(pool.getMaxActive());
			poolConfig.setMaxWaitMillis(pool.getMaxWait().toMillis());
		}
		poolConfig.setTestOnCreate(true);
		poolConfig.setTestOnBorrow(true);
		poolConfig.setTestOnReturn(true);
		poolConfig.setTestWhileIdle(true);
		return poolConfig;
	}

	/**
	 * RedisTemplate配置
	 * 
	 * @param connectionFactory
	 * @return
	 */
	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
		log.info("init RedisTemplate");
		// 配置redisTemplate
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(factory);
		redisTemplate.setKeySerializer(new StringRedisSerializer());// key序列化
		redisTemplate.setHashKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());// value序列化
		redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
		redisTemplate.afterPropertiesSet();
		RedisTemplateWrapper.setRedisTemplate(redisTemplate);
		RedisTemplateWrapper.setRedisTemplate(redisTemplate);
		return redisTemplate;
	}
}
