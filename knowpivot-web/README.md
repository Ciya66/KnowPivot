# knowpivot-web

知枢前端 SPA — Vue 3 + TypeScript 构建。

## 技术栈

| 类别 | 技术 |
|------|------|
| 框架 | Vue 3.5 |
| 语言 | TypeScript |
| 构建 | Vite |
| 路由 | Vue Router 5 |
| 状态管理 | Pinia 3 |
| UI 组件 | Element Plus 2.13 |
| HTTP | Axios |
| Markdown | Marked 18 |
| 样式 | 纯 CSS Custom Properties |
| 包管理 | npm |

## 页面路由

| 路径 | 页面 | 说明 |
|------|------|------|
| `/login` | LoginView | 用户登录 |
| `/` | ChatView | AI 对话（SSE 流式） |
| `/knowledge` | KnowledgeView | 知识库列表 |
| `/knowledge/:kbId` | KnowledgeDetailView | 知识库详情与文档管理 |
| `/settings` | SettingsView | 用户设置 |

所有路由（除 `/login` 外）均需认证，未登录自动跳转登录页。

## 架构

### 三栏布局

```
┌──────────┬──────────────────────────┬────────────┐
│ SideNav  │     Main Content         │ RightPanel │
│  240px   │     flex: 1              │  320px     │
│ 可折叠    │                          │ 条件显示   │
└──────────┴──────────────────────────┴────────────┘
```

### 状态管理

| Store | 说明 |
|-------|------|
| `app` | 主题与侧边栏状态 |
| `auth` | 登录状态与用户信息 |
| `chat` | 会话列表与消息管理 |
| `knowledge` | 知识库数据 |
| `document` | 文档列表 |

### API 封装

| 模块 | 文件 | 说明 |
|------|------|------|
| Auth | `api/auth.ts` | 注册/登录 |
| Chat | `api/chat.ts` | 会话/消息/SSE 流式 |
| Knowledge | `api/knowledge.ts` | 知识库 CRUD |
| HTTP | `utils/request.ts` | Axios 封装 + Token 管理 |
| SSE | `utils/sse.ts` | SSE 流式客户端 |

## 常用命令

```bash
npm install          # 安装依赖
npm run dev          # 启动开发服务器（端口 5173）
npm run build        # 类型检查 + 生产构建
npm run type-check   # TypeScript 类型检查
npm run preview      # 预览生产构建
```

## 样式规范

- 设计令牌：`src/styles/variables.css`（颜色、间距、圆角、阴影、字体）
- 全局重置：`src/styles/global.css`
- 过渡动画：`src/styles/transitions.css`
- 暗黑模式：`[data-theme='dark']` 选择器
- 组件样式：全部 `<style scoped>`，引用 CSS 变量

## 所属仓库

本项目属于 KnowPivot 单仓库，详见根目录 [CLAUDE.md](../CLAUDE.md) 及 [README.md](../README.md)。
