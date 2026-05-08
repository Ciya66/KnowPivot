# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

`knowpivot-task` 是 KnowPivot 的后台文档解析 Worker，用 Python 3.13 编写。通过 Kafka 消费文档解析消息，从 MinIO 下载文件，提取文本内容，后续进行分块和向量化存储。

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
├── core/                  # 核心业务逻辑
│   └── document/
│       └── parser.py      # 文本提取（PDF/DOCX/TXT/MD/JSON）
├── infrastructure/        # 基础设施层
│   ├── config.py          # pydantic-settings 配置，从 .env 读取
│   ├── kafka.py           # Kafka 消费者
│   ├── minio.py           # MinIO 文件下载
│   └── redis.py           # Redis 向量存储（尚未接入主流程）
main.py                    # 入口：Kafka 消费 → MinIO 下载 → 文本提取
```

**处理流程**: Kafka message(`document-parsing` topic) → 下载文件到 `temp/{docId}/` → 提取文本 → (TODO: 分块 + 向量化 + Redis 存储)

## 环境变量

通过 `.env` 文件配置（`task/infrastructure/config.py` 中的 `Settings` 类）：

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `KAFKA_BROKER` | `localhost:9092` | Kafka 地址 |
| `REDIS_HOST` | `localhost` | Redis 地址 |
| `REDIS_PORT` | `6379` | Redis 端口 |
| `MINIO_ENDPOINT` | `localhost:9000` | MinIO 地址 |
| `MINIO_ACCESS_KEY` | — | MinIO 访问密钥 |
| `MINIO_SECRET_KEY` | — | MinIO 密钥 |

## 父仓库 Git 规范

本项目属于 KnowPivot 单仓库，提交 scope 为 `task`。详见根目录 `../CLAUDE.md`。

- 提交前确保 `uv run pytest` 通过
- 使用中文 Conventional Commits：`feat(task): 描述`
