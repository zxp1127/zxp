package com.redculture.pojo;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户实体类（V3 公共模型，服务端/客户端共享）
 * 字段与 users 表完全对齐
 */
public class User implements Serializable {
    private static final long serialVersionUID = 20260907L;

    private int id;
    private String username;
    private String password;
    private String role;          // admin / user
    private int points;           // 学习积分
    private Date createTime;

    public User() {
    }

    public User(int id, String username, String password, String role, int points) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.points = points;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /** 是否为管理员 */
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", points=" + points +
                '}';
    }
}
