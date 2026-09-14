package com.redculture.server;

import com.redculture.handler.Dispatcher;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * V3 服务端启动入口
 *
 * 启动流程：
 *   1) 监听端口（默认 8088，可通过 -Dport 覆盖）；
 *   2) 使用固定大小线程池处理多个客户端并发连接；
 *   3) 收到 Ctrl+C 时优雅关闭线程池。
 *
 * 编译运行：
 *   java -jar redculture-v3-server.jar
 *   java -Dport=9000 -jar redculture-v3-server.jar
 */
public class ServerMain {

    private static final Logger LOG = Logger.getLogger("ServerMain");

    public static void main(String[] args) {
        int port = Integer.getInteger("port", 8088);
        int poolSize = Integer.getInteger("poolSize", 20);
        Dispatcher dispatcher = new Dispatcher();
        ExecutorService pool = Executors.newFixedThreadPool(poolSize);

        // 启动 HTTP 接口适配器（供 Postman/Apifox 接口测试，默认 8089，-DhttpPort 覆盖）
        HttpApiServer.start(dispatcher, Integer.getInteger("httpPort", 8089));

        // try-with-resources：退出时自动关闭监听套接字
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            LOG.info("==================================================");
            LOG.info("红色文化打卡系统 V3 服务端启动成功");
            LOG.info("监听端口：" + port);
            LOG.info("线程池大小：" + poolSize);
            LOG.info("等待客户端连接...");
            LOG.info("==================================================");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LOG.info("服务端关闭中...");
                pool.shutdown();
                try {
                    if (!pool.awaitTermination(3, TimeUnit.SECONDS)) {
                        pool.shutdownNow();
                    }
                } catch (InterruptedException ignored) {
                }
            }));

            // 接收循环
            while (!Thread.currentThread().isInterrupted()) {
                Socket socket = serverSocket.accept();
                pool.execute(new ClientHandler(socket, dispatcher));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "服务端异常退出：" + e.getMessage(), e);
        }
    }
}
