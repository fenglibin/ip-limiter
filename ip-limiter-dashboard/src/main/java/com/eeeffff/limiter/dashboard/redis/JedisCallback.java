/*
* Copyright All Rights Reserved.
* @Owner:fenglibin
* @Date:  2017-11-03 22:15
*/
package com.eeeffff.limiter.dashboard.redis;

/**
 * @author fenglibin
 */

public interface JedisCallback<T, E> {
    T doJedisCallbak(E e);
}

