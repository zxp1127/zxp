package com.redculture.service;

import com.redculture.dao.ScenicSpotDAO;
import com.redculture.pojo.ScenicSpot;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 景点业务逻辑层
 *
 * 业务规则：
 *   1) 新增/修改时景点名不能为空；
 *   2) 查询支持按名称或编号关键字过滤；
 *   3) 热门景点判定由 is_popular 字段控制（管理员维护），排行榜由 CheckinService 生成。
 */
public class ScenicSpotService {

    private final ScenicSpotDAO scenicSpotDAO = new ScenicSpotDAO();

    public List<ScenicSpot> list(String keyword) throws SQLException {
        return scenicSpotDAO.list(keyword);
    }

    public ScenicSpot get(int id) throws SQLException {
        return Optional.ofNullable(scenicSpotDAO.findById(id))
                .orElseThrow(() -> new RuntimeException("景点不存在"));
    }

    public void add(ScenicSpot s) throws SQLException {
        requireName(s);
        s.setDescription(Objects.toString(s.getDescription(), ""));
        s.setLocation(Objects.toString(s.getLocation(), ""));
        if (!scenicSpotDAO.insert(s)) {
            throw new RuntimeException("新增景点失败");
        }
    }

    public void update(ScenicSpot s) throws SQLException {
        requireName(s);
        get(s.getId());
        if (!scenicSpotDAO.update(s)) {
            throw new RuntimeException("修改景点失败");
        }
    }

    public void delete(int id) throws SQLException {
        get(id);
        if (!scenicSpotDAO.delete(id)) {
            throw new RuntimeException("删除景点失败");
        }
    }

    /** 景点名非空校验（新增/修改共用） */
    private static void requireName(ScenicSpot s) {
        if (s == null || s.getName() == null || s.getName().trim().isEmpty()) {
            throw new RuntimeException("景点名称不能为空");
        }
    }
}
