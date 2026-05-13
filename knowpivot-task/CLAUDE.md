# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

`knowpivot-task` 是 KnowPivot 的后台文档解析 Worker，用 Python 3.13 编写。通过 Kafka 消费文档解析消息，从 MinIO 下载文件，提取文本、分块、生成 Embedding，然后通过 HTTP 将结果交给 Java server，由 Java 统一管理 Redis 的写入、索引和检索。

## 架构规则

- **Python 不直接操作 Redis** — Redis 的写入、索引创建、向量检索统一由 Java (`knowpivot-server`) 管理
- Python 只负责：文本提取 → 分块 → Embedding 生成
- Embedding 结果通过 HTTP POST 到 Java server（`POST /api/v1/documents/indexing/callback`）
- Java 收到回调后：存储向量到 Redis → 保存 DocumentSegment 到 MySQL → 更新文档状态为 INDEXED

## 常用命令

```bash
# 安装依赖（使用 uv）
uv sync

# 运行 Worker
uv run python main.py

# 运行测试
uv run pytest

# 运行单个测试
uv run pytest tests/test_parser.py::test_extract_pdf
```

## 架构

```
task/
├── core/                        # 核心业务逻辑
│   └── document/
│       ├── parser.py            # 文本提取（PDF/DOCX/TXT/MD/JSON）
│       └── chunker.py           # 文本分块（langchain RecursiveCharacterTextSplitter）
├── infrastructure/              # 基础设施层
│   ├── config.py                # pydantic-settings 配置，从 .env 读取
│   ├── kafka.py                 # Kafka 消费者
│   ├── minio.py                 # MinIO 文件下载
│   ├── embedding.py             # OpenAI 兼容 Embedding API 客户端
│   └── server_client.py         # HTTP 客户端，将 Embedding 结果提交给 Java server
main.py                          # 入口：Kafka 消费 → MinIO 下载 → 文本提取 → 分块 → Embedding → 提交给 Java
```

**处理流程**: Kafka message(`document-parsing` topic) → 下载文件到 `temp/{docId}/` → 提取文本 → 分块 → 生成 Embedding → HTTP POST 到 Java server

## 环境变量

通过 `.env` 文件配置（`task/infrastructure/config.py` 中的 `Settings` 类）：

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `KAFKA_BROKER` | `localhost:9092` | Kafka 地址 |
| `MINIO_ENDPOINT` | `localhost:9000` | MinIO 地址 |
| `MINIO_ACCESS_KEY` | — | MinIO 访问密钥 |
| `MINIO_SECRET_KEY` | — | MinIO 密钥 |
| `EMBEDDING_BASE_URL` | `http://localhost:8001/v1` | Embedding API 地址（knowpivot-embedding 服务） |
| `EMBEDDING_API_KEY` | — | Embedding API 密钥 |
| `EMBEDDING_MODEL` | `text-embedding-3-small` | Embedding 模型名 |
| `EMBEDDING_DIMENSION` | `1536` | 向量维度 |
| `SERVER_API_URL` | `http://localhost:8080` | Java server 地址 |

## 父仓库 Git 规范

本项目属于 KnowPivot 单仓库，提交 scope 为 `task`。详见根目录 `../CLAUDE.md`。

- 提交前确保 `uv run pytest` 通过
- 使用中文 Conventional Commits：`feat(task): 描述`
