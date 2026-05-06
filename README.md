<div align="center">

# KnowPivot

**Agentic 智能知识库平台**

基于 Agent + RAG 技术，面向中小企业与个人用户的轻量化私有知识管理平台

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.13-brightgreen)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.5-4fc08d)](https://vuejs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-6.0-3178c6)](https://www.typescriptlang.org/)
[![Java](https://img.shields.io/badge/Java-17-ED8B00)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-blue)](#)

</div>

---

## 项目简介

知枢（KnowPivot）是一个 **Agentic 智能知识库平台**。用户上传文档到知识库，系统自动完成文档解析、文本切片与向量化索引，之后用户可通过多轮对话基于私有文档进行 RAG 智能问答。

### 核心能力

- **知识库管理** — 创建、配置、管理多个独立知识库，支持成员协作与权限控制
- **文档全生命周期** — 文档上传 → MinIO 对象存储 → Kafka 异步解析 → 向量化索引，支持 PDF / DOCX / TXT
- **Agentic RAG 对话** — 基于私有文档的多轮智能问答，SSE 实时流式推送，附带引用溯源
- **Token 配额系统** — 细粒度的 Token 消耗计量与余额管理，支持充值、消耗、回退
- **私有化部署** — 全链路私有，数据不出域，满足企业数据安全与合规要求

---

## 技术栈

<table>
<tr>
<td width="50%">

### 后端 — knowpivot-server

| 层级 | 技术 |
|------|------|
| **框架** | Spring Boot 3.5.13 |
| **语言** | Java 17 |
| **构建** | Maven 3.9+（含 Wrapper） |
| **ORM** | MyBatis-Plus 3.5.9 |
| **数据库** | MySQL 8.0 |
| **缓存 / 向量** | Redis Stack（RediSearch） |
| **消息队列** | Apache Kafka |
| **对象存储** | MinIO |
| **认证** | Sa-Token 1.39.0 + BCrypt |
| **响应式** | Spring WebFlux（SSE 流式） |
| **工具** | Lombok / Hutool / MapStruct / Jackson |

</td>
<td width="50%">

### 前端 — knowpivot-web

| 层级 | 技术 |
|------|------|
| **框架** | Vue 3.5.32 |
| **语言** | TypeScript 6.0 |
| **构建** | Vite 8.0 |
| **路由** | Vue Router 5.0 |
| **状态管理** | Pinia 3.0 |
| **UI 组件** | Element Plus 2.13 |
| **HTTP** | Axios 1.15 |
| **Markdown** | Marked 18.0 |
| **样式** | 纯 CSS Custom Properties |
| **Node 要求** | ^20.19 或 >=22.12 |

</td>
</tr>
</table>

---

## 项目结构

```
KnowPivot/
├── knowpivot-server/               # 后端（Spring Boot + DDD 五层架构）
│   ├── src/main/java/com/knowpivot/server/
│   │   ├── interfaces/             # 接口层 — Controller / DTO / VO
│   │   ├── application/            # 应用层 — 业务编排 / 事务管理
│   │   ├── domain/                 # 领域层 — Entity / Repository接口 / DomainService
│   │   ├── infrastructure/         # 基础设施层 — 持久化 / Redis / Kafka / MinIO / 配置
│   │   └── ai/                     # AI 网关层 — AgentGateway / PythonAgentClient / Strategy
│   ├── src/main/resources/
│   │   ├── application.yml         # 应用配置
│   │   └── db/init.sql             # 数据库初始化脚本（8 张表）
│   └── pom.xml
│
├── knowpivot-web/                  # 前端（Vue 3 + TypeScript）
│   ├── src/
│   │   ├── api/                    # HTTP 接口封装（auth / chat / knowledge）
│   │   ├── components/             # 通用组件（9 个）
│   │   ├── composables/            # 组合式函数（useMarkdown）
│   │   ├── layouts/                # 布局（三栏式 DefaultLayout）
│   │   ├── router/                 # 路由配置（5 个页面）
│   │   ├── stores/                 # Pinia 状态管理（5 个 Store）
│   │   ├── styles/                 # 全局样式（CSS 变量 / 过渡动画）
│   │   ├── types/                  # TypeScript 类型定义
│   │   ├── utils/                  # 工具类（Axios 封装 / SSE 客户端）
│   │   └── views/                  # 页面视图（5 个）
│   ├── package.json
│   └── vite.config.ts
│
├── ai-op/                          # 项目规划与 AI 提示词文档
│   ├── 0.Project-Background.md     # 项目背景与市场分析
│   ├── 1.Requirements-Analysis.md  # 需求分析
│   ├── 2.Requirement-Breakdown.md  # 需求拆解
│   ├── 3.Architecture-Design.md    # 架构设计
│   ├── 4.Database-Design.md        # 数据库设计
│   ├── 5.API.md                    # 接口文档
│   └── prompts/                    # AI 提示词模板
│
├── CLAUDE.md                       # Git 工作流规范
├── .gitignore
└── README.md
```

---

## 系统架构

### DDD 五层架构

```
┌─────────────────────────────────────────────────────────────────┐
│  Interfaces Layer   Controller / DTO / VO                       │
│  接口层             参数校验 · 身份认证 · 响应封装               │
├─────────────────────────────────────────────────────────────────┤
│  Application Layer  ApplicationService / Assembler              │
│  应用层             业务编排 · 事务管理 · 权限校验               │
├─────────────────────────────────────────────────────────────────┤
│  Domain Layer       Entity / Repository / DomainService / Enum  │
│  领域层             纯业务逻辑 · 状态流转 · 业务规则             │
├─────────────────────┬───────────────────────────────────────────┤
│  Infrastructure     │  AI Gateway                               │
│  基础设施层         │  AI 网关层                                 │
│  Repository实现     │  AgentGateway（唯一入口）                  │
│  MyBatis-Plus Mapper│  PythonAgentClient（WebClient SSE）        │
│  Redis / MinIO      │  ModelStrategy（模型选择策略）             │
│  Kafka / Config     │                                           │
└─────────────────────┴───────────────────────────────────────────┘
```

### 核心数据流

**用户提问：**
```
前端 → ChatController → ChatApplicationService
  → 保存用户消息 → 构建上下文 → AgentGateway.chat()
  → PythonAgentClient (SSE) → 流式推送 response
  → 保存 AI 回复 → 扣减 Token 配额
```

**文档上传：**
```
前端 → DocumentController → DocumentApplicationService
  → 上传至 MinIO → 保存数据库记录
  → Kafka 异步消息 → Python 端解析/向量化
  → 更新文档状态为"已索引"
```

---

## 数据库设计

系统共包含 **8 张核心表**，完整建表脚本见 `knowpivot-server/src/main/resources/db/init.sql`。

| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `t_user` | 用户表 | username, password_hash, token_quota, role |
| `t_knowledge_base` | 知识库表 | name, description, index_name, config(JSON) |
| `t_kb_member` | 知识库成员表 | kb_id, user_id, role |
| `t_document` | 文档表 | kb_id, file_name, storage_path, status |
| `t_document_segment` | 文档切片表 | doc_id, vector_id, content |
| `t_conversation` | 会话表 | user_id, kb_id, title |
| `t_message` | 消息表 | conversation_id, role, content, references(JSON) |
| `t_prompt_template` | Prompt模板表 | code, name, content |
| `t_token_transaction` | Token流水表 | user_id, amount, balance_after, type |

### 文档状态流转

```
[已上传] → [解析中] → [已索引]
                  ↘ [失败]
```

---

## API 接口

统一前缀 `/api/v1`，所有响应遵循统一结构：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { },
  "timestamp": "2025-01-01 00:00:00"
}
```

### 接口总览

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| **认证** | POST | `/api/v1/auth/register` | 用户注册 |
| | POST | `/api/v1/auth/login` | 用户登录，返回 Token |
| **用户** | GET | `/api/v1/users/me` | 获取当前用户信息 |
| **知识库** | POST | `/api/v1/knowledge-bases` | 创建知识库 |
| | GET | `/api/v1/knowledge-bases` | 知识库列表（分页） |
| | PUT | `/api/v1/knowledge-bases/{kbId}` | 更新知识库 |
| | DELETE | `/api/v1/knowledge-bases/{kbId}` | 删除知识库 |
| **文档** | POST | `/api/v1/knowledge-bases/{kbId}/documents/upload` | 上传文档 |
| | GET | `/api/v1/knowledge-bases/{kbId}/documents` | 文档列表（分页） |
| | DELETE | `/api/v1/documents/{docId}` | 删除文档 |
| **对话** | POST | `/api/v1/conversations` | 创建会话 |
| | POST | `/api/v1/chat/messages` | 发送消息（SSE 流式响应） |
| | GET | `/api/v1/conversations/{id}/messages` | 历史消息（分页） |

### SSE 流式事件类型

| 事件 | 说明 | 数据结构 |
|------|------|----------|
| `message` | 增量回答内容 | `{ "delta": "..." }` |
| `references` | 引用来源 | `{ "sources": [...] }` |
| `done` | 回答结束 | `{ "messageId", "tokenCount", "finishReason" }` |

### 业务状态码

| 状态码 | 说明 |
|--------|------|
| `200` | 成功 |
| `400` | 参数错误 |
| `401` | 未认证 |
| `403` | 无权限 |
| `404` | 资源不存在 |
| `409` | 冲突（用户名重复等） |
| `50001` | Token 配额不足 |
| `50002` | 业务处理失败 |
| `50301` | 模型服务不可用 |

---

## 快速开始

### 环境要求

| 依赖 | 版本 | 用途 |
|------|------|------|
| JDK | 17+ | 后端运行 |
| Maven | 3.9+ | 后端构建（项目内置 Wrapper，可免安装） |
| Node.js | ^20.19 或 >=22.12 | 前端运行 |
| MySQL | 8.0+ | 主数据库 |
| Redis Stack | 7.0+ | 缓存 + RediSearch 向量检索 |
| Apache Kafka | 3.0+ | 文档解析异步消息 |
| MinIO | 最新版 | 文档对象存储 |

### 1. 克隆仓库

```bash
git clone https://github.com/Ciya66/KnowPivot.git
cd KnowPivot
```

### 2. 初始化数据库

启动 MySQL 后执行初始化脚本：

```bash
mysql -u root -p < knowpivot-server/src/main/resources/db/init.sql
```

脚本会自动创建 `knowpivot` 数据库和全部 8 张表，并插入默认管理员账号（`admin / admin123`）。

### 3. 配置中间件

确保以下服务已在本地运行（默认配置见 `application.yml`）：

| 服务 | 地址 | 配置文件 |
|------|------|----------|
| MySQL | `localhost:3306` | `spring.datasource.*` |
| Redis | `localhost:6379` | `spring.data.redis.*` |
| Kafka | `localhost:9092` | `spring.kafka.*` |
| MinIO | `localhost:9000` | `minio.*` |

### 4. 启动后端

```bash
cd knowpivot-server

# 首次编译
./mvnw compile

# 启动服务（默认端口 8080）
./mvnw spring-boot:run
```

### 5. 启动前端

```bash
cd knowpivot-web

# 安装依赖
npm install

# 启动开发服务器（默认端口 5173）
npm run dev
```

前端开发服务器已配置代理，`/api` 请求会自动转发到 `http://localhost:8080`。

### 6. 访问应用

打开浏览器访问 http://localhost:5173，使用默认管理员账号登录：

| 字段 | 值 |
|------|-----|
| 用户名 | `admin` |
| 密码 | `admin123` |

---

## 常用命令

### 后端

```bash
cd knowpivot-server

./mvnw compile                    # 编译
./mvnw clean package -DskipTests  # 打包（跳过测试）
./mvnw spring-boot:run            # 启动
./mvnw test                       # 运行全部测试
./mvnw test -Dtest=ClassName      # 运行单个测试类
```

### 前端

```bash
cd knowpivot-web

npm run dev          # 启动开发服务器
npm run build        # 类型检查 + 生产构建
npm run type-check   # TypeScript 类型检查
npm run build-only   # 仅生产构建（不检查类型）
npm run preview      # 预览生产构建
```

---

## 前端页面

系统采用三栏式布局设计，桌面端优先。

```
┌──────────┬──────────────────────────┬────────────┐
│ SideNav  │     Main Content         │ RightPanel │
│  240px   │     flex: 1              │  320px     │
│ 可折叠   │                          │ 条件显示   │
└──────────┴──────────────────────────┴────────────┘
```

| 路径 | 页面 | 功能 |
|------|------|------|
| `/` | Dashboard | 仪表盘，概览面板 |
| `/knowledge` | Knowledge | 知识库列表与管理 |
| `/agent` | Agent | AI 对话界面 |
| `/documents` | Documents | 文档管理 |
| `/settings` | Settings | 用户设置 |

---

## 环境变量

### 后端 — application.yml

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `server.port` | `8080` | 服务端口 |
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/knowpivot` | 数据库连接 |
| `spring.datasource.username` | `root` | 数据库用户名 |
| `spring.datasource.password` | `123456` | 数据库密码 |
| `spring.data.redis.host` | `localhost` | Redis 地址 |
| `spring.kafka.bootstrap-servers` | `localhost:9092` | Kafka 地址 |
| `minio.endpoint` | `http://localhost:9000` | MinIO 地址 |
| `minio.access-key` | `admin` | MinIO Access Key |
| `minio.secret-key` | `Admin@123456` | MinIO Secret Key |
| `ai.gateway.base-url` | `http://localhost:8000` | Python AI Agent 地址 |

### 前端 — .env.development

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `VITE_API_BASE_URL` | `http://localhost:8080` | API 服务地址 |

---

## 核心设计约定

| 约定 | 说明 |
|------|------|
| **ID 生成** | 雪花算法，所有 ID 为 `Long` 类型 |
| **ID 序列化** | DTO 中 ID 字段标注 `@JsonLongId`，Long → String 防止 JS 精度丢失 |
| **认证方式** | Sa-Token，Token 通过 `Authorization: Bearer {token}` 传递，有效期 2 小时 |
| **密码加密** | BCrypt |
| **ORM** | MyBatis-Plus，逻辑删除（`is_deleted`），自动填充时间字段 |
| **分页** | `pageNum` / `pageSize` 入参，`total` + `list` 出参 |
| **响应体** | 统一 `Result<T>`：`code` / `message` / `data` / `timestamp` |
| **API 前缀** | 所有接口 `/api/v1` 下 |
| **向量维度** | 1536 维（Cosine 距离） |

---

## Git 工作流

详细规范见根目录 [CLAUDE.md](./CLAUDE.md)。

| 分支 | 用途 |
|------|------|
| `main` | 生产就绪代码 |
| `develop` | 开发集成分支 |
| `feature/*` | 功能开发 |
| `fix/*` | 缺陷修复 |

提交信息格式：`<type>(<scope>): <中文描述>`

```
feat(server): 添加知识库CRUD接口
fix(web): 修复侧边栏折叠动画闪烁
docs: 更新API接口文档
```

---

## 开发路线图

- [x] 项目架构搭建（DDD 五层 + Vue3）
- [x] 用户认证系统（注册 / 登录 / Token）
- [x] 知识库 CRUD 与成员管理
- [x] 文档上传与 MinIO 存储
- [x] Kafka 异步文档解析管道
- [x] Redis 向量存储与相似度检索
- [x] SSE 流式对话接口
- [x] 前端完整 UI 实现（三栏布局 / 5 个页面）
- [ ] Python AI Agent 服务接入
- [ ] 前后端真实 API 联调
- [ ] Docker 容器化部署
- [ ] CI/CD 自动化流水线
- [ ] 文档版本管理与历史对比
- [ ] 多模型切换与降级策略

---

## 许可证

本项目采用 [MIT License](./LICENSE) 开源协议。
