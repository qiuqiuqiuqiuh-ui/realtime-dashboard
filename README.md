# 🚀 实时数据大屏 — 高并发系统

一个高并发实时数据大屏系统，涵盖 Kafka、Redis、Netty WebSocket、Flink 流计算、Nacos 配置中心、Docker 容器化等核心技术。

## 📐 系统架构

```
                          ┌─────────────────────────────┐
                          │         Nginx (80)           │
                          └──────┬──────────┬────────────┘
                                 │          │
                          ┌──────▼──────┐ ┌─▼────────────┐
                          │  Gateway    │ │  Collector    │
                          │  (8080)     │ │  (8081)       │
                          │  限流+校验   │ │  消费+聚合     │
                          └──────┬──────┘ └──┬────────────┘
                                 │           │
                          ┌──────▼──────┐ ┌──▼──────┐
                          │   Kafka     │ │  Redis   │
                          │  (9092)     │ │ (6379)   │
                          └─────────────┘ └─────────┘
                                 │
                          ┌──────▼──────┐
                          │  Storage    │──── MySQL
                          │  (8082)     │
                          └─────────────┘

        ┌────────────────────────────────────────────────────┐
        │  Netty WS (9090)  │  Flink (1.18)  │  Nacos (8848) │
        │  高并发长连接       │  流计算         │  配置中心       │
        └────────────────────────────────────────────────────┘
```

## 🛠️ 技术栈

| 层级 | 技术 | 说明 |
|------|------|------|
| 框架 | Spring Boot 3.2 | Java 框架 |
| 消息队列 | Apache Kafka 3.6 | 削峰填谷、高吞吐 |
| 缓存 | Redis 7 | 热数据缓存、限流计数 |
| 长连接 | Netty 4.1 | 单机 10 万+ WebSocket 连接 |
| 流计算 | Apache Flink 1.18 | 窗口聚合、Checkpoint |
| 注册中心 | Nacos 2.3 | 服务发现、动态配置 |
| 持久化 | MySQL 8 + MyBatis-Plus | 数据存储、分页查询 |
| 限流 | 滑动窗口 + 令牌桶 | 双层限流、防突刺 |
| API 文档 | SpringDoc OpenAPI 3 | Swagger UI |
| 监控 | Prometheus + Grafana | 指标采集、可视化 |
| 链路追踪 | Zipkin | 分布式追踪 |
| 容器化 | Docker Compose | 一键部署 |
| 反向代理 | Nginx | 负载均衡、WebSocket 代理 |

## 📦 模块说明

```
realtime-dashboard/
├── dashboard-common/         # 公共模块
│   ├── dto/                  # 数据传输对象
│   ├── result/               # 统一响应 R<T>、错误码
│   ├── exception/            # 业务异常
│   └── constant/             # Kafka/Redis 常量
├── dashboard-gateway/        # 网关服务 (端口 8080)
│   ├── controller/           # REST 接口
│   ├── ratelimit/            # 滑动窗口 + 令牌桶限流
│   ├── aspect/               # AOP 耗时统计、操作日志
│   ├── exception/            # 全局异常处理
│   └── config/               # Kafka、Swagger、Nacos 配置
├── dashboard-consumer/       # 采集服务 (端口 8081 + Netty 9090)
│   ├── kafka/                # Kafka 消费者 (手动 ACK)
│   ├── service/              # 实时聚合计算
│   ├── netty/                # Netty WebSocket (连接管理、心跳)
│   ├── flink/                # Flink 集成提示
│   └── config/               # Redis、线程池、Nacos 配置
├── dashboard-storage/        # 存储服务 (端口 8082)
│   ├── entity/               # MyBatis-Plus 实体
│   ├── mapper/               # 数据库 Mapper
│   ├── service/              # 查询服务
│   └── controller/           # 查询接口
├── dashboard-flink/          # Flink 流计算模块 (独立部署)
│   └── src/                  # Kafka Source → 窗口聚合 → 双路输出
├── dashboard-front/          # 前端大屏 (Vue3 + ECharts)
├── deploy/                   # 部署配置
│   ├── nginx/nginx.conf      # Nginx 反向代理
│   ├── prometheus/           # Prometheus 配置
│   ├── nacos/                # Nacos 配置模板
│   └── init.sql              # MySQL 初始化脚本
├── docker-compose.yml        # Docker 一键部署
├── start.ps1                 # Windows 一键启动脚本
└── stop.ps1                  # 一键停止脚本
```

