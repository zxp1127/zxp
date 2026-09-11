package com.redculture.dao;

import com.redculture.db.JdbcHelper;
import com.redculture.pojo.CheckinRecord;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 打卡记录数据访问对象
 *
 * 列表查询时关联 users / scenic_spots 表填充 username / spotName 冗余字段，
 * 便于客户端直接展示。
 */
public class CheckinDAO {

    /** 列表查询公共片段：关联用户表与景点表 */
    private static final String LIST_FROM =
            "SELECT r.id, r.user_id, r.spot_id, r.comment, r.checkin_time, " +
                    "u.username, s.name AS spot_name " +
                    "FROM checkin_records r " +
                    "LEFT JOIN users u ON u.id = r.user_id " +
                    "LEFT JOIN scenic_spots s ON s.id = r.spot_id ";

    /** 按 id 查询打卡记录（用于删除前定位所属用户，扣分用；不关联冗余字段） */
    public CheckinRecord findById(int id) throws SQLException {
        String sql = "SELECT id, user_id, spot_id, comment, checkin_time FROM checkin_records WHERE id = ?";
        return JdbcHelper.queryOne(sql, rs -> {
            CheckinRecord c = new CheckinRecord();
            c.setId(rs.getInt("id"));
            c.setUserId(rs.getInt("user_id"));
            c.setSpotId(rs.getInt("spot_id"));
            c.setComment(rs.getString("comment"));
            c.setCheckinTime(rs.getTimestamp("checkin_time"));
            return c;
        }, id);
    }

    /** 新增打卡 */
    public boolean insert(CheckinRecord c) throws SQLException {
        return JdbcHelper.execute(
                "INSERT INTO checkin_records (user_id, spot_id, comment) VALUES (?,?,?)",
                c.getUserId(), c.getSpotId(), c.getComment());
    }

    /** 修改打卡心得 */
    public boolean updateComment(int id, String comment) throws SQLException {
        return JdbcHelper.execute("UPDATE checkin_records SET comment = ? WHERE id = ?", comment, id);
    }

    /** 删除打卡 */
    public boolean delete(int id) throws SQLException {
        return JdbcHelper.execute("DELETE FROM checkin_records WHERE id = ?", id);
    }

    /** 按用户查询打卡记录 */
    public List<CheckinRecord> listByUser(int userId, String keyword, Integer spotId) throws SQLException {
        StringBuilder sql = new StringBuilder(LIST_FROM).append("WHERE r.user_id = ?");
        List<Object> args = new ArrayList<>();
        args.add(userId);
        if (spotId != null && spotId > 0) {
            sql.append(" AND r.spot_id = ?");
            args.add(spotId);
        }
        appendKeyword(sql, args, keyword, "(s.name LIKE ? OR r.comment LIKE ? OR CAST(r.id AS CHAR) LIKE ?)");
        sql.append(" ORDER BY r.checkin_time DESC, r.id DESC");
        return JdbcHelper.query(sql.toString(), CheckinDAO::mapRow, args.toArray());
    }

    /** 查询全部打卡（管理员） */
    public List<CheckinRecord> listAll(String keyword) throws SQLException {
        StringBuilder sql = new StringBuilder(LIST_FROM).append("WHERE 1 = 1");
        List<Object> args = new ArrayList<>();
        appendKeyword(sql, args, keyword, "(u.username LIKE ? OR s.name LIKE ? OR r.comment LIKE ?)");
        sql.append(" ORDER BY r.checkin_time DESC, r.id DESC");
        return JdbcHelper.query(sql.toString(), CheckinDAO::mapRow, args.toArray());
    }

    /** 热门景点排行榜：景点名 + 打卡次数 */
    public List<Object[]> rankPopularSpots() throws SQLException {
        String sql = "SELECT s.name AS spot_name, COUNT(r.id) AS cnt " +
                "FROM scenic_spots s " +
                "LEFT JOIN checkin_records r ON r.spot_id = s.id " +
                "GROUP BY s.id, s.name " +
                "ORDER BY cnt DESC, s.id ASC";
        return JdbcHelper.query(sql,
                rs -> new Object[]{rs.getString("spot_name"), rs.getInt("cnt")});
    }

    /** 关键字非空时拼接模糊匹配条件，并复制对应个数的 %keyword% 参数 */
    private static void appendKeyword(StringBuilder sql, List<Object> args, String keyword, String condition) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }
        sql.append(" AND ").append(condition);
        String k = "%" + keyword.trim() + "%";
        int placeholders = condition.length() - condition.replace("?", "").length();
        for (int i = 0; i < placeholders; i++) {
            args.add(k);
        }
    }

    /** 结果集行 -> CheckinRecord（静态方法，供方法引用使用） */
    private static CheckinRecord mapRow(ResultSet rs) throws SQLException {
        CheckinRecord c = new CheckinRecord();
        c.setId(rs.getInt("id"));
        c.setUserId(rs.getInt("user_id"));
        c.setSpotId(rs.getInt("spot_id"));
        c.setComment(rs.getString("comment"));
        c.setCheckinTime(rs.getTimestamp("checkin_time"));
        c.setUsername(rs.getString("username"));
        c.setSpotName(rs.getString("spot_name"));
        return c;
    }
}
