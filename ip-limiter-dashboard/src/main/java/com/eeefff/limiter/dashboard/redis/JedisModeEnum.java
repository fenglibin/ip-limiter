/*
* Copyright All Rights Reserved.
* @owner: fenglibin
* @date:  2017-12-05 19:21
*/
package com.eeefff.limiter.dashboard.redis;

/**
 * 类或方法的功能描述 :TODO
 *
 * @author: fenglibin
 * @date: 2017-12-05 19:21
 */
public enum  JedisModeEnum {
    // 基本模式
    BASIC,
    // 分片模式
    SHARDED,
    // 有监控的M-S
    //SENTINEL,
    // 集群
    CLUSTER
}

