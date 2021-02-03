package com.eeefff.limiter.core.cache;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;
import javax.cache.Cache;
import javax.cache.Cache.Entry;

import org.springframework.stereotype.Service;

import com.eeefff.limiter.common.enumeration.AccessType;
import com.eeefff.limiter.common.util.TimeUtil;
import com.eeefff.limiter.common.vo.AccessVO;

import lombok.extern.slf4j.Slf4j;

/**
 * ＩＰ请求访问记录缓存
 * 
 * @author fenglibin
 *
 */
@Slf4j
@Service("secondIpCacheHelper")
public class SecondIpCacheHelper implements IpAccessCache {
	@Resource(name = "ipCache")
	Cache<String,Object> ipCache;

	// 根据访问类型，增加指定ＩＰ指定秒钟的访问总量、正常访问量或被拒绝的访问量
	@Override
	public boolean incrVisit(String ip, String url, AccessType accessType, int maxValue) {
		boolean result = true;
		// 当前秒钟
		final Long currentSencond = TimeUtil.currentTimeSeconds();

		// 判断Map中是否有包含当前IP的Map，如果没有则增加当前IP的信息
		Object accessMapObj = Optional.ofNullable(ipCache.get(ip)).orElseGet(() -> {
			String lock = new StringBuilder("seconds").append(ip).append("-lock").toString();
			synchronized (lock) {
				Object e = ipCache.get(ip);
				if (e == null) {
					HashMap<Long, AccessVO> map = new HashMap<Long, AccessVO>();
					map.put(currentSencond, AccessVO.builder().build());
					e = map;
					ipCache.put(ip, map);
				}
				return e;
			}

		});

		@SuppressWarnings("unchecked")
		HashMap<Long, AccessVO> accessMap = (HashMap<Long, AccessVO>) accessMapObj;
		AccessVO accessVO = accessMap.get(currentSencond);
		// 判断当前IP是否包括当前秒的统计纬度，没有则为其补充一个存储对象
		if (accessVO == null) {
			String lock = new StringBuilder("seconds").append(ip).append("-").append(currentSencond).append("-lock").toString();
			synchronized (lock) {
				accessVO = accessMap.get(currentSencond);
				if (accessVO == null) {
					accessVO = AccessVO.builder().build();
					accessMap.put(currentSencond, accessVO);
				}
			}
		}
		accessVO.getTotal().incrementAndGet();
		if (accessType == AccessType.Normal) {
			String lock = new StringBuilder("seconds").append(ip).append("--").append(currentSencond).append("-lock").toString();
			synchronized(lock) {
				if (accessVO.getNormal().get() < maxValue) {
					accessVO.getNormal().incrementAndGet();
				}else {
					accessVO.getBlock().incrementAndGet();
					result = false;
				}
			}
		} else if (accessType == AccessType.Block) {
			accessVO.getBlock().incrementAndGet();
		}
		// 增加当前被访问Url的访问次数
		AtomicInteger urlAccess = accessVO.getUrlsAccess().get(url);
		if (urlAccess == null) {
			String lock = new StringBuilder("seconds").append(ip).append("-").append(url).append("-lock").toString();
			synchronized (lock) {
				urlAccess = accessVO.getUrlsAccess().get(url);
				if (urlAccess == null) {
					accessVO.getUrlsAccess().put(url, new AtomicInteger(1));
				}else {
					urlAccess.incrementAndGet();
				}
			}
		}else {
			urlAccess.incrementAndGet();
		}
		return result;
	}

	/**
	 * 获取指定IP的以秒为统计纬度的所有可用的统计
	 * 
	 * @param ip
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public HashMap<Long, AccessVO> getVisit(String ip) {
		Object e = ipCache.get(ip);
		if (e == null) {
			return null;
		}
		return (HashMap<Long, AccessVO>) e;
	}

	/**
	 * 获取指定IP指定时间(秒)的统计，该统计是获取内存中的统计，如果内存中的统计被删除则不能够获取到值
	 * 
	 * @param ip
	 * @param senconds
	 * @return
	 */
	@Override
	public AccessVO getVisit(String ip, Long senconds) {
		Object e = ipCache.get(ip);
		if (e == null) {
			return null;
		}
		@SuppressWarnings("unchecked")
		HashMap<Long, AccessVO> map = (HashMap<Long, AccessVO>) e;
		return map.get(senconds);
	}

	/**
	 * 获取缓存中所有ＩＰ访问行为缓存的内容
	 * 
	 * @return
	 */
	@Override
	public Map<String, HashMap<Long, AccessVO>> getAllVisit() {
		Map<String, HashMap<Long, AccessVO>> result = new HashMap<String, HashMap<Long, AccessVO>>();
		Iterator<Entry<String, Object>> entries = ipCache.iterator();
		if(!entries.hasNext()) {
			log.info("ＩＰ访问行为缓存内容为空");
			return result;
		}
		entries.forEachRemaining(e->{
			@SuppressWarnings("unchecked")
			HashMap<Long, AccessVO> value = (HashMap<Long, AccessVO>) ipCache.get(e.getKey());
			result.put(e.getKey(), value);
		});
		return result;
	}

	/**
	 * 清除指定ＩＰ的访问记录
	 * 
	 * @param ip
	 * @return
	 */
	@Override
	public boolean cleanVisit(String ip) {
		return ipCache.remove(ip);
	}

	/**
	 * 清除全部ＩＰ的访问记录
	 * 
	 * @return
	 */
	@Override
	public int cleanAllVisit() {
		AtomicInteger size = new AtomicInteger(0);
		Iterator<Entry<String, Object>> entries = ipCache.iterator();
		entries.forEachRemaining(e->{
			size.incrementAndGet();
			ipCache.remove(e.getKey());
		});
		return size.get();
	}

}
