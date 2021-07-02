package com.eeeffff.limiter.common.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSON;
import com.eeeffff.limiter.common.action.HttpAction;
import com.eeeffff.limiter.common.constant.Constants;
import com.eeeffff.limiter.common.vo.Result;

import lombok.extern.slf4j.Slf4j;

/**
 * Http异步调用Client工具类
 * 
 * @author fenglibin
 *
 */
@Slf4j
public class HttpClientUtil {
	private static CloseableHttpClient httpclient;

	public static void init(int connectTimeout, int soTimeout, int maxConnTotal, int maxConnPerRoute) {
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(5000).setConnectionRequestTimeout(1000)
				.setSocketTimeout(5000).build();
		httpclient = HttpClients.custom().setRedirectStrategy(new DefaultRedirectStrategy() {
			@Override
			protected boolean isRedirectable(final String method) {
				return false;
			}
		}).setMaxConnTotal(maxConnTotal).setMaxConnPerRoute(maxConnPerRoute).setDefaultRequestConfig(requestConfig)
				.build();
	}

	/**
	 * 执行Get请求，响应的内容一定要是com.eeeffff.limiter.common.vo.Result格式的。
	 * 
	 * @param url
	 * @return
	 */
	public static void doGet(String url, HttpAction action) {
		// log.info("Http请求的URL：" + url);
		final HttpGet httpGet = new HttpGet(url);
		httpGet.setHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_CLOSE);
		try {
			CloseableHttpResponse response = httpclient.execute(httpGet);
			if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
				String responseData = getResponseData(response);
				Result<?> responseResult = null;
				if (StringUtils.isNotEmpty(responseData)) {
					responseResult = JSON.parseObject(responseData, Result.class);
				}
				action.doAction(responseResult);
			}else {
				httpGet.abort();
			}
		} catch (Exception e) {
			httpGet.abort();
			if (url.endsWith("/health/check")) {
				log.warn("对url:" + url + "执行健康检查发生异常，该节点当前不可用。");
			} else {
				log.error("执行Get请求发生异常:" + e.getMessage() + ", url:" + url, e);
			}
		}
	}

	/**
	 * 执行Post请求，响应的内容一定要是com.eeeffff.limiter.common.vo.Result格式的。
	 * 
	 * @param data
	 * @param url
	 * @throws UnsupportedEncodingException
	 */
	public static void doPost(String data, String url, HttpAction action) {
		final HttpPost httpPost = new HttpPost(url);
		httpPost.setHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_CLOSE);
		// 声明存放参数的List集合
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("metric", data));

		// 创建form表单对象
		UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(params, Constants.HTTP.DEFAULT_CHARSET);

		// 把表单对象设置到httpPost中
		httpPost.setEntity(formEntity);

		try {
			CloseableHttpResponse response = httpclient.execute(httpPost);
			if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
				String responseData = getResponseData(response);
				Result<?> responseResult = null;
				if (StringUtils.isNotEmpty(responseData)) {
					responseResult = JSON.parseObject(responseData, Result.class);
				}
				action.doAction(responseResult);
			}else {
				httpPost.abort();
			}
		} catch (Exception e) {
			httpPost.abort();
			log.error("执行Post请求发生异常:" + e.getMessage() + ", url:" + url, e);
		}
	}

	/**
	 * 从响应中获取内容
	 * 
	 * @param response
	 * @return
	 * @throws Exception
	 */
	private static String getResponseData(final HttpResponse response) throws Exception {
		Charset charset = null;
		try {
			String contentTypeStr = response.getFirstHeader("Content-Type").getValue();
			if (StringUtils.isNotEmpty(contentTypeStr)) {
				ContentType contentType = ContentType.parse(contentTypeStr);
				charset = contentType.getCharset();
			}
		} catch (Exception ignore) {
		}
		String result = EntityUtils.toString(response.getEntity(),
				charset != null ? charset : Constants.HTTP.DEFAULT_CHARSET);
		// log.info("Http请求响应内容：" + result);
		return result;
	}
}
