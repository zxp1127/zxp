package com.redculture.dao;

import com.redculture.db.JdbcHelper;
import com.redculture.pojo.ScenicSpot;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 景点数据访问对象
 */
public class ScenicSpotDAO {

    private static final String COLUMNS = "id, name, description, location, is_popular, create_time";

    /** 查询全部，可按名称/编号关键字过滤 */
    public List<ScenicSpot> list(String keyword) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT ").append(COLUMNS)
                .append(" FROM scenic_spots WHERE 1 = 1");
        List<Object> args = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (name LIKE ? OR CAST(id AS CHAR) LIKE ?)");
            String k = "%" + keyword.trim() + "%";
            args.add(k);
            args.add(k);
        }
        sql.append(" ORDER BY is_popular DESC, id ASC");
        return JdbcHelper.query(sql.toString(), ScenicSpotDAO::mapRow, args.toArray());
    }

    /** 按 id 查询 */
    public ScenicSpot findById(int id) throws SQLException {
        return JdbcHelper.queryOne("SELECT " + COLUMNS + " FROM scenic_spots WHERE id = ?",
                ScenicSpotDAO::mapRow, id);
    }

    /** 新增 */
    public boolean insert(ScenicSpot s) throws SQLException {
        return JdbcHelper.execute(
                "INSERT INTO scenic_spots (name, description, location, is_popular) VALUES (?,?,?,?)",
                s.getName(), s.getDescription(), s.getLocation(), s.isPopular() ? 1 : 0);
    }

    /** 修改 */
    public boolean update(ScenicSpot s) throws SQLException {
        return JdbcHelper.execute(
                "UPDATE scenic_spots SET name = ?, description = ?, location = ?, is_popular = ? WHERE id = ?",
                s.getName(), s.getDescription(), s.getLocation(), s.isPopular() ? 1 : 0, s.getId());
    }

    /** 删除 */
    public boolean delete(int id) throws SQLException {
        return JdbcHelper.execute("DELETE FROM scenic_spots WHERE id = ?", id);
    }

    /** 结果集行 -> ScenicSpot（静态方法，供方法引用使用） */
    private static ScenicSpot mapRow(ResultSet rs) throws SQLException {
        ScenicSpot s = new ScenicSpot();
        s.setId(rs.getInt("id"));
        s.setName(rs.getString("name"));
        s.setDescription(rs.getString("description"));
        s.setLocation(rs.getString("location"));
        s.setPopular(rs.getInt("is_popular") == 1);
        s.setCreateTime(rs.getTimestamp("create_time"));
        return s;
    }
}
