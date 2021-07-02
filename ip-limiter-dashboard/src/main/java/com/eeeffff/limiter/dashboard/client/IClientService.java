package com.eeeffff.limiter.dashboard.client;

import java.util.List;

/**
 * 处理客户端请求的服务
 * 
 * @author fenglibin
 *
 */
public interface IClientService {
	/**
	 * 保存上报的Client信息
	 * 
	 * @param appName 应用的名称
	 * @param ip      当前应用的ip
	 * @param port    当前应用用于接收控制台指令的port
	 * @return
	 */
	public boolean saveClient(String appName, String ip, int port);

	/**
	 * 获取所有注册的客户端应用的名称列表
	 * 
	 * @return
	 */
	public List<Object> getAllAppNames();

	/**
	 * 获取指定的客户端应用的所有节点ＩＰ及端口列表
	 * 
	 * @return
	 */
	public List<String> getAppRegisteredIps(String appName);

	/**
	 * 根据传入的appName，从注册中心删除该应用
	 * 
	 * @param appName 应用的名称
	 */
	public void delApp(String appName);

	/**
	 * 根据传入的appName及该appName对应某个客户端的IP+端口，从注册中心删除该应用的该客户端
	 * 
	 * @param appName 应用的名称
	 * @param ipPort  应用的ＩＰ及端口
	 */
	public void delAppClient(String appName, String ipPort);
}
