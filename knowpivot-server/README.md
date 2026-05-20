# knowpivot-server

知枢后端核心服务 — Spring Boot 3.5 + DDD 五层架构。

## 技术栈

| 类别 | 技术 |
|------|------|
| 框架 | Spring Boot 3.5.13 |
| 语言 | Java 17 |
| 构建 | Maven 3.9+（内置 Wrapper） |
| ORM | MyBatis-Plus 3.5.9 |
| 数据库 | MySQL 8.0 |
| 缓存/向量 | Redis Stack（RediSearch） |
| 消息队列 | Apache Kafka |
| 对象存储 | MinIO |
| 认证 | Sa-Token 1.39 + BCrypt |
| 响应式 SSE | Spring WebFlux |

## 架构

严格 DDD 五层架构，**禁止跨层调用**，每层只依赖下层：

```
interfaces (Controller, DTO, VO)
    ↓ 调用
application (ApplicationService)
    ↓ 调用
domain (Entity, Repository接口, DomainService)
    ↑ 由...实现
infrastructure (Repository实现, Mapper, PO, Redis, MinIO, Config)

AI 网关层（ai/gateway/）— 隔离 Java 与 Python AI 服务
```

## API 总览

统一前缀 `/api/v1`。

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 认证 | POST | `/auth/register` | 注册 |
| | POST | `/auth/login` | 登录，返回 Token |
| 用户 | GET | `/users/me` | 当前用户信息 |
| 知识库 | POST | `/knowledge-bases` | 创建 |
| | GET | `/knowledge-bases` | 列表（分页） |
| | PUT | `/knowledge-bases/{kbId}` | 更新 |
| | DELETE | `/knowledge-bases/{kbId}` | 删除 |
| 文档 | POST | `/{kbId}/documents/upload` | 上传 |
| | GET | `/{kbId}/documents` | 列表（分页） |
| | DELETE | `/documents/{docId}` | 删除 |
| 对话 | POST | `/conversations` | 创建会话 |
| | POST | `/chat/messages` | 发送消息（SSE 流式） |
| | GET | `/conversations/{id}/messages` | 历史消息 |
| Agent | POST | `/agent/generate-title` | 生成对话标题 |
| 索引回调 | POST | `/documents/indexing/callback` | 文档解析回调 |

### SSE 流式事件

| 事件 | 说明 | 数据 |
|------|------|------|
| `message` | 增量回答 | `{"delta": "..."}` |
| `references` | 引用来源 | `{"sources": [...]}` |
| `done` | 结束 | `{"messageId", "tokenCount"}` |
| `error` | 错误 | `{"errorMessage": "..."}` |

## 数据流

**用户提问：**
```
ChatController → ChatApplicationService（保存消息、构建上下文）
  → AgentGateway.chat() → PythonAgentClient (SSE)
  → Flux<AgentResponse> → 流式推送到前端
  → done 事件后：保存 AI 回复 → 扣减 Token
```

**文档上传：**
```
DocumentController → DocumentApplicationService
  → 上传 MinIO → 保存记录 → Kafka 消息
  → Python 端解析/向量化
  → 回调 IndexingCallbackController → 更新文档状态
```

## 数据库

9 张核心表，初始化脚本：`src/main/resources/db/init.sql`

| 表 | 说明 |
|----|------|
| `t_user` | 用户 |
| `t_knowledge_base` | 知识库 |
| `t_kb_member` | 知识库成员 |
| `t_document` | 文档 |
| `t_document_segment` | 文档切片 |
| `t_conversation` | 会话 |
| `t_message` | 消息 |
| `t_prompt_template` | Prompt 模板 |
| `t_token_transaction` | Token 流水 |

## 常用命令

```bash
./mvnw compile                    # 编译
./mvnw spring-boot:run            # 启动（端口 8080）
./mvnw test                       # 测试
./mvnw clean package -DskipTests  # 打包
```

## 环境要求

- JDK 17+
- MySQL 8.0
- Redis Stack
- Kafka
- MinIO
- Python AI Agent 服务（knowpivot-agent，端口 8000）
- Embedding 服务（knowpivot-embedding，端口 8001）

## 所属仓库

本项目属于 KnowPivot 单仓库，详见根目录 [CLAUDE.md](../CLAUDE.md) 及 [README.md](../README.md)。
