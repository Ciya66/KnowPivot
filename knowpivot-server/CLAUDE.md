# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

知枢（KnowPivot）— Agentic 智能知识库平台。用户上传文档到知识库，系统解析并向量化后，用户可基于文档进行 RAG 智能问答。

## 构建与运行命令

```bash
# 编译
./mvnw compile

# 打包（跳过测试）
./mvnw clean package -DskipTests

# 启动
./mvnw spring-boot:run

# 运行全部测试
./mvnw test

# 运行单个测试类
./mvnw test -Dtest=ClassName

# 运行单个测试方法
./mvnw test -Dtest=ClassName#methodName
```

环境要求：JDK 17+、MySQL 8.0、Redis Stack、MinIO（端口和凭据见 `application.yml`）。

数据库初始化：`src/main/resources/db/init.sql`

## 架构（严格 DDD 五层）

`com.knowpivot.server` 下五层架构，**禁止跨层调用**，每层只依赖下层：

```
interfaces (Controller, DTO, VO)        ← 接口层：参数校验 + 响应封装
    ↓ 调用
application (ApplicationService)         ← 应用层：业务编排 + 事务管理
    ↓ 调用
domain (Entity, Repository接口, DomainService)  ← 领域层：纯业务逻辑，无框架依赖
    ↑ 由...实现
infrastructure (Repository实现, Mapper, PO, Redis, MinIO, Config)  ← 基础设施层
```

另有 **AI 接入层**（`ai/`），隔离 Java 业务系统与 Python AI 服务：

```
ai/gateway/   → AgentGateway 接口 + DefaultAgentGateway（唯一入口）
ai/client/    → PythonAgentClient（WebClient SSE 调用 Python 服务）
ai/strategy/  → ModelStrategy（动态模型选择策略模式）
ai/model/     → AgentContext, AgentRunRequest, AgentResponse, PromptTemplate
```

### 核心数据流 — 用户提问

`ChatController` → `ChatApplicationService`（保存用户消息、构建上下文、调用 `AgentGateway.chat()` 返回 `Flux<AgentResponse>`）→ SSE 流式推送到前端。收到 `done` 事件后：保存 AI 回复、扣减 Token 配额。

### 核心数据流 — 文档上传

`DocumentController` → `DocumentApplicationService`（上传 MinIO、保存数据库记录）→ 异步 Kafka 消息触发 Python 端解析/向量化。

## 各层职责

- **interfaces**：Controller 仅做 `@Valid` 参数校验和响应封装，不含业务逻辑。所有接口统一返回 `Result<T>`，禁止使用 `Map.of()` 返回数据。
- **application**：编排领域服务和仓储，管理事务，不含 SQL 或框架特有代码。
- **domain**：纯 Java。实体包含业务规则（如 `Document.markAsParsing()`）。Repository 接口在此定义，infrastructure 层实现。
- **infrastructure**：所有技术实现。PO（持久化对象）1:1 映射数据库表，`POConverter` 负责 PO ↔ Entity 转换。
- **ai**：隔离 Java 系统与 Python AI 服务，`AgentGateway` 是唯一入口。

## 关键约定

- **ID 生成**：雪花算法 `IdGenerator`，所有 ID 为 `Long` 类型
- **ID 序列化**：DTO 中所有 ID 字段必须标注 `@JsonLongId`（将 Long 序列化为 String，避免 JS 精度丢失）。`tokenQuota`、`fileSize`、`docCount` 等数值字段不标注
- **认证**：Sa-Token（`StpUtil.login/getLoginIdAsLong`），Token 通过 `Authorization: Bearer {token}` 传递
- **密码**：BCrypt（`BCrypt.hashpw/checkpw`）
- **ORM**：MyBatis-Plus + `BaseMapper`。PO 使用 `@TableId(type = IdType.ASSIGN_ID)`、`@TableLogic` 逻辑删除、`@TableField(fill = FieldFill.INSERT)` 自动填充时间
- **分页**：MyBatis-Plus `Page<T>`，请求参数 `pageNum`/`pageSize`，响应 `PageResult<T>`（含 `total` + `list`）
- **API 前缀**：所有端点 `/api/v1` 下
- **统一响应体**：`Result<T>` 含 `code`、`message`、`data`、`timestamp`
- **业务状态码**：200 成功、400 参数错误、401 未认证、403 无权限、404 不存在、409 冲突、50001 Token 不足、50002 业务失败、50301 模型不可用
- **SSE 流式**：对话端点返回 `text/event-stream`，事件类型：`message`（增量内容）、`references`（引用来源）、`done`（完成统计）
- **枚举模式**：所有枚举实现 `getCode()` 和 `getDesc()`，`POConverter` 通过反射做 int ↔ enum 转换
- **JSON 字段**：`KnowledgeBase.config`、`Message.references` 存为 MySQL JSON，由 `POConverter` 通过 Jackson 映射为对象

## 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 包名 | 全小写 | `com.knowpivot.server.domain.entity` |
| 类名 | 大驼峰 | `KnowledgeApplicationService` |
| 方法/变量 | 小驼峰 | `findByUsername` |
| 数据库表 | t_下划线 | `t_knowledge_base` |
| API 路径 | 短横线 | `/api/v1/knowledge-bases` |
| PO 类名 | {Entity}PO | `KnowledgeBasePO` |

## 关键文档

- `docs/架构设计.md` — 完整架构设计（DDD 分层、AI 网关、数据流）
- `docs/数据库设计.md` — 全部 8 张表的建表 DDL
- `docs/接口文档.md` — 完整 API 规范（路径、参数、返回、状态码）
- `.claude/skills/backend-dev-spec/SKILL.md` — 后端开发编码规范
- `.claude/agents/java-backend-agent.md` — 后端 Agent 规则与约束

## 待接入的外部依赖

- **Kafka**：文档解析异步管道（Java 生产者 → Python 消费者）
- **Redis Stack (RediSearch)**：向量存储与相似度检索（`FT.CREATE`/`FT.SEARCH`）
- **Python AI Agent**：独立服务，位于 `ai.gateway.base-url`，由 `PythonAgentClient` 通过 SSE 调用
