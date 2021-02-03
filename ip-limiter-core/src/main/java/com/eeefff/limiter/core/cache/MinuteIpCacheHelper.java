package com.eeefff.limiter.core.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.cache.Cache;

import org.springframework.stereotype.Service;

import com.eeefff.limiter.common.enumeration.AccessType;
import com.eeefff.limiter.common.util.TimeUtil;
import com.eeefff.limiter.common.vo.AccessVO;

/**
 * ＩＰ请求访问记录缓存
 * 
 * @author fenglibin
 *
 */
@Service("minuteIpCacheHelper")
public class MinuteIpCacheHelper implements IpAccessCache {
	@Resource(name = "ipLimitCache")
	private Cache<String,Object> ipLimitCache;
	// 以分钟为单存放IP访问量，最外层map的Key为IP，里面map的Key为分钟，值为这一分钟的即时访问数
	// private static Map<String, HashMap<Long, AccessVO>> minuteAccessCache = new ConcurrentHashMap<String, HashMap<Long, AccessVO>>();
	private static Map<String, HashMap<Long, AccessVO>> minuteAccessCache = null;
	
	@SuppressWarnings("unchecked")
	@PostConstruct
	public void init() {
		ipLimitCache.put("minuteAccessCache",new ConcurrentHashMap<String, HashMap<Long, AccessVO>>());
		minuteAccessCache = (Map<String, HashMap<Long, AccessVO>>) ipLimitCache.get("minuteAccessCache");
	}
	// 根据访问类型，增加指定ＩＰ指定分钟的访问总量、正常访问量或被拒绝的访问量
	@Override
	public boolean incrVisit(String ip, String url, AccessType accessType, int maxValue) {
		// 当前分钟
		final Long currentMinute = TimeUtil.currentTimeMinutes();
		HashMap<Long, AccessVO> accessMap = Optional.ofNullable(minuteAccessCache.get(ip)).orElseGet(() -> {
			String lock = new StringBuilder("minutes").append(ip).append("-lock").toString();
			synchronized (lock) {
				HashMap<Long, AccessVO> _accessMap = minuteAccessCache.get(ip);
				if (_accessMap == null) {
					_accessMap = new HashMap<Long, AccessVO>();
					_accessMap.put(currentMinute, AccessVO.builder().ip(ip).currentMinutes(currentMinute).build());
					minuteAccessCache.put(ip, _accessMap);
				}
				return _accessMap;
			}

		});
		AccessVO accessVO = accessMap.get(currentMinute);
		if (accessVO == null) {
			String lock = new StringBuilder("minutes").append(ip).append("-").append(currentMinute).append("-lock").toString();
			synchronized (lock) {
				accessVO = accessMap.get(currentMinute);
				if (accessVO == null) {
					accessVO = AccessVO.builder().ip(ip).currentMinutes(currentMinute).build();
					accessMap.put(currentMinute, accessVO);
				}
			}
		}
		// 增加总的访问的次数、正常或被拒绝的访问次数
		accessVO.getTotal().incrementAndGet();
		if (accessType == AccessType.Normal) {
			accessVO.getNormal().incrementAndGet();
		} else if (accessType == AccessType.Block) {
			accessVO.getBlock().incrementAndGet();
		}
		// 增加当前被访问Url的访问次数
		AtomicInteger urlAccess = accessVO.getUrlsAccess().get(url);
		if (urlAccess == null) {
			String lock = new StringBuilder("minutes").append(ip).append("-").append(url).append("-lock").toString();
			synchronized (lock) {
				urlAccess = accessVO.getUrlsAccess().get(url);
				if (urlAccess == null) {
					accessVO.getUrlsAccess().put(url, new AtomicInteger(1));
				}
			}
		} else {
			urlAccess.incrementAndGet();
		}
		return true;
	}

	/**
	 * minuteAccessCache 获取指定IP的以秒为统计纬度的所有可用的统计
	 * 
	 * @param ip
	 * @return
	 */
	@Override
	public HashMap<Long, AccessVO> getVisit(String ip) {
		return minuteAccessCache.get(ip);
	}

	/**
	 * 获取指定IP指定时间(秒)的统计，该统计是获取内存中的统计，如果内存中的统计被删除则不能够获取到值
	 * 
	 * @param ip
	 * @param minute
	 * @return
	 */
	@Override
	public AccessVO getVisit(String ip, Long minute) {
		HashMap<Long, AccessVO> accessVO = minuteAccessCache.get(ip);
		if (accessVO == null) {
			return null;
		}
		return accessVO.get(minute);
	}

	/**
	 * 获取缓存中所有缓存的内容
	 * 
	 * @return
	 */
	@Override
	public Map<String, HashMap<Long, AccessVO>> getAllVisit() {
		return minuteAccessCache;
	}

	/**
	 * 清除指定ＩＰ的访问记录
	 * 
	 * @param ip
	 * @return
	 */
	@Override
	public boolean cleanVisit(String ip) {
		minuteAccessCache.remove(ip);
		return true;
	}

	/**
	 * 清除全部缓存，返回清除的数量
	 * 
	 * @return
	 */
	@Override
	public int cleanAllVisit() {
		int size = minuteAccessCache.size();
		minuteAccessCache.clear();
		return size;
	}

	public static Map<String, HashMap<Long, AccessVO>> getMinuteAccessCache() {
		return minuteAccessCache;
	}

}
