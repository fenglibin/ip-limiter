/*
 * TOP SECRET Copyright 2006-2015 Transsion.com All right reserved. This software is the confidential and proprietary
 * information of Transsion.com ("Confidential Information"). You shall not disclose such Confidential Information and
 * shall use it only in accordance with the terms of the license agreement you entered into with Transsion.com.
 */
package com.eeefff.limiter.dashboard.filter;

/**
 * ClassName:ResponseFilter <br/>
 * Date: 2018年9月28日 下午3:05:32 <br/>
 * 
 * @author fenglibin1982@163.com
 * @Blog http://blog.csdn.net/fenglibing
 * @version
 * @see
 */
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * 返回值输出过滤器，这里用来加密返回值
 * 
 * @Title: ResponseFilter
 * @Description:
 * @author kokJuis
 * @date 上午9:52:42
 */
@WebFilter(urlPatterns = "/*", asyncSupported = true)
@Component
public class ResponseToPrettyJsonFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
			throws IOException, ServletException {
		String pretty = request.getParameter("pretty");
		// 判断是否带了pretty参数，如果没有带就直接处理并返回
		if (!(pretty != null && ("".equals(pretty.trim()) || "true".equalsIgnoreCase(pretty)))) {
			filterChain.doFilter(request, response);
			return;
		}
		ResponseToPrettyJsonWrapper wrapperResponse = new ResponseToPrettyJsonWrapper((HttpServletResponse) response);// 转换成代理类
		// 这里只拦截返回，直接让请求过去，如果在请求前有处理，可以在这里处理
		filterChain.doFilter(request, wrapperResponse);
		byte[] content = wrapperResponse.getContent();// 获取返回值
		// 判断是否有值
		if (content.length > 0) {
			content = prettyJson(content, response);
			// 把返回值输出到客户端
			ServletOutputStream out = response.getOutputStream();
			out.write(content);
			out.flush();
		}

	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {

	}

	@Override
	public void destroy() {

	}

	/**
	 * 对带了pretty参数的请求，如果返回的json，则格式为pretty格式展示
	 * 
	 * @param origContent
	 * @param pretty
	 * @param response
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private byte[] prettyJson(byte[] origContent, ServletResponse response) throws UnsupportedEncodingException {
		String origStr = new String(origContent);
		if (origStr.startsWith("{") || origStr.startsWith("[")) {// Json串
			Object object = JSON.parse(origStr);
			byte[] result = JSON.toJSONString(object, SerializerFeature.WriteMapNullValue,
					SerializerFeature.WriteNullListAsEmpty, SerializerFeature.WriteNullNumberAsZero,
					SerializerFeature.WriteNullStringAsEmpty, SerializerFeature.PrettyFormat).getBytes();
			// 因为输出的内容变化了，因而必须重置输出的长度，要不然输出的长度会被截断
			response.setContentLength(result.length);
			// 同时修改contentType，要不然在页面上也不会展示为格式化后的json
			response.setContentType("application/json; charset=utf-8");
			return result;
		}

		return origContent;
	}

}
