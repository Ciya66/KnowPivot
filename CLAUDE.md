# CLAUDE.md

本文件指导 Claude Code 在 KnowPivot 单仓库中的 Git 工作流。代码规范见各子项目 CLAUDE.md。

## 仓库初始化

项目尚未初始化 Git 仓库，首次使用需执行：

```bash
cd KnowPivot
git init
git checkout -b main
```

初始化后立即创建根目录 `.gitignore`，再进行首次提交。子项目已有的 `.gitignore` 会自动生效，无需额外配置。

## 分支策略

| 分支 | 用途 | 命名规则 | 保护 |
|------|------|----------|------|
| `main` | 生产就绪代码 | — | 禁止直接推送 |
| `develop` | 开发集成分支 | — | 禁止直接推送 |
| `feature/*` | 功能开发 | `feature/<简述>` | — |
| `fix/*` | 缺陷修复 | `fix/<简述>` | — |
| `docs/*` | 文档更新 | `docs/<简述>` | — |

```bash
# 从 develop 创建功能分支
git checkout develop
git pull origin develop
git checkout -b feature/知识库CRUD
```

## 提交规范

格式遵循 Conventional Commits：

```
<type>(<scope>): <简要描述>

<详细说明（可选）>
```

### Type

| Type | 说明 | 示例 |
|------|------|------|
| `feat` | 新功能 | `feat(server): 添加知识库CRUD接口` |
| `fix` | 修复 Bug | `fix(web): 修复侧边栏折叠动画闪烁` |
| `docs` | 文档变更 | `docs: 更新API接口文档` |
| `refactor` | 重构（不改变行为） | `refactor(server): 拆分ChatApplicationService` |
| `style` | 格式调整（不影响逻辑） | `style(web): 统一组件缩进为2空格` |
| `test` | 测试相关 | `test(server): 添加DocumentService单元测试` |
| `chore` | 构建/依赖/工具变更 | `chore(server): 升级MyBatis-Plus至3.5.9` |
| `perf` | 性能优化 | `perf(web): 懒加载DashboardView` |

### Scope

`server`（knowpivot-server）、`web`（knowpivot-web）、`ai-op`（文档）、`docs`（项目文档）、`root`（仓库级变更）

### 规则

- subject 行不超过 50 字符
- 使用中文描述
- 每个提交只做一件事（原子提交）
- `feat` 提交不应包含不相关的格式修改

## 各子项目提交要求

### knowpivot-server

- `./mvnw compile` 必须通过
- 涉及数据库变更时，需同时提交 `src/main/resources/db/` 下的 SQL 文件
- 不提交 `target/`、`.idea/` 等生成文件（由 `.gitignore` 保证）

### knowpivot-web

- `npm run type-check` 必须通过
- 新组件需同时包含 dark mode 样式
- 不提交 `node_modules/`、`dist/` 等生成文件（由 `.gitignore` 保证）

### ai-op

- 纯文档目录，无需构建验证
- 文件命名保持现有数字前缀格式（如 `6.新文档.md`）

## 提交前检查清单

提交前必须确认：

- [ ] 相关子项目的构建命令通过（server: `./mvnw compile`，web: `npm run type-check`）
- [ ] 没有误提交密钥、凭证或 `.env` 文件
- [ ] 提交信息符合规范（type(scope): 中文描述）
- [ ] 每个提交是原子的，只包含一个逻辑变更
- [ ] 涉及数据库变更时，SQL 文件已包含在提交中
- [ ] 新增文件不在 `.gitignore` 规则中

## 推送工作流

### 开发阶段（推送到远程功能分支）

```bash
git add <具体文件>              # 禁止 git add .
git status                      # 确认暂存区内容
git commit -m "feat(server): 添加xxx"
git push origin feature/知识库CRUD
# 在 GitHub/GitLab 创建 PR → develop
```

### 生产发布（合并到 main）

```bash
git checkout main
git merge --no-ff develop
git tag -a v0.1.0 -m "v0.1.0: 知识库基础功能"
git push origin main --tags
```

### 关键规则

- 禁止直接向 `main` 或 `develop` 推送，必须通过 PR/MR
- `--no-ff` 合并保留分支历史
- 推送前确认远程分支是最新的（`git pull --rebase`）

## 版本标签

使用语义化版本：`v<major>.<minor>.<patch>`

| 变更类型 | 版本变更 | 示例 |
|----------|----------|------|
| 重大架构变更 / 不兼容变更 | `major` | v1.0.0 → v2.0.0 |
| 新增功能模块 | `minor` | v0.1.0 → v0.2.0 |
| Bug 修复 / 小调整 | `patch` | v0.1.0 → v0.1.1 |

```bash
git tag -a v0.1.0 -m "v0.1.0: 知识库基础CRUD + RAG问答"
```

## 单仓库协作要点

1. **一次提交只涉及一个子项目** — 避免混合 server 和 web 的变更
2. **跨子项目功能使用同一分支名** — 如 `feature/知识库CRUD` 同时修改 server 和 web 时，分支名保持一致
3. **PR 描述中注明涉及哪些子项目** — 使用标签 `[server]`、`[web]`、`[ai-op]`
4. **合并顺序** — 先合并 server PR，再合并 web PR（前端依赖后端接口）
5. **CHANGELOG** — 在仓库根目录维护 `CHANGELOG.md`，记录每个版本的变更摘要

## Claude Code 操作规范

1. **提交前验证构建**：`knowpivot-server` 变更 → 运行 `./mvnw compile`；`knowpivot-web` 变更 → 运行 `npm run type-check`
2. **暂存文件**：使用 `git add <具体文件>` 逐个添加，**禁止** `git add .` 或 `git add -A`
3. **提交信息**：严格遵循 Conventional Commits 中文格式
4. **禁止操作**：未经用户明确要求，不得执行 `git push`、`git reset --hard`、`git clean -f`
5. **首次提交**：仓库未初始化时，先完成 `git init` + `.gitignore` 创建，再提交代码
6. **分支管理**：新功能从 `develop` 创建 `feature/*` 分支；修复从 `develop` 创建 `fix/*` 分支
