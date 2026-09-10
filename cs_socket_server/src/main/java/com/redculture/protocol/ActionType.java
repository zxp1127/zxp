package com.redculture.protocol;

/**
 * 业务动作常量（V3 通信协议）
 * 客户端 Request.action 与服务端 Dispatcher 路由共用此常量
 *
 * 命名规则：模块_动作
 */
public final class ActionType {

    private ActionType() {
    }

    // ===== 用户模块 =====
    /** 登录，参数：username / password */
    public static final String USER_LOGIN = "USER_LOGIN";
    /** 注册，参数：username / password */
    public static final String USER_REGISTER = "USER_REGISTER";
    /** 修改密码，参数：userId / newPassword */
    public static final String USER_UPDATE_PASSWORD = "USER_UPDATE_PASSWORD";
    /** 修改用户名，参数：userId / newUsername */
    public static final String USER_UPDATE_USERNAME = "USER_UPDATE_USERNAME";
    /** 查询全部用户（管理员） */
    public static final String USER_LIST_ALL = "USER_LIST_ALL";
    /** 按用户 id 查询 */
    public static final String USER_GET_BY_ID = "USER_GET_BY_ID";

    // ===== 公告模块 =====
    /** 查询全部公告，可选参数：keyword */
    public static final String ANN_LIST = "ANN_LIST";
    /** 按 id 查询公告 */
    public static final String ANN_GET = "ANN_GET";
    /** 新增公告，参数：title / content / publisher */
    public static final String ANN_ADD = "ANN_ADD";
    /** 修改公告，参数：id / title / content / publisher */
    public static final String ANN_UPDATE = "ANN_UPDATE";
    /** 删除公告，参数：id */
    public static final String ANN_DELETE = "ANN_DELETE";

    // ===== 景点模块 =====
    /** 查询全部景点，可选参数：keyword */
    public static final String SPOT_LIST = "SPOT_LIST";
    /** 按 id 查询景点 */
    public static final String SPOT_GET = "SPOT_GET";
    /** 新增景点，参数：name / description / location / popular */
    public static final String SPOT_ADD = "SPOT_ADD";
    /** 修改景点，参数：id / name / description / location / popular */
    public static final String SPOT_UPDATE = "SPOT_UPDATE";
    /** 删除景点，参数：id */
    public static final String SPOT_DELETE = "SPOT_DELETE";

    // ===== 打卡模块 =====
    /** 新增打卡，参数：userId / spotId / comment */
    public static final String CHECKIN_ADD = "CHECKIN_ADD";
    /** 修改打卡心得，参数：id / comment */
    public static final String CHECKIN_UPDATE_COMMENT = "CHECKIN_UPDATE_COMMENT";
    /** 删除打卡记录，参数：id */
    public static final String CHECKIN_DELETE = "CHECKIN_DELETE";
    /** 查询用户打卡列表，参数：userId / 可选 keyword / spotId */
    public static final String CHECKIN_LIST_BY_USER = "CHECKIN_LIST_BY_USER";
    /** 查询全部打卡记录（管理员），可选参数：keyword */
    public static final String CHECKIN_LIST_ALL = "CHECKIN_LIST_ALL";
    /** 热门景点排行榜（按打卡次数降序） */
    public static final String CHECKIN_RANK = "CHECKIN_RANK";
}
