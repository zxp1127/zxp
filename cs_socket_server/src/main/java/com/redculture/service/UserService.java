package com.redculture.service;

import com.redculture.dao.UserDAO;
import com.redculture.pojo.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * 用户业务逻辑层
 *
 * 业务规则：
 *   1) 注册前校验用户名是否已存在；
 *   2) 普通用户默认积分 0、角色 user；
 *   3) 修改用户名前再次校验重名；
 *   4) 管理员账号由 SQL 脚本硬编码（admin / admin123），不在业务层创建。
 */
public class UserService {

    private final UserDAO userDAO = new UserDAO();

    /** 登录 */
    public User login(String username, String password) throws SQLException {
        User u = Optional.ofNullable(userDAO.findByUsername(username))
                .orElseThrow(() -> new RuntimeException("用户名不存在"));
        String stored = u.getPassword();
        if (!stored.equals(password)) {
            throw new RuntimeException("密码错误");
        }
        return u;
    }

    /** 注册 */
    public User register(String username, String password) throws SQLException {
        requireNotBlank(username, "用户名不能为空");
        requireNotBlank(password, "密码不能为空");
        if (userDAO.findByUsername(username) != null) {
            throw new RuntimeException("用户名已存在");
        }
        User u = new User();
        u.setUsername(username.trim());
        u.setPassword(password);
        u.setRole("user");
        u.setPoints(0);
        if (!userDAO.insert(u)) {
            throw new RuntimeException("注册失败");
        }
        // 回查一次，拿 id 等完整字段
        return userDAO.findByUsername(u.getUsername());
    }

    /** 修改密码 */
    public void updatePassword(int userId, String newPassword) throws SQLException {
        requireNotBlank(newPassword, "密码不能为空");
        if (!userDAO.updatePassword(userId, newPassword)) {
            throw new RuntimeException("修改密码失败");
        }
    }

    /** 修改用户名 */
    public User updateUsername(int userId, String newUsername) throws SQLException {
        requireNotBlank(newUsername, "用户名不能为空");
        User exist = userDAO.findByUsername(newUsername.trim());
        if (exist != null && exist.getId() != userId) {
            throw new RuntimeException("用户名已被占用");
        }
        if (!userDAO.updateUsername(userId, newUsername.trim())) {
            throw new RuntimeException("修改用户名失败");
        }
        return userDAO.findById(userId);
    }

    /** 全部用户 */
    public List<User> listAll() throws SQLException {
        return userDAO.listAll();
    }

    /** 按 id 查询 */
    public User getById(int id) throws SQLException {
        return Optional.ofNullable(userDAO.findById(id))
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    /** 给用户加积分（增删打卡时调用） */
    public void addPoints(int userId, int delta) throws SQLException {
        userDAO.addPoints(userId, delta);
    }

    /** 字符串非空校验：为 null 或全是空白时抛出业务异常 */
    private static void requireNotBlank(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException(message);
        }
    }
}
