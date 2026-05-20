# knowpivot-embedding

知枢统一 Embedding 代理服务 — Python + FastAPI，暴露 OpenAI 兼容的 `/v1/embeddings` 接口。

## 技术栈

| 类别 | 技术 |
|------|------|
| 框架 | FastAPI 0.136 |
| 语言 | Python 3.13 |
| 包管理 | uv |
| HTTP | httpx |

## API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/v1/embeddings` | 生成向量（OpenAI 兼容格式） |

请求/响应格式完全兼容 OpenAI `/v1/embeddings`，保持 `model`、`input`、`encoding_format` 等标准参数。

## 架构

```python
knowpivot-embedding/
├── main.py                    # FastAPI 入口，port 8001
├── api/v1/embedding.py        # POST /v1/embeddings 接口
├── infrastructure/config.py   # pydantic-settings 配置
└── schema/
    ├── request.py             # EmbeddingRequest
    └── response.py            # EmbeddingResponse
```

本服务是纯粹的代理层 — 将请求转发到上游 Embedding API。不做向量存储、不做文本分块。

## 常用命令

```bash
uv sync                   # 安装依赖
uv run python main.py     # 启动服务（端口 8001）
uv run pytest             # 运行测试
```

## 环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `EMBEDDING_BASE_URL` | `https://api.openai.com` | 上游 API 地址 |
| `EMBEDDING_API_KEY` | — | 上游 API 密钥 |
| `EMBEDDING_MODEL` | `qwen3-embedding:0.6b` | 默认模型 |
| `EMBEDDING_DIMENSION` | `1536` | 向量维度 |
| `SERVER_PORT` | `8001` | 服务端口 |

## 调用方

- `knowpivot-task`：`EMBEDDING_BASE_URL=http://localhost:8001/v1`
- `knowpivot-server`：`knowpivot.embedding.base-url=http://localhost:8001`

## 所属仓库

本项目属于 KnowPivot 单仓库，详见根目录 [CLAUDE.md](../CLAUDE.md) 及 [README.md](../README.md)。

提交 scope：`embedding`
