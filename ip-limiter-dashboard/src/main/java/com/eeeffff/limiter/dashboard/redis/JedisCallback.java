/*
* Copyright All Rights Reserved.
* @Owner:fenglibin
* @Date:  2017-11-03 22:15
*/
package com.eeeffff.limiter.dashboard.redis;

/**
 * @author fenglibin
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description:
 * @date 2017-11-03 22:15
 */

public interface JedisCallback<T, E> {
    T doJedisCallbak(E e);
}

