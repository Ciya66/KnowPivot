# knowpivot-task

知枢后台文档解析 Worker — Python + Kafka 消费，负责文档文本提取、分块与向量化。

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Python 3.13 |
| 包管理 | uv |
| 消息队列 | kafka-python |
| 文件解析 | pypdf / python-docx |
| 文本分块 | langchain-text-splitters |
| HTTP | httpx |
| 对象存储 | minio |

## 处理流程

```
Kafka message (topic: document-parsing)
    ↓
MinIO 下载文件到 temp/{docId}/
    ↓
提取文本（PDF / DOCX / TXT / MD / JSON）
    ↓
文本分块（RecursiveCharacterTextSplitter）
    ↓
生成 Embedding（调用 knowpivot-embedding）
    ↓
HTTP POST 提交结果到 knowpivot-server (回调)
```

## 架构

```python
task/
├── core/document/
│   ├── parser.py     # 文本提取（支持多格式）
│   └── chunker.py    # 文本分块
├── infrastructure/
│   ├── config.py     # 配置（pydantic-settings）
│   ├── kafka.py      # Kafka 消费者
│   ├── minio.py      # MinIO 文件下载
│   ├── embedding.py  # Embedding API 客户端
│   └── server_client.py  # 提交结果到 Java server
main.py                # 入口
```

## 常用命令

```bash
uv sync                   # 安装依赖
uv run python main.py     # 启动 Kafka 消费者
uv run pytest             # 运行测试
```

## 环境变量

通过 `.env` 文件配置：

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `KAFKA_BROKER` | `localhost:9092` | Kafka 地址 |
| `MINIO_ENDPOINT` | `localhost:9000` | MinIO 地址 |
| `MINIO_ACCESS_KEY` | — | MinIO 密钥 |
| `MINIO_SECRET_KEY` | — | MinIO 密钥 |
| `EMBEDDING_BASE_URL` | `http://localhost:8001/v1` | Embedding 服务地址 |
| `EMBEDDING_API_KEY` | — | Embedding API 密钥 |
| `EMBEDDING_MODEL` | `qwen3-embedding:0.6b` | Embedding 模型 |
| `EMBEDDING_DIMENSION` | `1536` | 向量维度 |
| `SERVER_API_URL` | `http://localhost:8080` | Java server 地址 |

## 架构规则

- Python **不直接操作 Redis** — Redis 写入由 Java (`knowpivot-server`) 统一管理
- Python 只负责：文本提取 → 分块 → Embedding 生成
- Embedding 结果通过 HTTP POST 到 Java server 回调接口

## 所属仓库

本项目属于 KnowPivot 单仓库，详见根目录 [CLAUDE.md](../CLAUDE.md) 及 [README.md](../README.md)。

提交 scope：`task`
