package com.redculture.service;

import com.redculture.dao.AnnouncementDAO;
import com.redculture.pojo.Announcement;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 公告业务逻辑层
 *
 * 业务规则：
 *   1) 新增/修改时标题不能为空；
 *   2) 查询支持按标题或编号关键字过滤。
 */
public class AnnouncementService {

    private final AnnouncementDAO announcementDAO = new AnnouncementDAO();

    public List<Announcement> list(String keyword) throws SQLException {
        return announcementDAO.list(keyword);
    }

    public Announcement get(int id) throws SQLException {
        return Optional.ofNullable(announcementDAO.findById(id))
                .orElseThrow(() -> new RuntimeException("公告不存在"));
    }

    public void add(Announcement a) throws SQLException {
        requireTitle(a);
        a.setContent(Objects.toString(a.getContent(), ""));
        if (!announcementDAO.insert(a)) {
            throw new RuntimeException("新增公告失败");
        }
    }

    public void update(Announcement a) throws SQLException {
        requireTitle(a);
        get(a.getId());
        if (!announcementDAO.update(a)) {
            throw new RuntimeException("修改公告失败");
        }
    }

    public void delete(int id) throws SQLException {
        get(id);
        if (!announcementDAO.delete(id)) {
            throw new RuntimeException("删除公告失败");
        }
    }

    /** 标题非空校验（新增/修改共用） */
    private static void requireTitle(Announcement a) {
        if (a == null || a.getTitle() == null || a.getTitle().trim().isEmpty()) {
            throw new RuntimeException("公告标题不能为空");
        }
    }
}
