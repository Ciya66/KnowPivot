---
name: backend-dev-spec
description: 知枢Agentic智能知识库平台后端开发规范，SpringBoot + DDD分层 + Redis向量 + MySQL + Kafka + MinIO，严格遵循项目接口与数据库设计。
---

# 知枢项目 - 后端统一开发规范
## 1. 技术栈规范
- Java 17+ + SpringBoot 3.2.x
- MyBatis-Plus
- MySQL 8.0
- Redis Stack（向量检索）
- Kafka 异步消息
- MinIO 对象存储
- Sa-Token 权限鉴权
- SSE 流式输出

## 2. 架构分层规范（严格DDD）
1. interfaces：Controller、VO、DTO
2. application：服务编排、事务、流程
3. domain：实体、领域服务、仓储接口
4. ai：Agent网关、模型策略、Prompt管理
5. infrastructure：数据实现、Redis向量、MQ、文件存储

## 3. 命名规范
- 包名：全小写
- 类：大驼峰
- 方法/变量：小驼峰
- 常量：大写+下划线
- 表名：t_下划线命名
- 接口：RESTful 小写短横线

## 4. 数据库规范
- 所有表必须：id、created_at、updated_at、is_deleted
- 逻辑删除，不物理删除
- 文档状态：0已上传 1解析中 2已索引 3失败
- 向量ID全局唯一
- JSON字段仅用于配置、引用、元数据

## 5. 接口规范
- 统一返回格式：code、message、data、timestamp
- 200成功，401未登录，403无权限，404不存在
- 50001 Token配额不足
- 对话必须使用 SSE 流式返回
- 入参使用 @Valid 校验

## 6. 业务规范
- 用户：token_quota 配额控制
- 知识库：kb_id 隔离权限
- 文档：上传 → Kafka → 解析 → 向量化 → Redis
- 对话：session_id 管理上下文
- Agent：Java网关调用Python服务
- 向量检索：Redis FT.SEARCH

## 7. 代码禁止项
- 禁止跨层直接调用
- 禁止实体直接返回前端
- 禁止硬编码魔法值
- 禁止SQL拼接
- 禁止超长方法、超大文件