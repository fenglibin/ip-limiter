package com.eeeffff.limiter.dashboard.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisUtil {

	private final Logger logger = LoggerFactory.getLogger(RedisUtil.class);

	private static JedisClient jedisClient;
	
	private static RedisUtil redisUtil;

	public RedisUtil(JedisClient jedisClient) {
		RedisUtil.jedisClient = jedisClient;
		redisUtil = this;
	}
	
	public static RedisUtil getRedisUtil() {
		return redisUtil;
	}

	/**
	 * 请求等待
	 *
	 * @param key    [key]
	 * @param count  [count]
	 * @param second [second]
	 * @return void
	 * @remark 根据key X 秒内最多访问Y次,超过限制则休眠等待
	 * @author: fenglibin
	 * @date : 2018/4/27:20:35
	 */
	public void requestLimit(String key, int count, int second) {

		Long currentTime = System.currentTimeMillis();// 开始加锁的时间
		// todo:获取分布式锁
		String lastTimeSting = get(key);
		if (lastTimeSting == null || lastTimeSting == "") {
			long nextTime = currentTime + 1000 * second;
			logger.info("setKey {} lock second {} ms ,next available at {}", key, second, nextTime);
			set(key, nextTime + "");
			return;
		} else {
			Long lastTime = Long.parseLong(lastTimeSting.trim()) + 1000 * second;
			logger.info("updateKey {} lockd second {} ms ,next available at {} ", key, second, lastTime);
			set(key, currentTime + "");
			Long sleepMillis = lastTime - currentTime;
			logger.info("sleep {} ms", sleepMillis / 1000);
			try {
				Thread.sleep(sleepMillis);
			} catch (Exception ex) {

			}
		}

	}

	/**
	 * get 根据key获取值.
	 *
	 * @author jiang chao
	 * @param key [key]
	 * @return java.lang.String
	 * @throws @date 2019/4/15:14:35
	 */
	public String get(String key) {
		return jedisClient.get(key);
	}

	/**
	 * get 根据key获取值.
	 *
	 * @author jiang chao
	 * @param key [key]
	 * @return java.lang.byte[]
	 * @throws @date 2019/4/15:14:35
	 */
	public byte[] get(byte[] key) {
		return jedisClient.get(key);
	}

	/**
	 * set 设置一个字符串类型的值,如果记录存在则覆盖原有值.
	 *
	 * @author jiang chao
	 * @param key   [key]
	 * @param value [value]
	 * @return java.lang.String
	 * @throws @date 2019/4/15:14:37
	 */
	public String set(String key, String value) {
		return jedisClient.set(key, value);
	}

	/**
	 * set 设置一个字节类型的值.
	 * <p>
	 * 如果记录存在则覆盖原有值.
	 * </p>
	 * 
	 * @author jiang chao
	 * @param key   [key]
	 * @param value [value]
	 * @return java.lang.String
	 * @throws @date 2019/4/15:14:37
	 */
	public String set(byte[] key, byte[] value) {
		return jedisClient.set(key, value);
	}

	public String set(byte[] key, byte[] value, Integer second) {
		return jedisClient.set(key, value, second);
	}

	/**
	 * 设置一个字符串类型的值,同时设置过期时间.
	 * <p>
	 * 如果记录存在则覆盖原有值.
	 * </p>
	 *
	 * @author jiang chao
	 * @param key   [key]
	 * @param value [value]
	 * @return java.lang.String
	 * @throws @date 2019/4/15:14:37
	 */
	public String set(String key, String value, Integer second) {
		return jedisClient.set(key, value, second);
	}

	public String hget(String hkey, String key) {
		return jedisClient.hget(hkey, key);
	}

	public Long hset(String hkey, String key, String value) {
		return jedisClient.hset(hkey, key, value);
	}

	public Long incr(String key) {
		return jedisClient.incr(key);
	}

	public Long decr(String key) {
		return jedisClient.decr(key);
	}

	public Long expire(String key, int second) {
		return jedisClient.expire(key, second);
	}

	public Long ttl(String key) {
		return jedisClient.ttl(key);
	}

	public Long del(String key) {
		return jedisClient.del(key);
	}

	public Long hdel(String hkey, String key) {
		return jedisClient.hdel(hkey, key);
	}

	public Long setnx(String key, String value) {
		return jedisClient.setnx(key, value);
	}

}
