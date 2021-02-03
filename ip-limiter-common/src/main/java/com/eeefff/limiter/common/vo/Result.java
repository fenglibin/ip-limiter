package com.eeefff.limiter.common.vo;

import java.io.Serializable;

/**
 * 
 * @author fenglibin
 *
 * @param <R>
 */
public class Result<R> implements Serializable {

	private static final long serialVersionUID = 1L;
	private boolean success;
	private int code;
	private String msg;
	private R data;

	public static <R> Result<R> empty() {
		Result<R> result = new Result<>();
		return result;
	}

	public static <R> Result<R> ofSuccess(R data) {
		return new Result<R>().setSuccess(true).setMsg("success").setData(data);
	}

	public static <R> Result<R> ofSuccessMsg(String msg) {
		return new Result<R>().setSuccess(true).setMsg(msg);
	}

	public static <R> Result<R> ofFail(String msg) {
		Result<R> result = new Result<>();
		result.setSuccess(false);
		result.setCode(-1);
		result.setMsg(msg);
		return result;
	}

	public static <R> Result<R> ofFail(int code, String msg) {
		Result<R> result = new Result<>();
		result.setSuccess(false);
		result.setCode(code);
		result.setMsg(msg);
		return result;
	}

	public static <R> Result<R> ofThrowable(int code, Throwable throwable) {
		Result<R> result = new Result<>();
		result.setSuccess(false);
		result.setCode(code);
		result.setMsg(throwable.getClass().getName() + ", " + throwable.getMessage());
		return result;
	}

	public boolean isSuccess() {
		return success;
	}

	public Result<R> setSuccess(boolean success) {
		this.success = success;
		return this;
	}

	public int getCode() {
		return code;
	}

	public Result<R> setCode(int code) {
		this.code = code;
		return this;
	}

	public String getMsg() {
		return msg;
	}

	public Result<R> setMsg(String msg) {
		this.msg = msg;
		return this;
	}

	public R getData() {
		return data;
	}

	public Result<R> setData(R data) {
		this.data = data;
		return this;
	}

	@Override
	public String toString() {
		return "Result{" + "success=" + success + ", code=" + code + ", msg='" + msg + '\'' + ", data=" + data + '}';
	}
}
