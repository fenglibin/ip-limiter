import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.eeeffff.limiter.common.util.IpHelper;
import com.eeeffff.limiter.common.vo.AccessVO;

public class Test {

	public static void main(String[] args) {
		getOneLevelIpAddressTest();
		getTwoLevelIpAddressTest();
		getThreeLevelIpAddressTest();
	}
	
	public static void sortTest() {
		List<AccessVO> topAccess = new ArrayList<AccessVO>();
		AccessVO vo1 = AccessVO.builder().total(new AtomicInteger(100)).build();
		AccessVO vo2 = AccessVO.builder().total(new AtomicInteger(99)).build();
		AccessVO vo3 = AccessVO.builder().total(new AtomicInteger(101)).build();
		
		topAccess.add(vo1);
		topAccess.add(vo2);
		topAccess.add(vo3);
		topAccess.forEach(o -> {
			System.out.println(o.getTotal());
		});
		
		topAccess = topAccess.stream().sorted((o1, o2) -> {
			if (o1.getTotal().longValue() < o2.getTotal().longValue()) {
				return 1;
			} else if (o1.getTotal().longValue() > o2.getTotal().longValue()) {
				return -1;
			} else {
				return 0;
			}
		}).limit(2).collect(Collectors.toList());
		topAccess.forEach(o -> {
			System.out.println(o.getTotal());
		});
	}
	public static void substrTest() {
		String ip = "127.0.*.*";
		System.out.println(IpHelper.removeIpWildcard(ip));
	}
	
	public static void getOneLevelIpAddressTest() {
		String ip = "127.01.0.1";
		System.out.println(IpHelper.getOneLevelIpAddress(ip));
	}
	
	public static void getTwoLevelIpAddressTest() {
		String ip = "127.01.0.1";
		System.out.println(IpHelper.getTwoLevelIpAddress(ip));
	}
	
	public static void getThreeLevelIpAddressTest() {
		String ip = "127.02.01.1";
		System.out.println(IpHelper.getThreeLevelIpAddress(ip));
	}
}
