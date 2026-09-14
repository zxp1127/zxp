package com.redculture.server;

import com.redculture.handler.Dispatcher;
import com.redculture.protocol.Request;
import com.redculture.protocol.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 客户端连接处理线程
 *
 * 协议约定：
 *   1) 长连接，循环读取 Request；
 *   2) 收到 null / EOF 表示客户端关闭，结束循环；
 *   3) 每次写完 Response 后调用 reset 防止 ObjectOutputStream 缓存膨胀。
 */
public class ClientHandler implements Runnable {

    private static final Logger LOG = Logger.getLogger("ClientHandler");

    private final Socket socket;
    private final Dispatcher dispatcher;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public ClientHandler(Socket socket, Dispatcher dispatcher) {
        this.socket = socket;
        this.dispatcher = dispatcher;
    }

    @Override
    public void run() {
        String clientInfo = socket.getRemoteSocketAddress().toString();
        LOG.info("客户端接入：" + clientInfo);
        try {
            // 先建输出流并 flush 头，避免对端 ObjectInputStream 阻塞
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            // for 循环的条件部直接读取请求：读到 null 即客户端断开，循环结束
            for (Request req = readRequest(); req != null; req = readRequest()) {
                LOG.info("[" + clientInfo + "] " + req);
                Response resp = dispatcher.dispatch(req);
                out.writeObject(resp);
                // 清空对象缓存，避免长连接下内存堆积
                out.reset();
                out.flush();
            }
        } catch (IOException e) {
            LOG.log(Level.WARNING, "与客户端通信异常：" + e.getMessage(), e);
        } finally {
            closeAll(in, out, socket);
        }
    }

    /**
     * 读取一次请求：
     *   返回 null 表示客户端断开（EOF / IO 异常）；
     *   反序列化失败（类版本不一致）时记录日志并继续等待下一条。
     */
    private Request readRequest() {
        for (;;) {
            try {
                return (Request) in.readObject();
            } catch (ClassNotFoundException cnf) {
                LOG.warning("反序列化失败：" + cnf.getMessage());
            } catch (IOException io) {
                // 客户端断开
                return null;
            }
        }
    }

    /** 依次静默关闭多个资源（可变参数 + 增强 for） */
    private static void closeAll(AutoCloseable... resources) {
        for (AutoCloseable r : resources) {
            if (r == null) {
                continue;
            }
            try {
                r.close();
            } catch (Exception ignored) {
            }
        }
    }
}
