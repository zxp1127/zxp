package com.redculture.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * MySQL 数据库工具类（V3 服务端）
 *
 * 配置文件位置：classpath:db.properties
 * 主要字段：
 *   jdbc.url
 *   jdbc.username
 *   jdbc.password
 *   jdbc.driver
 */
public class DBUtil {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/redculture_v3?useSSL=false&useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true";

    private static String url;
    private static String username;
    private static String password;

    static {
        Properties props = loadProps();
        url = props.getProperty("jdbc.url", DEFAULT_URL);
        username = props.getProperty("jdbc.username", "root");
        password = props.getProperty("jdbc.password", "123456");
        String driver = props.getProperty("jdbc.driver", "com.mysql.cj.jdbc.Driver");
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL 驱动加载失败：" + driver, e);
        }
    }

    /** 加载 classpath 下 db.properties；读不到或失败时返回空配置，走默认值兜底 */
    private static Properties loadProps() {
        Properties props = new Properties();
        try (InputStream in = DBUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException ignored) {
            // 静默处理，使用兜底默认值
        }
        return props;
    }

    /** 获取连接 */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    /** 按 rs -> stmt -> conn 顺序静默关闭资源 */
    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        for (AutoCloseable resource : new AutoCloseable[]{rs, stmt, conn}) {
            if (resource == null) {
                continue;
            }
            try {
                resource.close();
            } catch (Exception ignored) {
            }
        }
    }

    public static void close(Connection conn) {
        close(conn, null, null);
    }

    public static String getUrl() {
        return url;
    }

    public static String getUsername() {
        return username;
    }
}
