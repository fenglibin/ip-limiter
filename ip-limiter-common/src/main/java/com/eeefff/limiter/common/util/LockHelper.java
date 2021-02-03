package com.eeefff.limiter.common.util;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Resource;
import javax.cache.Cache;

import org.springframework.stereotype.Service;

/**
 * 锁的工具类
 * 
 * @author fenglibin
 *
 */
@Service("lockHelper")
public class LockHelper {
	ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	Lock newLock = readWriteLock.writeLock();
	@Resource(name = "lockCache")
	private Cache<String,Object> cache;

	/**
	 * 根据字符串获取对应的锁
	 * 
	 * @param lockKey
	 * @return
	 */
	public Object getLock(String lockKey) {
		Object lockElement = cache.get(lockKey);
		if (lockElement == null) {
			try {
				newLock.lock();
				lockElement = cache.get(lockKey);
				if (lockElement == null) {
					// Element用于存放字符串对应的锁，Key为对应的字符串，Value为其对应的锁，可以跨多线程使用
					cache.put(lockKey,new Object());
				}
			} finally {
				newLock.unlock();
			}
		}
		return lockElement;
	}
}
