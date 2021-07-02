/*
* Copyright All Rights Reserved.
* @Owner:fenglibin
* @Date:  2017-11-03 22:13
*/
package com.eeeffff.limiter.dashboard.redis;


import java.util.List;
import java.util.Map;

/**
 * redis interface
 *
 * @author: fenglibin
 * @date: 2017-12-05 12:08
 */
public interface JedisClient {


    /**
     * 从redis中根据key取值
     *
     * @param key 要取得值对应的key
     * @return 取到的value值
     * @author: fenglibin
     * @date : 2017/12/5:17:45
     */
    String get(String key);

    /**
     * 从redis中根据key取值
     *
     * @param key 要取得值对应的key
     * @return 取到的value值
     * @author: fenglibin
     */
    byte[] get(byte[] key);


    /**
     * 设置一个字符串类型的值,如果记录存在则覆盖原有值
     *
     * @param key   值对应的键
     * @param value 值
     * @return 状态码, 成功则返回OK
     * @author: fenglibin
     * @date : 2017/12/5:17:45
     */
    String set(String key, String value);

    /**
     * 设置一个值,如果记录存在则覆盖原有值
     *
     * @param key   值对应的键
     * @param value 值
     * @return 状态码, 成功则返回OK
     */
    String set(byte[] key, byte[] value);

    /**
     * 设置一个字符串类型的值同时设置过期时间(秒)
     *
     * @param key   值对应的键
     * @param value 值
     * @param second 过期时间
     * @return 状态码, 成功则返回OK
     * @author: fenglibin
     * @date : 2017/12/5:17:45
     */
    String set(String key, String value,Integer second);

    /**
     * 设置一个字符串类型的值同时设置过期时间(秒)
     *
     * @param key   值对应的键
     * @param value 值
     * @param second 过期时间
     * @return 状态码, 成功则返回OK
     * @author: fenglibin
     * @date : 2017/12/5:17:45
     */
    String set(byte[] key, byte[] value,Integer second);
    
    Long setnx(String key, String value);

    /**
     * 判断某个键值对是否存在
     *
     * @param key 根据键判断
     * @return 判断结果
     * @author: fenglibin
     * @date : 2017/12/5:17:45
     */
    Boolean exists(String key);

    /**
     * 返回hash中指定存储的值
     *
     * @param key   查找的存储的键
     * @param field 查找的存储的名字
     * @return 指定存储的值
     * @author: fenglibin
     * @date : 2017/12/5:17:45
     */
    String hget(String key, String field);

    /**
     * 添加一个对应关系
     *
     * @param key   存储的键
     * @param field 存储的名字
     * @param value 存储的值
     * @return 状态码, 1成功, 0失败, 如果field已存在将更新, 返回0
     * @author: fenglibin
     * @date : 2017/12/5:17:45
     */
    Long hset(String key, String field, String value);


    /**
     * 从hash中删除指定的存储
     *
     * @param key   存储的键
     * @param field 存储的名字
     * @return 状态码, 1成功, 0失败
     * @author: fenglibin
     * @date : 2017/12/5:17:45
     */
    Long hdel(String key, String... field);


    /**
     * 检测hash中指定的存储是否存在
     *
     * @param key   存储的键
     * @param field 存储的额名字
     * @return 状态码, 1代表成功, 0代表失败
     * @author: fenglibin
     * @date : 2017/12/5:17:45
     */
    Boolean hexists(String key, String field);


    /**
     * 以map的形式返回hash存储的名字和值
     *
     * @param key 存储的键
     * @return 根据key查找到的存储的名字和值
     * @author: fenglibin
     * @date : 2017/12/5:17:45
     */
    Map<String, String> hgetAll(String key);


    /**
     * 获取hash中value的集合
     *
     * @param key hash中存储的键
     * @return 指定键的所有value的集合
     * @author: fenglibin
     * @date : 2017/12/5:17:45
     */
    List<String> hvals(String key);


    /**
     * 原子性递增1并返回递增后的结果
     *
     * @param key 要使用的键
     * @return 递增后的结果
     * @author: fenglibin
     * @date : 2017/12/5:17:45
     */
    Long incr(String key);

    /**
     * 原子性递减1并返回递减后的结果
     *
     * @param key 要使用的键
     * @return 递减后的结果
     * @author: fenglibin
     * @date : 2017/12/5:17:45
     */
    Long decr(String key);

    /**
     * 设置键值对的过期时间
     *
     * @param key     要设置过期时间的k键值对的键
     * @param seconds 过期时间
     * @return 影响的记录数
     * @author: fenglibin
     * @date : 2017/12/5:17:45
     */
    Long expire(String key, int seconds);

    /**
     * 查看键值对的剩余时间
     *
     * @param key 要查看的键值对的键
     * @return 剩余时间
     * @author: fenglibin
     * @date : 2017/12/5:17:45
     */
    Long ttl(String key);

    /**
     * 根据存储的键删除存储
     *
     * @param key 存储的键
     * @return 状态码, 1成功, 0失败
     * @author: fenglibin
     * @date : 2017/12/5:17:45
     */
    Long del(String key);

    /**
     * 根据存储的键删除存储
     *
     * @param key 存储的键
     * @return 状态码, 1成功, 0失败
     * @author: fenglibin
     * @date : 2017/12/5:17:45
     */
    Long hdel(String hkey, String key);


}

