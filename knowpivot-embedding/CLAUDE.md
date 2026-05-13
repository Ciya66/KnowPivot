# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

`knowpivot-embedding` 是 KnowPivot 的统一 Embedding 代理服务，用 Python 3.13 + FastAPI 编写。暴露 OpenAI 兼容的 `/v1/embeddings` 接口，供 `knowpivot-task`（文档入库）和 `knowpivot-server`（RAG 检索）调用。

## 架构规则

- 本服务是纯粹的代理层，将请求转发到上游 Embedding API（OpenAI 兼容）
- 保持 OpenAI `/v1/embeddings` 的请求/响应格式不变
- 不做向量存储、不做文本分块 — 那些由其他服务负责

## 常用命令

```bash
# 安装依赖（使用 uv）
uv sync

# 运行服务
uv run python main.py

# 运行测试
uv run pytest
```

## 架构

```
knowpivot-embedding/
├── main.py                    # FastAPI 入口，port 8001
├── api/v1/embedding.py        # POST /v1/embeddings 接口
├── infrastructure/config.py   # pydantic-settings 配置
└── schema/
    ├── request.py             # EmbeddingRequest
    └── response.py            # EmbeddingResponse
```

## 环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `EMBEDDING_BASE_URL` | `https://api.openai.com` | 上游 Embedding API 地址 |
| `EMBEDDING_API_KEY` | — | 上游 API 密钥 |
| `EMBEDDING_MODEL` | `text-embedding-3-small` | 默认模型 |
| `EMBEDDING_DIMENSION` | `1536` | 向量维度 |
| `SERVER_PORT` | `8001` | 服务端口 |

## 调用方

- `knowpivot-task`：`EMBEDDING_BASE_URL=http://localhost:8001/v1`
- `knowpivot-server`：`knowpivot.embedding.base-url=http://localhost:8001`

## 父仓库 Git 规范

本项目属于 KnowPivot 单仓库，提交 scope 为 `embedding`。详见根目录 `../CLAUDE.md`。

- 提交前确保 `uv run pytest` 通过
- 使用中文 Conventional Commits：`feat(embedding): 描述`
