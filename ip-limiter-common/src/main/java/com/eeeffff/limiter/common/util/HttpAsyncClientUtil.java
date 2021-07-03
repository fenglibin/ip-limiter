package com.eeeffff.limiter.common.util;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
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
public class HttpAsyncClientUtil {
	private static CloseableHttpAsyncClient httpclient;

	public static void init(int connectTimeout, int soTimeout, int maxConnTotal, int maxConnPerRoute) {
		IOReactorConfig ioConfig = IOReactorConfig.custom().setConnectTimeout(connectTimeout).setSoTimeout(soTimeout)
				.setIoThreadCount(1).build();
		httpclient = HttpAsyncClients.custom().setRedirectStrategy(new DefaultRedirectStrategy() {
			@Override
			protected boolean isRedirectable(final String method) {
				return false;
			}
		}).setMaxConnTotal(maxConnTotal).setMaxConnPerRoute(maxConnPerRoute).setDefaultIOReactorConfig(ioConfig)
				.build();
		httpclient.start();
	}

	/**
	 * 执行Get请求，响应的内容一定要是com.eeeffff.limiter.common.vo.Result格式的。
	 * 
	 * @param url
	 * @param action
	 */
	public static void doGet(String url, HttpAction action) {
		// log.info("Http请求的URL：" + url);
		final HttpGet httpGet = new HttpGet(url);
		httpGet.setHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_CLOSE);
		httpclient.execute(httpGet, new FutureCallback<HttpResponse>() {
			@Override
			public void completed(final HttpResponse response) {
				try {
					String responseData = getResponseData(response);
					Result<?> responseResult = null;
					if (StringUtils.isNotEmpty(responseData)) {
						responseResult = JSON.parseObject(responseData, Result.class);
					}
					action.doAction(responseResult);
				} catch (Exception e) {
					log.error("执行Get请求发生异常:" + e.getMessage() + ", url:" + url, e);
				}
			}

			@Override
			public void failed(final Exception ex) {
				httpGet.abort();
				if (ex instanceof SocketTimeoutException) {
					log.error("Failed to do get request from <{}>: socket timeout", url);
				} else if (ex instanceof ConnectException) {
					log.error("Failed to do get request from <{}> (ConnectionException: {})", url, ex.getMessage());
				} else {
					log.error("Failed to do get request from " + url + " error", ex);
				}
			}

			@Override
			public void cancelled() {
				httpGet.abort();
			}
		});
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

	/**
	 * 执行Post请求，响应的内容一定要是com.eeeffff.limiter.common.vo.Result格式的。
	 * 
	 * @param data
	 * @param url
	 * @param action
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

		httpclient.execute(httpPost, new FutureCallback<HttpResponse>() {
			@Override
			public void completed(HttpResponse response) {
				try {
					String responseData = getResponseData(response);
					int code = response.getStatusLine().getStatusCode();
					if (code != Constants.HTTP.HTTP_OK) {
						log.warn("发送metric到控制台失败，响应码：" + code + "，响应内容:" + responseData);
					}

					Result<?> responseResult = null;
					if (StringUtils.isNotEmpty(responseData)) {
						responseResult = JSON.parseObject(responseData, Result.class);
					}
					action.doAction(responseResult);
				} catch (Exception e) {
					log.error("发送metric到控制台失败：" + e.getMessage(), e);
				}
			}

			@Override
			public void failed(Exception ex) {
				httpPost.abort();
			}

			@Override
			public void cancelled() {
				httpPost.abort();
			}
		});
	}
}
