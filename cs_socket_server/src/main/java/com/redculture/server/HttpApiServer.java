package com.redculture.server;

import com.redculture.handler.Dispatcher;
import com.redculture.protocol.Request;
import com.redculture.protocol.Response;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * HTTP 接口适配器（仅供 Postman / Apifox / JMeter 接口测试使用）
 *
 * 用法：
 *   POST http://localhost:8089/api
 *   Content-Type: application/json
 *   {"action":"user.login","params":{"username":"admin","password":"123456"}}
 *
 * 说明：把 HTTP+JSON 请求转成内部 Request 交给 Dispatcher 分发，
 * 再把 Response 序列化为 JSON 返回；现有 Socket 协议与客户端完全不受影响。
 * 端口默认 8089，可用 -DhttpPort 覆盖。
 */
public final class HttpApiServer {

    private static final Logger LOG = Logger.getLogger("HttpApiServer");

    private HttpApiServer() {
    }

    /** 启动 HTTP 适配器；启动失败只告警，不影响 Socket 主服务 */
    public static void start(Dispatcher dispatcher, int port) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/api", ex -> handle(ex, dispatcher));
            server.setExecutor(Executors.newFixedThreadPool(10));
            server.start();
            LOG.info("HTTP 接口适配器已启动：http://localhost:" + port + "/api （供接口测试使用）");
        } catch (IOException e) {
            LOG.warning("HTTP 接口适配器启动失败（不影响 Socket 服务）：" + e.getMessage());
        }
    }

    private static void handle(HttpExchange ex, Dispatcher dispatcher) throws IOException {
        try {
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
                respond(ex, 405, JsonUtil.toJson(Response.fail("只支持 POST 请求")));
                return;
            }
            String body = new String(readAll(ex.getRequestBody()), StandardCharsets.UTF_8);
            Object parsed = JsonUtil.parse(body);
            if (!(parsed instanceof Map)) {
                respond(ex, 400, JsonUtil.toJson(Response.fail("请求体必须是 JSON 对象，形如 {\"action\":\"...\",\"params\":{...}}")));
                return;
            }
            Map<?, ?> json = (Map<?, ?>) parsed;

            // JSON -> Request
            Request req = new Request();
            Object action = json.get("action");
            req.setAction(action == null ? null : action.toString());
            Object params = json.get("params");
            if (params instanceof Map) {
                Map<String, Object> p = new LinkedHashMap<>();
                ((Map<?, ?>) params).forEach((k, v) -> p.put(String.valueOf(k), v));
                req.setParams(p);
            }

            // 走与 Socket 完全相同的分发与业务逻辑
            Response resp = dispatcher.dispatch(req);
            respond(ex, 200, JsonUtil.toJson(resp));
        } catch (Exception e) {
            respond(ex, 400, JsonUtil.toJson(Response.fail("请求解析失败：" + e.getMessage())));
        } finally {
            ex.close();
        }
    }

    private static void respond(HttpExchange ex, int code, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    /** 读取请求体全部字节（兼容 JDK 8，不用 InputStream.readAllBytes） */
    private static byte[] readAll(java.io.InputStream in) throws IOException {
        java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
        byte[] tmp = new byte[4096];
        int n;
        while ((n = in.read(tmp)) != -1) {
            buf.write(tmp, 0, n);
        }
        return buf.toByteArray();
    }
}
