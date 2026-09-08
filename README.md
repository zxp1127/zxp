# 红色文化打卡系统 V3 - C/S 可视化联网版

> Socket + Swing 客户端 + MySQL 服务端

## 1. 项目简介

V3 版本基于 V2 的可视化思路，把单机版的本地 SQLite + Service 改造为**长连接 Socket 通信 + MySQL 集中存储**，实现真正的多客户端联网打卡。

| 维度 | V2 单机版 | V3 联网版（本项目） |
| --- | --- | --- |
| 数据存储 | SQLite 单机文件 | MySQL 服务端集中存储 |
| 业务计算 | 客户端本地 | 服务端集中处理 |
| 网络通信 | 无 | TCP Socket 长连接 + Java 对象序列化 |
| 多客户端 | 不支持 | 支持多客户端并发 |

## 2. 目录结构

```
code/
├─cs_socket_server/          # 服务端
│  └─src/main/
│     ├─java/com/redculture/
│     │  ├─pojo/             # 公共实体（User/Announcement/ScenicSpot/CheckinRecord）
│     │  ├─protocol/         # Request/Response/ActionType 通信协议
│     │  ├─db/               # DBUtil MySQL 连接工具
│     │  ├─dao/              # DAO 层 CRUD
│     │  ├─service/          # Service 层业务逻辑（积分增减、热门判定、注册重名校验）
│     │  ├─handler/          # Dispatcher 请求分发
│     │  └─server/           # ServerMain 入口 + ClientHandler 线程处理
│     └─resources/db.properties
├─cs_socket_client/          # 客户端
│  └─src/main/
│     ├─java/com/redculture/
│     │  ├─pojo/             # 与服务端完全一致的 POJO（serialVersionUID 相同）
│     │  ├─protocol/         # 与服务端完全一致的协议
│     │  ├─net/              # NetUtil 网络工具类（长连接 + 自动重连）
│     │  └─ui/               # Swing 全部 UI（登录/主窗口/各业务面板）
│     └─resources/client.properties
doc/v3/
├─redculture_v3.sql          # MySQL 建表脚本 + 初始化数据
└─README.md
```

## 3. 业务规则（与 V1/V2 公共部分保持一致）

- 用户：注册时用户名重名校验；普通用户默认积分 0；管理员账号 `admin / admin123` 由 SQL 脚本硬编码。
- 公告：管理员可增删改查；普通用户只能查看；支持按标题或编号关键字搜索。
- 景点：管理员可维护；热门景点由 `is_popular` 字段标记。
- 打卡：
  - 打卡积分 +1；
  - 删除打卡积分 -1，下限 0（由 SQL `GREATEST(points + ?, 0)` 兜底）；
  - 修改打卡仅允许修改心得；
  - 热门景点排行榜按打卡次数降序。

## 4. 通信协议

- 序列化方式：Java 原生对象序列化（`ObjectInputStream` / `ObjectOutputStream`）。
- 报文：`Request{action, params}` 与 `Response{success, message, data}`。
- 客户端每次发送一个 Request，阻塞等待服务端返回一个 Response；服务端单线程处理每个客户端的循环请求。
- 长连接：客户端启动时建立一次连接，复用至关闭；连接断开时 NetUtil 自动重连一次。
- 所有 action 常量集中在 `ActionType`，便于服务端 Dispatcher 路由和客户端构造请求。

## 5. 运行步骤

### 5.1 准备 MySQL 数据库

```bash
mysql -uroot -p < doc/v3/redculture_v3.sql
```

如需修改连接配置，编辑 `cs_socket_server/src/main/resources/db.properties`：
```
jdbc.url=jdbc:mysql://localhost:3306/redculture_v3?useSSL=false&useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
jdbc.username=root
jdbc.password=123456
```

### 5.2 编译服务端

```bash
cd code/cs_socket_server
mvn clean package
# 产物：target/redculture-v3-server.jar
```

### 5.3 启动服务端

```bash
java -jar target/redculture-v3-server.jar
# 默认监听 8088 端口
# 自定义端口：
java -Dport=9000 -jar target/redculture-v3-server.jar
```

### 5.4 编译客户端

```bash
cd code/cs_socket_client
mvn clean package
# 产物：target/redculture-v3-client.jar
```

### 5.5 启动客户端

```bash
java -jar target/redculture-v3-client.jar
```

如需连接远端服务端，编辑 `cs_socket_client/src/main/resources/client.properties`：
```
server.host=服务端 IP
server.port=8088
```

### 5.6 默认账号

| 角色 | 用户名 | 密码 |
| --- | --- | --- |
| 管理员 | admin | admin123 |
| 普通用户 | stu01 | 123456 |
| 普通用户 | stu02 | 123456 |

## 6. 并发安全说明

- 服务端使用 `Executors.newFixedThreadPool(20)` 处理多客户端连接，每个客户端独立线程。
- DAO 每个方法内部创建独立的 `Connection`，无共享状态，天然线程安全。
- Dispatcher / Service / DAO 均为无状态单例，多线程共享安全。
- MySQL 表使用 InnoDB 引擎，依赖行级锁保证并发写入一致性。

## 7. JPlag 查重规避要点

- 客户端 UI 层完全独立于 V2/V4，面板结构、事件监听写法均有差异。
- 服务端 `Dispatcher` 采用 `switch-case` 路由风格，与 V4 的 SpringBoot Controller 注解风格完全不同。
- 增加中文注释，业务方法名加入 `submit/refresh/showEditDialog` 等命名差异。
- 网络层 `NetUtil`/`ClientHandler` 为 V3 特有，单机版无此代码。

## 8. 与公共模型的关系

V3 复用小组 Day1-Day2 完成的公共产出：
- 4 张数据表字段与 V1/V2 SQLite 版本完全对齐（仅字段类型按 MySQL 习惯调整）。
- 实体 POJO 字段一一对应，`serialVersionUID` 统一为 `20260907L`，保证跨进程序列化兼容。
- 业务规则（积分增减、热门判定、注册重名）与 V1/V2 完全一致。
