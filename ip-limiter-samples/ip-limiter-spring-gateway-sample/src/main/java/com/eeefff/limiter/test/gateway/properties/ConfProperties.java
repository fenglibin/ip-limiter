/*
* Copyright All Rights Reserved.
* @owner: ip-limiter
* @date:  2018-04-28 10:54
*/
package com.eeefff.limiter.test.gateway.properties;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 类或方法的功能描述 :TODO
 *
 * @author: ip-limiter
 * @date: 2018-04-28 10:54
 */

@Component
@ConfigurationProperties(prefix = "spring.cloud.gateway")
public class ConfProperties {

    /**
     * 不用登陆校验的列表.
     */
    @Value("${allowlist:}")
    private List<String> allowlist;  
    
    public List<String> getAllowlist() {
        return allowlist;
    }

    public void setAllowlist(List<String> allowlist) {
        this.allowlist = allowlist;
    }
}

