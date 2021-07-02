package com.eeeffff.limiter.dashboard.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.eeeffff.limiter.common.util.HttpClientUtil;
import com.eeeffff.limiter.common.util.NetUtil;
import com.eeeffff.limiter.core.config.SystemEnv;

import lombok.Getter;

@Component
@Getter
public class SystemConfig {
	@Autowired
	private IpLimiterDashboardConfigurationProperties ipLimiterConfigurationProperties;
	@Autowired
	private LimiterResource limiterResource;
	// 项目对外服务的端口
	@Value("${server.port:#{8080}}")
	private int serverPort;
	// 当前服务所在的ＩＰ地址
	private String ip;
	// 当前服务的ＩＰ与端口
	private String ipPort;

	@PostConstruct
	public void init() {
		ip = NetUtil.getLinuxLocalIp();
		ipPort = new StringBuilder(ip).append(":").append(serverPort).toString();
		initHttpClient();
		registerToServer();
	}
	private void initHttpClient() {
		HttpClientUtil.init(ipLimiterConfigurationProperties.getConnectTimeout(),
				ipLimiterConfigurationProperties.getSoTimeout(), ipLimiterConfigurationProperties.getMaxConnTotal(),
				ipLimiterConfigurationProperties.getMaxConnPerRoute());
	}
	/**
     * 往控制台注册
     */
    private void registerToServer() {
            new Thread() {
                    @SuppressWarnings("synthetic-access")
                    public void run() {
                            try {
                                    // 等待Redis初使化完成
                                    Thread.sleep(10000);
                                    String ip = NetUtil.getLinuxLocalIp();
                                    limiterResource.getClientService().saveClient(SystemEnv.getAppName(), ip, serverPort);
                            } catch (Exception e) {
                            }
                    }
            }.start();

    }
}
