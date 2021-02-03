/*
 * Copyright All Rights Reserved.
 * @author ip-limiter
 * @date  2020-07-23 10:47
 */
package com.eeefff.limiter.test.gateway.vo;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 统一请求返回格式.
 *
 * @author ip-limiter
 * @date 2020-07-23 10:47
 */
public class ApiResponses<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 表示接口调用成功
     */
    public static final int SUCCESS = 1;
    /**
     * 表示接口调用失败
     */
    public static final int FAIL = -1;
    /**
     * 表示没有权限调用该接口
     */
    public static final int NO_PERMISSION = -2;
    /**
     * 表示未登录或者登录过期
     */
    public static final int NO_LOGIN = -3;
    /**
     * 含义:表示token错误导致解析失败<br>
     */
    public static final int TOKEN_ERROR = -4;

    public static final String NO_LOGIN_MSG = "未登录";
    public static final String NO_PERMISSION_MSG = "没有权限";
    public static final String SUCC_MSG = "成功";
    public static final String FAIL_MSG = "失败";

    private String msg = SUCC_MSG;

    /**
     * 状态码 1:成功, -1:失败, -2:没有权限, -3:未登录或者登录过期, -4:token错误
     */
    private int result = SUCCESS;
    /**
     * 结果集返回
     */
    private T data;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    /**
     * 返回成功
     */
    public static ApiResponses<Void> success() {
        ApiResponses responses = new ApiResponses<>();
        responses.setMsg(SUCC_MSG);
        responses.setResult(SUCCESS);
        return responses;

    }

    /**
     * 成功返回
     *
     * @param object
     */
    public static <T> ApiResponses<T> success(T object) {
        ApiResponses responses = new ApiResponses<>();
        responses.setMsg(SUCC_MSG);
        responses.setResult(SUCCESS);
        responses.setData(object);
        return responses;
    }

    /**
     * 返回失败
     */
    public static ApiResponses<Void> failure() {
        ApiResponses responses = new ApiResponses<>();
        responses.setMsg(FAIL_MSG);
        responses.setResult(FAIL);
        return responses;
    }

    /**
     * 返回失败
     */
    public static ApiResponses<Void> failure(int result, String msg) {
        ApiResponses responses = new ApiResponses<>();
        responses.setMsg(msg);
        responses.setResult(result);
        return responses;

    }

    @JsonIgnore
    public boolean isSuccess() {
        return result == SUCCESS;
    }

    @JsonIgnore
    public boolean isFailed() {
        return !isSuccess();
    }

}
