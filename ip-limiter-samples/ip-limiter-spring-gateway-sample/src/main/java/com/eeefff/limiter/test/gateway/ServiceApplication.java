package com.eeefff.limiter.test.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;


/**
 * GatewayApplication.
 * erp-data 上传下载接口不加入熔断超时处理.
 * 新加服务需要在这里申明@bean，当服务不可用才会触发503
 *
 * @author: ip-limiter
 * @date: 2017-10-16 15:25
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableFeignClients
public class ServiceApplication {

    private static Logger logger = LoggerFactory.getLogger(ServiceApplication.class);

    public static void main(String[] args) {

        SpringApplication.run(ServiceApplication.class, args);
        logger.info("services start");

    }
    
}
