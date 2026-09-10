package com.redculture.pojo;

import java.io.Serializable;
import java.util.Date;

/**
 * 公告实体类（V3 公共模型）
 */
public class Announcement implements Serializable {
    private static final long serialVersionUID = 20260907L;

    private int id;
    private String title;
    private String content;
    private String publisher;
    private Date createTime;

    public Announcement() {
    }

    public Announcement(int id, String title, String content, String publisher) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.publisher = publisher;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "Announcement{id=" + id + ", title='" + title + "', publisher='" + publisher + "'}";
    }
}
