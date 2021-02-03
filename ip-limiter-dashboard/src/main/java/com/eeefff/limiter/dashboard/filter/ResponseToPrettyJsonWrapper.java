/*
 * TOP SECRET Copyright 2006-2015 Transsion.com All right reserved. This software is the confidential and proprietary
 * information of Transsion.com ("Confidential Information"). You shall not disclose such Confidential Information and
 * shall use it only in accordance with the terms of the license agreement you entered into with Transsion.com.
 */
package com.eeefff.limiter.dashboard.filter;

/**
 * ClassName:ResponseWrapper <br/>
 * Date: 2018年9月28日 下午3:06:25 <br/>
 * 
 * @author fenglibin1982@163.com
 * @Blog http://blog.csdn.net/fenglibing
 * @version
 * @see
 */
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * 返回值输出代理类
 * 
 * @Title: ResponseWrapper
 * @Description:
 * @author kokJuis
 * @date 上午9:52:11
 */
public class ResponseToPrettyJsonWrapper extends HttpServletResponseWrapper {

	private ByteArrayOutputStream buffer;
	private ServletOutputStream out;
	private PrintWriter writer = null;

	public ResponseToPrettyJsonWrapper(HttpServletResponse httpServletResponse) throws UnsupportedEncodingException {
		super(httpServletResponse);
		buffer = new ByteArrayOutputStream();
		out = new WrapperOutputStream(buffer);
		writer = new PrintWriter(new OutputStreamWriter(buffer, this.getCharacterEncoding()));
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return out;
	}

	@Override
	public void flushBuffer() throws IOException {
		if (out != null) {
			out.flush();
		}
		if (writer != null) {
			writer.flush();
		}
	}

	/** 重载父类获取writer的方法 */
	@Override
	public PrintWriter getWriter() throws UnsupportedEncodingException {
		return writer;
	}

	public byte[] getContent() throws IOException {
		flushBuffer();
		return buffer.toByteArray();
	}

	@Override
	public void reset() {
		buffer.reset();
	}

	class WrapperOutputStream extends ServletOutputStream {

		private ByteArrayOutputStream bos;

		public WrapperOutputStream(ByteArrayOutputStream bos) {
			this.bos = bos;
		}

		@Override
		public void write(int b) throws IOException {
			bos.write(b);
		}

		@Override
		public void write(byte[] b) throws IOException {
			bos.write(b, 0, b.length);
		}

		@Override
		public boolean isReady() {

			// TODO Auto-generated method stub
			return false;

		}

		@Override
		public void setWriteListener(WriteListener arg0) {

			// TODO Auto-generated method stub

		}
	}

}
