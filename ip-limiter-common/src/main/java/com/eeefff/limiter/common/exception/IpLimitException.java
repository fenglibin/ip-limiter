package com.eeefff.limiter.common.exception;

public class IpLimitException extends RuntimeException {

	private static final long serialVersionUID = 4184041466207765768L;

	public IpLimitException() {
		super();
	}

	public IpLimitException(String message) {
		super(message);
	}

	public IpLimitException(String message, Throwable cause) {
		super(message, cause);
	}

	public IpLimitException(Throwable cause) {
		super(cause);
	}

	public IpLimitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
