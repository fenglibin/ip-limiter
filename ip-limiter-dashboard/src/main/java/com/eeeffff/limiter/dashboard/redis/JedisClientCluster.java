/*
* Copyright All Rights Reserved.
* @owner: fenglibin
* @date:  2017-12-05 12:08
*/
package com.eeeffff.limiter.dashboard.redis;

import redis.clients.jedis.JedisCluster;

import java.util.List;
import java.util.Map;

/**
 * 类或方法的功能描述 :TODO
 *
 * @author: fenglibin
 * @date: 2017-12-05 12:08
 */
public class JedisClientCluster implements JedisClient {


    private static JedisCluster jedisclient;


    public JedisClientCluster(JedisCluster jedisCluster) {
        jedisclient = jedisCluster;
    }

    private static <T> T excute(JedisCallback<T, JedisCluster> jedisClusterCallback) {
        try {
            return jedisClusterCallback.doJedisCallbak(jedisclient);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedisclient != null) {
                jedisclient.close();
            }
        }
        return null;
    }

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
            return jedis.setex(key, second,value);
        });
    }

    @Override
    public String set(byte[] key, byte[] value, Integer second) {
        return excute((jedis) -> {
            return jedis.setex(key, second,value);
        });
    }

    @Override
    public Boolean exists(String key) {
        return excute((jedis) -> {
            return jedis.exists(key);
        });
    }


    @Override
    public String hget(String hkey, String key) {
        return excute((jedis) -> {
            return jedis.hget(hkey, key);
        });
    }

    @Override
    public Long hset(String hkey, String key, String value) {
        return excute((jedis) -> {
            return jedis.hset(hkey, key, value);
        });
    }

    @Override
    public Long hdel(String key, String... field) {
        return excute((jedis) -> {
            return jedis.hdel(key,field);
        });
    }

    @Override
    public Boolean hexists(String key, String field) {
        return excute((jedis) -> {
            return jedis.hexists(key,field);
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
    public Long expire(String key, int second) {
        return excute((jedis) -> {
            return jedis.expire(key, second);
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

    @Override
    public Long hdel(String hkey, String key) {
        return excute((jedis) -> {
            return jedis.hdel(hkey, key);
        });
    }

	@Override
	public Long setnx(String key, String value) {
		return excute((jedis) -> {
            return jedis.setnx(key, value);
        });
	}

}

