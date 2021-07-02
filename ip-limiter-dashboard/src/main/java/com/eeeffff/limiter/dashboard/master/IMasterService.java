package com.eeeffff.limiter.dashboard.master;

/**
 * 考虑会同时存在多个ip-limiter-dashbord节点，但是有些定时任务需要在ip-limiter-dashbord执行，如应用的健康检查等，
 * 但是又不能够在所有的ip-limiter-dashbord都执行相同的定时任务，因而需要在所有的ip-limiter-dashbord中选举一个master，
 * 用于执行这些需要执行的定时任务
 * 
 * @author fenglibin
 *
 */
public interface IMasterService {
	/**
	 * 获取ip-limiter-dashbord的master节点
	 * 
	 * @return 代表ip-limiter-dashbord的IP及端口
	 */
	public String getMasterAddress();

	/**
	 * 检查当前Master是否可用
	 * 
	 * @return
	 */
	public boolean checkMasterHealth();

	/**
	 * 检查当前Master是否可用
	 * 
	 * @param masterAddress master的IP及端口
	 * @return
	 */
	public boolean checkMasterHealth(String masterAddress);

	/**
	 * 注册master节点
	 * 
	 * @param ipPort 节点的ＩＰ及端口
	 * @return 是否注册成功
	 */
	public boolean registerMasterAddress(String ipPort);
}
