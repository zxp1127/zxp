-- ============================================================
-- 红色文化打卡系统 V3 C/S 版本 MySQL 建表脚本
-- 数据库：redculture_v3
-- 字段与 V1/V2( SQLite )完全对齐，便于公共模型复用
-- ============================================================

DROP DATABASE IF EXISTS redculture_v3;
CREATE DATABASE redculture_v3 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE redculture_v3;

-- ----------------------------
-- 1. 用户表 users
-- ----------------------------
CREATE TABLE users (
    id          INT AUTO_INCREMENT PRIMARY KEY COMMENT '用户编号',
    username    VARCHAR(32)  NOT NULL UNIQUE COMMENT '用户名',
    password    VARCHAR(64)  NOT NULL COMMENT '密码',
    role        VARCHAR(16)  NOT NULL DEFAULT 'user' COMMENT '角色：admin / user',
    points      INT          NOT NULL DEFAULT 0 COMMENT '学习积分',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间'
) ENGINE = InnoDB COMMENT '用户表';

-- ----------------------------
-- 2. 公告表 announcements
-- ----------------------------
CREATE TABLE announcements (
    id          INT AUTO_INCREMENT PRIMARY KEY COMMENT '公告编号',
    title        VARCHAR(128) NOT NULL COMMENT '公告标题',
    content      TEXT         COMMENT '公告内容',
    publisher    VARCHAR(32)  COMMENT '发布人',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间'
) ENGINE = InnoDB COMMENT '公告表';

-- ----------------------------
-- 3. 景点表 scenic_spots
-- ----------------------------
CREATE TABLE scenic_spots (
    id          INT AUTO_INCREMENT PRIMARY KEY COMMENT '景点编号',
    name        VARCHAR(64)  NOT NULL COMMENT '景点名称',
    description TEXT         COMMENT '景点描述',
    location    VARCHAR(128) COMMENT '景点位置',
    is_popular  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否热门：0 否，1 是',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '录入时间'
) ENGINE = InnoDB COMMENT '景点表';

-- ----------------------------
-- 4. 打卡记录表 checkin_records
-- ----------------------------
CREATE TABLE checkin_records (
    id          INT AUTO_INCREMENT PRIMARY KEY COMMENT '打卡记录编号',
    user_id     INT          NOT NULL COMMENT '所属用户',
    spot_id     INT          NOT NULL COMMENT '景点编号',
    comment     TEXT         COMMENT '打卡心得',
    checkin_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '打卡时间',
    CONSTRAINT fk_checkin_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_checkin_spot FOREIGN KEY (spot_id) REFERENCES scenic_spots(id) ON DELETE CASCADE,
    INDEX idx_checkin_user (user_id),
    INDEX idx_checkin_spot (spot_id)
) ENGINE = InnoDB COMMENT '打卡记录表';

-- ----------------------------
-- 初始化数据
-- ----------------------------
INSERT INTO users (username, password, role, points) VALUES
    ('admin', 'admin123', 'admin', 999),
    ('stu01', '123456',   'user', 0),
    ('stu02', '123456',   'user', 0);

INSERT INTO announcements (title, content, publisher) VALUES
    ('关于开展红色文化打卡活动的通知',
     '为弘扬红色文化，开展线上线下打卡活动，完成打卡可获得学习积分。',
     'admin'),
    ('活动奖励规则',
     '每完成一次打卡积分 +1，删除打卡积分 -1，积分下限 0。',
     'admin');

INSERT INTO scenic_spots (name, description, location, is_popular) VALUES
    ('井冈山革命根据地', '中国革命的摇篮，井冈山精神发源地。', '江西省吉安市', 1),
    ('延安革命纪念馆',   '党中央在延安十三年的光辉历程。',     '陕西省延安市', 1),
    ('南湖红船',         '中共一大召开地，红船精神起源。',       '浙江省嘉兴市', 1),
    ('西柏坡纪念馆',     '党中央进入北平前的最后一个农村指挥所。', '河北省石家庄市', 0);

INSERT INTO checkin_records (user_id, spot_id, comment) VALUES
    (2, 1, '井冈山之行让我感受到革命先辈的艰辛与伟大。'),
    (2, 3, '红船精神永放光芒。');
