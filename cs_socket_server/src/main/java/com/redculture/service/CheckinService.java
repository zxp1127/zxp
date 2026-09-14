package com.redculture.service;

import com.redculture.dao.CheckinDAO;
import com.redculture.dao.ScenicSpotDAO;
import com.redculture.pojo.CheckinRecord;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 打卡业务逻辑层
 *
 * 业务规则：
 *   1) 打卡前校验景点是否存在；
 *   2) 打卡成功积分 +1；
 *   3) 删除打卡记录积分 -1，积分下限 0（由 UserDAO.addPoints 中 GREATEST 兜底）；
 *   4) 修改打卡仅允许修改心得；
 *   5) 热门景点排行榜按打卡次数降序展示。
 */
public class CheckinService {

    private final CheckinDAO checkinDAO = new CheckinDAO();
    private final ScenicSpotDAO scenicSpotDAO = new ScenicSpotDAO();
    private final UserService userService = new UserService();

    /** 新增打卡 */
    public void addCheckin(int userId, int spotId, String comment) throws SQLException {
        Optional.ofNullable(scenicSpotDAO.findById(spotId))
                .orElseThrow(() -> new RuntimeException("景点不存在，无法打卡"));
        CheckinRecord c = new CheckinRecord();
        c.setUserId(userId);
        c.setSpotId(spotId);
        c.setComment(Objects.toString(comment, ""));
        if (!checkinDAO.insert(c)) {
            throw new RuntimeException("打卡失败");
        }
        // 打卡成功积分 +1
        userService.addPoints(userId, 1);
    }

    /** 修改心得 */
    public void updateComment(int id, String comment) throws SQLException {
        if (!checkinDAO.updateComment(id, Objects.toString(comment, ""))) {
            throw new RuntimeException("修改心得失败");
        }
    }

    /** 删除打卡（服务端自行根据记录 id 查询所属用户再扣分，避免客户端传错 userId） */
    public void deleteCheckin(int id) throws SQLException {
        CheckinRecord existing = Optional.ofNullable(checkinDAO.findById(id))
                .orElseThrow(() -> new RuntimeException("打卡记录不存在"));
        if (!checkinDAO.delete(id)) {
            throw new RuntimeException("删除打卡失败");
        }
        // 删除成功积分 -1，下限 0
        userService.addPoints(existing.getUserId(), -1);
    }

    /** 按用户查询打卡列表 */
    public List<CheckinRecord> listByUser(int userId, String keyword, Integer spotId) throws SQLException {
        return checkinDAO.listByUser(userId, keyword, spotId);
    }

    /** 查询全部打卡（管理员） */
    public List<CheckinRecord> listAll(String keyword) throws SQLException {
        return checkinDAO.listAll(keyword);
    }

    /** 热门景点排行榜（景点名 + 打卡次数） */
    public List<Object[]> rankPopularSpots() throws SQLException {
        return checkinDAO.rankPopularSpots();
    }
}
