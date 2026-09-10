package com.redculture.pojo;

import java.io.Serializable;
import java.util.Date;

/**
 * 景点实体类（V3 公共模型）
 */
public class ScenicSpot implements Serializable {
    private static final long serialVersionUID = 20260907L;

    private int id;
    private String name;
    private String description;
    private String location;
    private boolean popular;        // 是否热门
    private Date createTime;

    public ScenicSpot() {
    }

    public ScenicSpot(int id, String name, String description, String location, boolean popular) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.location = location;
        this.popular = popular;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isPopular() {
        return popular;
    }

    public void setPopular(boolean popular) {
        this.popular = popular;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "ScenicSpot{id=" + id + ", name='" + name + "', popular=" + popular + "}";
    }
}
