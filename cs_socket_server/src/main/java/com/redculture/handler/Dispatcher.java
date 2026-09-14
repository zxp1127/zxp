package com.redculture.handler;

import com.redculture.pojo.Announcement;
import com.redculture.pojo.ScenicSpot;
import com.redculture.protocol.ActionType;
import com.redculture.protocol.Request;
import com.redculture.protocol.Response;
import com.redculture.service.AnnouncementService;
import com.redculture.service.CheckinService;
import com.redculture.service.ScenicSpotService;
import com.redculture.service.UserService;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求分发器（V3 服务端核心）
 *
 * 作用：
 *   1) 把客户端发来的 Request.action 路由到对应 Service 方法；
 *   2) 把业务异常 / SQL 异常统一包装成 Response.fail，避免异常信息暴露给客户端；
 *   3) 单例无状态，多个 ClientHandler 线程共享，安全并发。
 *
 * 实现方式：查表法 —— action 与处理函数的映射在构造时登记进 Map，
 * 分发时按 action 取出执行，取代逐条 case 的 switch 分支。
 */
public class Dispatcher {

    private final UserService userService = new UserService();
    private final AnnouncementService announcementService = new AnnouncementService();
    private final ScenicSpotService scenicSpotService = new ScenicSpotService();
    private final CheckinService checkinService = new CheckinService();

    /** action -> 处理函数（查表法，构造后不再修改，线程安全） */
    private final Map<String, ActionHandler> handlers = new HashMap<>();

    public Dispatcher() {
        // ---------- 用户模块 ----------
        handlers.put(ActionType.USER_LOGIN, req -> Response.ok("登录成功",
                userService.login(req.getString("username"), req.getString("password"))));
        handlers.put(ActionType.USER_REGISTER, req -> Response.ok("注册成功",
                userService.register(req.getString("username"), req.getString("password"))));
        handlers.put(ActionType.USER_UPDATE_PASSWORD, req -> {
            userService.updatePassword(req.getInt("userId"), req.getString("newPassword"));
            return Response.ok("修改密码成功");
        });
        handlers.put(ActionType.USER_UPDATE_USERNAME, req -> Response.ok("修改用户名成功",
                userService.updateUsername(req.getInt("userId"), req.getString("newUsername"))));
        handlers.put(ActionType.USER_LIST_ALL, req -> Response.ok("查询成功", userService.listAll()));
        handlers.put(ActionType.USER_GET_BY_ID, req -> Response.ok("查询成功",
                userService.getById(req.getInt("userId"))));

        // ---------- 公告模块 ----------
        handlers.put(ActionType.ANN_LIST, req -> Response.ok("查询成功",
                announcementService.list(req.getString("keyword"))));
        handlers.put(ActionType.ANN_GET, req -> Response.ok("查询成功",
                announcementService.get(req.getInt("id"))));
        handlers.put(ActionType.ANN_ADD, req -> {
            announcementService.add(buildAnnouncement(req));
            return Response.ok("新增公告成功");
        });
        handlers.put(ActionType.ANN_UPDATE, req -> {
            announcementService.update(buildAnnouncement(req));
            return Response.ok("修改公告成功");
        });
        handlers.put(ActionType.ANN_DELETE, req -> {
            announcementService.delete(req.getInt("id"));
            return Response.ok("删除公告成功");
        });

        // ---------- 景点模块 ----------
        handlers.put(ActionType.SPOT_LIST, req -> Response.ok("查询成功",
                scenicSpotService.list(req.getString("keyword"))));
        handlers.put(ActionType.SPOT_GET, req -> Response.ok("查询成功",
                scenicSpotService.get(req.getInt("id"))));
        handlers.put(ActionType.SPOT_ADD, req -> {
            scenicSpotService.add(buildSpot(req));
            return Response.ok("新增景点成功");
        });
        handlers.put(ActionType.SPOT_UPDATE, req -> {
            scenicSpotService.update(buildSpot(req));
            return Response.ok("修改景点成功");
        });
        handlers.put(ActionType.SPOT_DELETE, req -> {
            scenicSpotService.delete(req.getInt("id"));
            return Response.ok("删除景点成功");
        });

        // ---------- 打卡模块 ----------
        handlers.put(ActionType.CHECKIN_ADD, req -> {
            checkinService.addCheckin(req.getInt("userId"), req.getInt("spotId"), req.getString("comment"));
            return Response.ok("打卡成功，积分 +1");
        });
        handlers.put(ActionType.CHECKIN_UPDATE_COMMENT, req -> {
            checkinService.updateComment(req.getInt("id"), req.getString("comment"));
            return Response.ok("修改心得成功");
        });
        handlers.put(ActionType.CHECKIN_DELETE, req -> {
            checkinService.deleteCheckin(req.getInt("id"));
            return Response.ok("删除打卡成功，积分 -1");
        });
        handlers.put(ActionType.CHECKIN_LIST_BY_USER, req -> Response.ok("查询成功",
                checkinService.listByUser(req.getInt("userId"), req.getString("keyword"),
                        req.get("spotId") == null ? null : req.getInt("spotId"))));
        handlers.put(ActionType.CHECKIN_LIST_ALL, req -> Response.ok("查询成功",
                checkinService.listAll(req.getString("keyword"))));
        handlers.put(ActionType.CHECKIN_RANK, req -> Response.ok("查询成功",
                checkinService.rankPopularSpots()));
    }

    /**
     * 处理一次请求
     */
    public Response dispatch(Request req) {
        if (req == null || req.getAction() == null) {
            return Response.fail("请求为空");
        }
        String action = req.getAction();
        ActionHandler handler = handlers.get(action);
        if (handler == null) {
            return Response.fail("未知动作：" + action);
        }
        try {
            return handler.handle(req);
        } catch (RuntimeException re) {
            // 业务异常：返回给客户端可读的中文提示
            return Response.fail(re.getMessage() == null ? "业务处理失败" : re.getMessage());
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            return Response.fail("数据库异常：" + sqle.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Response.fail("系统异常：" + e.getMessage());
        }
    }

    /** 由请求参数组装公告实体（新增/修改共用） */
    private static Announcement buildAnnouncement(Request req) {
        Announcement a = new Announcement();
        if (req.get("id") != null) {
            a.setId(req.getInt("id"));
        }
        a.setTitle(req.getString("title"));
        a.setContent(req.getString("content"));
        a.setPublisher(req.getString("publisher"));
        return a;
    }

    /** 由请求参数组装景点实体（新增/修改共用） */
    private static ScenicSpot buildSpot(Request req) {
        ScenicSpot s = new ScenicSpot();
        if (req.get("id") != null) {
            s.setId(req.getInt("id"));
        }
        s.setName(req.getString("name"));
        s.setDescription(req.getString("description"));
        s.setLocation(req.getString("location"));
        s.setPopular(req.getBool("popular"));
        return s;
    }

    /** 单个动作的处理函数：允许抛出受检异常，由 dispatch 统一兜底包装 */
    @FunctionalInterface
    private interface ActionHandler {
        Response handle(Request req) throws Exception;
    }
}