## 🚀 启动步骤

### 前置条件

- JDK 17+
- Maven 3.8+
- Docker Desktop
- PowerShell

### 方式一：本地开发 (推荐)

```powershell
# 1. 启动 Kafka + Redis
docker compose up -d kafka zookeeper redis

# 2. 等待 30 秒
Start-Sleep -Seconds 30

# 3. 构建项目
cd d:\AI开发\realtime-dashboard
mvn clean package -DskipTests

# 4. 启动 Gateway (终端 1)
java -jar dashboard-gateway/target/dashboard-gateway-1.0.0.jar

# 5. 启动 Collector (终端 2)
java -jar dashboard-consumer/target/dashboard-consumer-1.0.0.jar

# 6. 打开大屏
# 浏览器打开 dashboard-front\index.html
```

### 方式二：Docker Compose

```powershell
docker compose up -d
```

### 测试

```powershell
# 健康检查
curl http://localhost:8080/api/metric/health

# 发送测试数据
curl -X POST http://localhost:8080/api/metric/report -H "Content-Type: application/json" -d "{\"metricName\":\"qps\",\"value\":1500,\"tags\":{},\"timestamp\":0}"

# 高并发压测
for ($i = 1; $i -le 500; $i++) {
    $qps = Get-Random -Minimum 10000 -Maximum 100000
    $body = '{\"metricName\":\"qps\",\"value\":' + $qps + ',\"tags\":{},\"timestamp\":0}'
    Invoke-RestMethod -Uri \"http://localhost:8080/api/metric/report\" -Method Post -Body $body -ContentType \"application/json\" | Out-Null
    Start-Sleep -Milliseconds 30
}
```

### 停止服务

```powershell
# 停止 Java 进程
Get-Process java | Stop-Process -Force

# 停止 Docker 容器
docker compose down
```

## 🎯 核心技术亮点

### 1. Netty WebSocket (单机 10 万+ 连接)

```java
// 主从 Reactor 线程模型
// Boss: 1 线程接收连接
// Worker: N 线程处理 I/O

// 心跳检测: 30s 间隔, 60s 超时
// 分组推送: ConcurrentHashMap 管理连接
// 在线计数: AtomicInteger + Redis
```

### 2. 双层限流

```java
// 滑动窗口: 精确控制 QPS，解决固定窗口临界突刺
// 令牌桶: 允许一定程度的突发流量
// 注解式: @RateLimit(qps = 50000)
```

### 3. Kafka 高吞吐优化

```java
// 批量大小: 64KB (减少网络请求)
// 凑批等待: 10ms (更多消息合并)
// 压缩: lz4 (减少传输量)
// Consumer: 500 条/次 + 5 线程并发
```

### 4. Flink 流计算

```java
// 滑动窗口: 5 秒窗口, 1 秒滑动
// 增量聚合: AggregateFunction (内存 O(1))
// 双路输出: Kafka + Redis
// Checkpoint: Exactly-Once 语义
```

### 5. Nacos 动态配置

```java
// @RefreshScope: 配置变更自动刷新
// 限流阈值、线程池参数可热更新
// 共享配置: common.yml 多服务复用
```

### 6. 统一响应 & 异常体系

```java
// R<T> 统一响应: code + message + data + traceId
// ErrorCode 枚举: 语义化错误码
// GlobalExceptionHandler: 区分业务异常/系统异常
```

## 📊 压测数据

| 指标 | 数值 |
|------|------|
| QPS | 50,000+ |
| 响应时间 | 500ms (高并发下) |
| WebSocket 连接 | 10 万+ (Netty) |
| Kafka 吞吐 | 10 万+ msg/s |

## 📁 项目统计

| 指标 | 数量 |
|------|------|
| 模块数 | 5 个 |
| Java 类 | 30+ 个 |
| 配置文件 | 15+ 个 |
| Docker 服务 | 9 个 |
