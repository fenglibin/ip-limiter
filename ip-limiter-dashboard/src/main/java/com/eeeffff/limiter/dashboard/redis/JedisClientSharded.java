/*
 * Copyright All Rights Reserved.
 * @owner: fenglibin
 * @date:  2017-12-05 16:07
 */
package com.eeeffff.limiter.dashboard.redis;

import java.util.List;
import java.util.Map;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

/**
 * 类或方法的功能描述 :TODO
 *
 * @author: fenglibin
 * @date: 2017-12-05 16:07
 */
public class JedisClientSharded implements JedisClient {

	private ShardedJedisPool jedisPool;

	public JedisClientSharded(ShardedJedisPool shardedJedisPool) {
		jedisPool = shardedJedisPool;
	}

	private <T> T excute(JedisCallback<T, ShardedJedis> jedisJedisCallback) {
		ShardedJedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			return jedisJedisCallback.doJedisCallbak(jedis);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
		return null;
	}

	/**
	 * @param key   键名
	 * @param value 被保存的值
	 * @Description: 当字符串类型的时在value后面追加
	 * @remark
	 * @author
	 * @date 2017/11/6:11:23
	 */
	public long append(String key, String value) {

		return excute((jedis) -> {
			return jedis.append(key, value);
		});
	}

	/**
	 * @Description: 设置过期时间
	 * @remark
	 * @author
	 * @date 2016-7-8
	 */
	@Override
	public Long expire(String key, int seconds) {

		return excute((jedis) -> {
			return jedis.expire(key, seconds);
		});
	}

	@Override
	public Long ttl(String key) {

		return excute((jedis) -> {
			return jedis.ttl(key);
		});
	}

	@Override
	public Long del(String key) {
		return excute((jedis) -> {
			return jedis.del(key);
		});
	}

	/**
	 * @param key 键名
	 * @Description: 获取数据(string)
	 * @remark
	 * @author
	 * @date 2017/11/6:11:23
	 */
	@Override
	public String get(String key) {
		return excute((jedis) -> {
			return jedis.get(key);
		});
	}

	@Override
	public byte[] get(byte[] key) {
		return excute((jedis) -> {
			return jedis.get(key);
		});
	}

	@Override
	public String set(String key, String value) {

		return excute((jedis) -> {
			return jedis.set(key, value);
		});
	}

	@Override
	public String set(byte[] key, byte[] value) {
		return excute((jedis) -> {
			return jedis.set(key, value);
		});
	}

	@Override
	public String set(String key, String value, Integer second) {
		return excute((jedis) -> {
			return jedis.setex(key, second, value);
		});
	}

	@Override
	public String set(byte[] key, byte[] value, Integer second) {
		return excute((jedis) -> {
			return jedis.setex(key, second, value);
		});
	}

	@Override
	public String hget(String key, String field) {

		return excute((jedis) -> {
			return jedis.hget(key, field);
		});

	}

	@Override
	public Long hset(String key, String field, String value) {

		return excute((jedis) -> {
			return jedis.hset(key, field, value);
		});
	}

	@Override
	public Long hdel(String key, String... field) {
		return excute((jedis) -> {
			return jedis.hdel(key, field);
		});
	}

	@Override
	public Boolean hexists(String key, String field) {
		return excute((jedis) -> {
			return jedis.hexists(key, field);
		});
	}

	@Override
	public Long incr(String key) {
		return excute((jedis) -> {
			return jedis.incr(key);
		});

	}

	@Override
	public Long decr(String key) {
		return excute((jedis) -> {
			return jedis.decr(key);
		});

	}

	@Override
	public Long hdel(String key, String field) {
		return excute((jedis) -> {
			return jedis.hdel(key, field);
		});
	}

	@Override
	public Map<String, String> hgetAll(String key) {

		return excute((jedis) -> {
			return jedis.hgetAll(key);
		});
	}

	@Override
	public List<String> hvals(String key) {
		return excute((jedis) -> {
			return jedis.hvals(key);
		});
	}

	public List<String> hmget(String key, String... fields) {

		return excute((jedis) -> {
			return jedis.hmget(key, fields);
		});
	}

	@Override
	public Boolean exists(String key) {
		return excute((jedis) -> {
			return jedis.exists(key);
		});
	}

	@Override
	public Long setnx(String key, String value) {
		return excute((jedis) -> {
			return jedis.setnx(key, value);
		});
	}

}
