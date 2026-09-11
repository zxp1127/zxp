package com.redculture.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC 通用查询/更新模板（服务端 DAO 层共用）
 *
 * 把"开连接、设参数、遍历结果集、关资源"这套样板流程收敛到一处，
 * 各 DAO 只声明 SQL 与行映射函数即可，避免每个方法重复 try-with-resources。
 */
public final class JdbcHelper {

    private JdbcHelper() {
    }

    /** 结果集行 -> 实体对象 的映射函数 */
    @FunctionalInterface
    public interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    /** 查询多行：逐行映射为实体列表 */
    public static <T> List<T> query(String sql, RowMapper<T> mapper, Object... params) throws SQLException {
        List<T> rows = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(mapper.map(rs));
                }
            }
        }
        return rows;
    }

    /** 查询单行：取第一条记录，无记录返回 null */
    public static <T> T queryOne(String sql, RowMapper<T> mapper, Object... params) throws SQLException {
        List<T> rows = query(sql, mapper, params);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /** 增/删/改：返回是否至少影响一行 */
    public static boolean execute(String sql, Object... params) throws SQLException {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate() > 0;
        }
    }

    /** 按占位符顺序绑定参数 */
    private static void bind(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }
}
