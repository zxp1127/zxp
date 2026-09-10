package com.redculture.pojo;

import java.io.Serializable;
import java.util.Date;

/**
 * 打卡记录实体类（V3 公共模型）
 */
public class CheckinRecord implements Serializable {
    private static final long serialVersionUID = 20260907L;

    private int id;
    private int userId;
    private int spotId;
    private String spotName;          // 冗余字段，便于客户端直接展示
    private String username;         // 冗余字段，便于客户端直接展示
    private String comment;
    private Date checkinTime;

    public CheckinRecord() {
    }

    public CheckinRecord(int id, int userId, int spotId, String comment) {
        this.id = id;
        this.userId = userId;
        this.spotId = spotId;
        this.comment = comment;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getSpotId() {
        return spotId;
    }

    public void setSpotId(int spotId) {
        this.spotId = spotId;
    }

    public String getSpotName() {
        return spotName;
    }

    public void setSpotName(String spotName) {
        this.spotName = spotName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getCheckinTime() {
        return checkinTime;
    }

    public void setCheckinTime(Date checkinTime) {
        this.checkinTime = checkinTime;
    }

    @Override
    public String toString() {
        return "CheckinRecord{id=" + id + ", userId=" + userId + ", spotId=" + spotId + "}";
    }
}
