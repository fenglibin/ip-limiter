/*
 * Copyright All Rights Reserved.
 * @author jiangchao
 * @date  2019-04-15 10:19
 */
package com.eeeffff.limiter.dashboard.redis;

import java.util.List;
import java.util.Map;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * jedis标准模式 （非切片和集群模式）
 *
 * @author jiangchaoo
 */
public class JedisClientSingle implements JedisClient {

	private JedisPool jedisPool;

	public JedisClientSingle(JedisPool jedisPoolSingle) {
		jedisPool = jedisPoolSingle;
	}

	private <T> T excute(JedisCallback<T, Jedis> jedisJedisCallback) {
		Jedis jedis = null;
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

	@Override
	public String get(String key) {
		return this.excute((jedis) -> {
			return jedis.get(key);
		});
	}

	@Override
	public byte[] get(byte[] key) {
		return this.excute((jedis) -> {
			return jedis.get(key);
		});
	}

	@Override
	public String set(String key, String value) {
		return this.excute((jedis) -> {
			return jedis.set(key, value);
		});
	}

	@Override
	public String set(byte[] key, byte[] value) {
		return this.excute((jedis) -> {
			return jedis.set(key, value);
		});
	}

	@Override
	public String set(String key, String value, Integer seconds) {
		return this.excute((jedis) -> {
			return jedis.setex(key, seconds, value);
		});
	}

	@Override
	public String set(byte[] key, byte[] value, Integer second) {
		return this.excute((jedis) -> {
			return jedis.set(key, value);
		});
	}

	@Override
	public Boolean exists(String key) {
		return this.excute((jedis) -> {
			return jedis.exists(key);
		});
	}

	@Override
	public String hget(String key, String field) {
		return this.excute((jedis) -> {
			return jedis.hget(key, field);
		});
	}

	@Override
	public Long hset(String key, String field, String value) {
		return this.excute((jedis) -> {
			return jedis.hset(key, field, value);
		});
	}

	@Override
	public Long hdel(String key, String... field) {
		return this.excute((jedis) -> {
			return jedis.hdel(key, field);
		});
	}

	@Override
	public Boolean hexists(String key, String field) {
		return this.excute((jedis) -> {
			return jedis.hexists(key, field);
		});
	}

	@Override
	public Map<String, String> hgetAll(String key) {
		return this.excute((jedis) -> {
			return jedis.hgetAll(key);
		});
	}

	@Override
	public List<String> hvals(String key) {
		return this.excute((jedis) -> {
			return jedis.hvals(key);
		});
	}

	@Override
	public Long incr(String key) {
		return this.excute((jedis) -> {
			return jedis.incr(key);
		});
	}

	@Override
	public Long decr(String key) {
		return this.excute((jedis) -> {
			return jedis.decr(key);
		});
	}

	@Override
	public Long expire(String key, int seconds) {
		return this.excute((jedis) -> {
			return jedis.expire(key, seconds);
		});
	}

	@Override
	public Long ttl(String key) {
		return this.excute((jedis) -> {
			return jedis.ttl(key);
		});
	}

	@Override
	public Long del(String key) {
		return this.excute((jedis) -> {
			return jedis.del(key);
		});
	}

	@Override
	public Long hdel(String hkey, String key) {
		return this.excute((jedis) -> {
			return jedis.hdel(hkey, key);
		});
	}

	@Override
	public Long setnx(String key, String value) {
		return this.excute((jedis) -> {
			return jedis.setnx(key, value);
		});
	}

}
