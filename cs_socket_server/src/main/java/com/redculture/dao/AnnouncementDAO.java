package com.redculture.dao;

import com.redculture.db.JdbcHelper;
import com.redculture.pojo.Announcement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 公告数据访问对象
 */
public class AnnouncementDAO {

    private static final String COLUMNS = "id, title, content, publisher, create_time";

    /** 查询全部，可按标题/编号关键字过滤 */
    public List<Announcement> list(String keyword) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT ").append(COLUMNS)
                .append(" FROM announcements WHERE 1 = 1");
        List<Object> args = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (title LIKE ? OR CAST(id AS CHAR) LIKE ?)");
            String k = "%" + keyword.trim() + "%";
            args.add(k);
            args.add(k);
        }
        sql.append(" ORDER BY create_time DESC, id DESC");
        return JdbcHelper.query(sql.toString(), AnnouncementDAO::mapRow, args.toArray());
    }

    /** 按 id 查询 */
    public Announcement findById(int id) throws SQLException {
        return JdbcHelper.queryOne("SELECT " + COLUMNS + " FROM announcements WHERE id = ?",
                AnnouncementDAO::mapRow, id);
    }

    /** 新增 */
    public boolean insert(Announcement a) throws SQLException {
        return JdbcHelper.execute(
                "INSERT INTO announcements (title, content, publisher) VALUES (?,?,?)",
                a.getTitle(), a.getContent(), a.getPublisher());
    }

    /** 修改 */
    public boolean update(Announcement a) throws SQLException {
        return JdbcHelper.execute(
                "UPDATE announcements SET title = ?, content = ?, publisher = ? WHERE id = ?",
                a.getTitle(), a.getContent(), a.getPublisher(), a.getId());
    }

    /** 删除 */
    public boolean delete(int id) throws SQLException {
        return JdbcHelper.execute("DELETE FROM announcements WHERE id = ?", id);
    }

    /** 结果集行 -> Announcement（静态方法，供方法引用使用） */
    private static Announcement mapRow(ResultSet rs) throws SQLException {
        Announcement a = new Announcement();
        a.setId(rs.getInt("id"));
        a.setTitle(rs.getString("title"));
        a.setContent(rs.getString("content"));
        a.setPublisher(rs.getString("publisher"));
        a.setCreateTime(rs.getTimestamp("create_time"));
        return a;
    }
}
