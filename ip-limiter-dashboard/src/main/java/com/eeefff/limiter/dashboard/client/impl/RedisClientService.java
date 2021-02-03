package com.eeefff.limiter.dashboard.client.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.eeefff.limiter.common.action.RetryAction;
import com.eeefff.limiter.common.util.RetryHelper;
import com.eeefff.limiter.common.vo.Client;
import com.eeefff.limiter.dashboard.client.IClientService;
import com.eeefff.limiter.dashboard.constants.RedisKey;
import com.eeefff.limiter.dashboard.redis.RedisTemplateWrapper;

import lombok.extern.slf4j.Slf4j;

@Service("redisClientService")
@Slf4j
public class RedisClientService implements IClientService {

	@SuppressWarnings("unchecked")
	@Override
	public boolean saveClient(String appName, String ip, int port) {
		String lockKey = new StringBuilder(RedisKey.REGISTERED_CLIENT_KEY).append("-").append(appName).toString();
		boolean isGetLock = RetryHelper.doRetryAction(new RetryAction() {
			@Override
			public boolean doAction() {
				return RedisTemplateWrapper.getLock(lockKey);
			}
		});
		if (isGetLock) {
			try {
				Object clientObj = RedisTemplateWrapper.hGet(RedisKey.REGISTERED_CLIENT_KEY, appName);
				Map<String, Client> clients;
				if (clientObj == null) {
					clients = new HashMap<String, Client>();
				} else {
					clients = (Map<String, Client>) clientObj;
				}
				String ipAndPort = new StringBuilder(ip).append(":").append(port).toString();
				clients.put(ipAndPort, Client.builder().ip(ip).port(port).build());
				RedisTemplateWrapper.hSet(RedisKey.REGISTERED_CLIENT_KEY, appName, clients);
			} finally {
				RedisTemplateWrapper.delLock(lockKey);
			}
		}
		return isGetLock;
	}

	@Override
	public List<Object> getAllAppNames() {
		List<Object> clients = RedisTemplateWrapper.hGetHashKeys(RedisKey.REGISTERED_CLIENT_KEY);
		return clients;
	}

	@Override
	public List<String> getAppRegisteredIps(String appName) {
		List<String> list = new ArrayList<String>();
		@SuppressWarnings("unchecked")
		Map<String, Client> clients = (Map<String, Client>) RedisTemplateWrapper
				.hGet(RedisKey.REGISTERED_CLIENT_KEY, appName);
		if (!CollectionUtils.isEmpty(clients)) {
			list.addAll(clients.keySet());
		}
		return list;
	}

	@Override
	public void delApp(String appName) {
		RedisTemplateWrapper.hDel(RedisKey.REGISTERED_CLIENT_KEY, appName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void delAppClient(String appName, String ipPort) {
		String lockKey = new StringBuilder(RedisKey.REGISTERED_CLIENT_KEY).append("-").append(appName).toString();
		boolean isGetLock = RetryHelper.doRetryAction(new RetryAction() {
			@Override
			public boolean doAction() {
				return RedisTemplateWrapper.getLock(lockKey);
			}
		});
		if (isGetLock) {
			try {
				Object clientObj = RedisTemplateWrapper.hGet(RedisKey.REGISTERED_CLIENT_KEY, appName);
				Map<String, Client> clients;
				if (clientObj == null) {
					clients = new HashMap<String, Client>();
				} else {
					clients = (Map<String, Client>) clientObj;
				}
				clients.remove(ipPort);
				RedisTemplateWrapper.hSet(RedisKey.REGISTERED_CLIENT_KEY, appName, clients);
			} finally {
				RedisTemplateWrapper.delLock(lockKey);
			}
		} else {
			log.warn("delAppClient操作没有获取到锁:{}", lockKey);
		}
	}

}
