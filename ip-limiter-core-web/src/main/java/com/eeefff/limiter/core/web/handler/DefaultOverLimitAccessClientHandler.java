package com.eeefff.limiter.core.web.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.eeefff.limiter.core.web.handler.OverLimitAccessClientHandler;

/**
 * 默认的超量ＩＰ访问处理类
 * 
 * @author fenglibin
 *
 */
public class DefaultOverLimitAccessClientHandler extends OverLimitAccessClientHandler {

	@Override
	public boolean handleOverLimitAccess(HttpServletRequest request, HttpServletResponse response, String ip,
			String accessUri) {
		response.setStatus(401);
		return false;
	}

}
