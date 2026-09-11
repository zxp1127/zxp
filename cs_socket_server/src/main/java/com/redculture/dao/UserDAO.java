package com.redculture.dao;

import com.redculture.db.JdbcHelper;
import com.redculture.pojo.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 用户数据访问对象
 * 字段：id / username / password / role / points / create_time
 */
public class UserDAO {

    private static final String COLUMNS = "id, username, password, role, points, create_time";

    /** 按用户名查询（用于登录、查重） */
    public User findByUsername(String username) throws SQLException {
        return JdbcHelper.queryOne("SELECT " + COLUMNS + " FROM users WHERE username = ?",
                UserDAO::mapRow, username);
    }

    /** 按 id 查询 */
    public User findById(int id) throws SQLException {
        return JdbcHelper.queryOne("SELECT " + COLUMNS + " FROM users WHERE id = ?",
                UserDAO::mapRow, id);
    }

    /** 查询全部用户 */
    public List<User> listAll() throws SQLException {
        return JdbcHelper.query("SELECT " + COLUMNS + " FROM users ORDER BY id", UserDAO::mapRow);
    }

    /** 新增用户 */
    public boolean insert(User u) throws SQLException {
        String role = u.getRole() == null ? "user" : u.getRole();
        return JdbcHelper.execute(
                "INSERT INTO users (username, password, role, points) VALUES (?,?,?,?)",
                u.getUsername(), u.getPassword(), role, u.getPoints());
    }

    /** 修改密码 */
    public boolean updatePassword(int id, String newPassword) throws SQLException {
        return JdbcHelper.execute("UPDATE users SET password = ? WHERE id = ?", newPassword, id);
    }

    /** 修改用户名 */
    public boolean updateUsername(int id, String newUsername) throws SQLException {
        return JdbcHelper.execute("UPDATE users SET username = ? WHERE id = ?", newUsername, id);
    }

    /** 修改积分（增量，可为负数） */
    public boolean addPoints(int id, int delta) throws SQLException {
        return JdbcHelper.execute("UPDATE users SET points = GREATEST(points + ?, 0) WHERE id = ?", delta, id);
    }

    /** 结果集行 -> User（静态方法，供方法引用使用） */
    private static User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setRole(rs.getString("role"));
        u.setPoints(rs.getInt("points"));
        u.setCreateTime(rs.getTimestamp("create_time"));
        return u;
    }
}
