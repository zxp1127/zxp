package com.redculture.protocol;

import java.io.Serializable;

/**
 * 响应对象（V3 通信协议）
 * 服务端 -> 客户端
 * success 表示业务是否成功，message 为提示信息，data 为返回的数据
 */
public class Response implements Serializable {
    private static final long serialVersionUID = 20260907L;

    private boolean success;
    private String message;
    private Object data;

    public Response() {
    }

    public Response(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public Response(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static Response ok(String msg) {
        return new Response(true, msg, null);
    }

    public static Response ok(String msg, Object data) {
        return new Response(true, msg, data);
    }

    public static Response fail(String msg) {
        return new Response(false, msg, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return new StringBuilder("Response{success=").append(success)
                .append(", message='").append(message).append("', data=").append(data)
                .append('}').toString();
    }
}
