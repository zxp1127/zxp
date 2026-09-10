package com.redculture.protocol;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 请求对象（V3 通信协议）
 * 客户端 -> 服务端
 * action 取值参见 {@link ActionType}
 * params 为业务参数，常用 key：username / password / id / keyword 等
 */
public class Request implements Serializable {
    private static final long serialVersionUID = 20260907L;

    private String action;
    /** LinkedHashMap 保持参数插入顺序，日志输出稳定可读 */
    private Map<String, Object> params = new LinkedHashMap<>();

    public Request() {
    }

    public Request(String action) {
        this.action = action;
    }

    public Request param(String key, Object value) {
        this.params.put(key, value);
        return this;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public Object get(String key) {
        return params.get(key);
    }

    public String getString(String key) {
        Object v = params.get(key);
        return v == null ? null : v.toString();
    }

    public int getInt(String key) {
        Object v = params.get(key);
        return v == null ? 0
                : v instanceof Number ? ((Number) v).intValue()
                : Integer.parseInt(v.toString());
    }

    public boolean getBool(String key) {
        Object v = params.get(key);
        return v == null ? false
                : v instanceof Boolean ? (Boolean) v
                : Boolean.parseBoolean(v.toString());
    }

    @Override
    public String toString() {
        return new StringBuilder("Request{action='")
                .append(action).append("', params=").append(params)
                .append('}').toString();
    }
}
