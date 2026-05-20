# knowpivot-agent

知枢 AI Agent 服务 — Python + FastAPI，负责流式对话与标题生成。

## 技术栈

| 类别 | 技术 |
|------|------|
| 框架 | FastAPI 0.136 |
| 语言 | Python 3.13 |
| 包管理 | uv |
| LLM SDK | OpenAI SDK（兼容接口） |
| 协议 | SSE 流式输出 |

## API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/agent/run` | 流式对话（SSE） |
| POST | `/api/v1/agent/generate-title` | 生成对话标题 |

### POST /api/v1/agent/run

接收用户查询与知识库引用，调用 LLM 流式返回回答。

SSE 事件格式：

| 事件 | 说明 |
|------|------|
| `references` | 知识库引用来源 |
| `message` | 增量回答内容（多个） |
| `done` | 回答结束，含 Token 统计 |
| `error` | 错误信息 |

### POST /api/v1/agent/generate-title

根据用户首条消息自动生成对话标题（10-30 字符）。

## 数据流

```
knowpivot-server → HTTP POST /api/v1/agent/run
  → Agent 构建 System Prompt + 历史 + 引用上下文
  → OpenAI SDK → LLM 流式调用
  → SSE 事件 → 客户端接收
```

## 常用命令

```bash
uv sync                   # 安装依赖
uv run python main.py     # 启动服务（端口 8000）
uv run pytest             # 运行测试
```

## 环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `OPENAI_API_KEY` | — | API 密钥 |
| `OPENAI_BASE_URL` | — | 兼容接口地址（如 DeepSeek） |
| `OPENAI_MODEL` | `gpt-4o` | 模型名称 |

## 调用方

- `knowpivot-server`：通过 `ai.gateway.base-url=http://localhost:8000` 调用

## 所属仓库

本项目属于 KnowPivot 单仓库，详见根目录 [CLAUDE.md](../CLAUDE.md) 及 [README.md](../README.md)。

提交 scope：`agent`
