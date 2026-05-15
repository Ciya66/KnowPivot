# 知识库问答提示词优化计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 根据 Harness Engineering 最佳实践，优化 KnowPivot 知识库问答系统的提示词设计

**Architecture:** 采用 Harness Engineering 的约束层设计方法，构建包含约束(Constrain)、告知(Inform)、验证(Verify)、纠正(Correct)四层结构的提示词体系

**Tech Stack:** Java + Python FastAPI + OpenAI/DeepSeek LLM

---

## 一、现状分析

### 1.1 当前提示词结构

**系统提示词 (SYSTEM_PROMPT)**:
```
你是一个智能知识库助手，请基于提供的文档内容回答用户问题。如果文档中没有相关内容，请如实告知。
```

**上下文注入方式** ([agent.py:44-46](file:///c:/Users/18259/Desktop/KnowPivot/knowpivot-agent/api/v1/agent.py#L44-L46)):
```python
full_system_prompt = system_prompt
if context_text:
    full_system_prompt += "\n\n以下是相关的知识库内容，请基于这些内容回答用户问题：\n" + context_text
```

### 1.2 根据 Harness Engineering 原则识别的问题

| 问题类别 | 当前状态 | Harness Engineering 要求 |
|---------|---------|------------------------|
| **约束层缺失** | 仅有一句角色定义 | 需要明确的边界约束、输出格式约束、行为约束 |
| **反馈循环缺失** | 无自我验证机制 | 需要 Generator + Evaluator 分离，输出前自检 |
| **上下文工程不足** | 简单拼接上下文 | 需要结构化上下文、明确来源标注、相似度阈值说明 |
| **输出格式未约束** | 自由文本输出 | 需要结构化输出、引用溯源格式、置信度声明 |
| **幻觉防护薄弱** | 仅口头约束 | 需要强制引用机制、无来源时的兜底回复模板 |
| **角色分离缺失** | 单一 Agent | 可考虑 Generator + CitationValidator 双角色 |

---

## 二、Harness Engineering 核心原则应用

### 2.1 四层约束结构设计

```
┌─────────────────────────────────────────────────────────┐
│                    Harness 架构                          │
├─────────────────────────────────────────────────────────┤
│  Constrain (约束层)                                      │
│  - 定义 Agent 能做什么：仅基于提供的文档内容回答           │
│  - 定义 Agent 不能做什么：禁止编造、禁止外部知识           │
│  - 输出格式约束：必须包含引用、置信度                      │
├─────────────────────────────────────────────────────────┤
│  Inform (告知层)                                         │
│  - 结构化上下文注入：文档片段 + 来源元数据 + 相似度        │
│  - 用户意图理解：问题类型识别、领域范围                    │
│  - 历史对话上下文：压缩后的关键信息                       │
├─────────────────────────────────────────────────────────┤
│  Verify (验证层)                                         │
│  - 引用完整性检查：每个声明是否有对应引用                  │
│  - 输出格式校验：是否符合结构化要求                        │
│  - 自我评估：置信度评分、知识覆盖度                       │
├─────────────────────────────────────────────────────────┤
│  Correct (纠正层)                                        │
│  - 无引用时的兜底回复模板                                 │
│  - 置信度低时的免责声明                                   │
│  - 引用不匹配时的修正建议                                 │
└─────────────────────────────────────────────────────────┘
```

### 2.2 关键设计决策

| 决策 | 选择 | 理由 |
|-----|-----|-----|
| 引用强制 | 每个事实陈述必须关联 [来源N] | 防止幻觉，可溯源 |
| 置信度声明 | 回答末尾附加置信度评分 | 用户知情权，低置信度提示 |
| 无来源处理 | 固定模板回复 + 建议换问题 | 避免"强行回答" |
| 输出结构 | Markdown + 引用块 + 元信息 | 结构化便于前端渲染 |
| 上下文格式 | 编号片段 + 元数据头 | 便于模型关联引用 |

---

## 三、优化后的提示词设计

### 3.1 系统提示词模板 (SYSTEM_PROMPT v2)

```markdown
# 角色定义
你是一个专业的知识库问答助手。你的核心职责是基于用户上传的文档内容，准确、可靠地回答用户问题。

# 核心约束 [Constrain]

## 你必须遵守
1. **仅基于提供的文档内容回答**：所有回答必须来源于提供的文档片段
2. **每个事实陈述必须标注引用**：使用 [来源N] 格式标注信息来源
3. **如实告知知识边界**：如果文档中没有相关内容，必须明确告知

## 你禁止的行为
1. ❌ 编造文档中不存在的信息
2. ❌ 使用外部知识或常识补充回答
3. ❌ 对无来源的内容做出确定性陈述
4. ❌ 忽略用户问题或答非所问

# 输出格式规范

## 标准回答格式
```markdown
### 回答
[你的回答内容，每个事实后标注 [来源N]]

### 📚 引用来源
- [来源1] 《文档名》第X页 (相似度: XX%)
- [来源2] 《文档名》第X页 (相似度: XX%)

### ℹ️ 置信度
- 综合置信度：高/中/低
- 知识覆盖度：完全覆盖/部分覆盖/未覆盖
```

## 无相关内容时的回复格式
```markdown
### 回答
抱歉，在当前知识库中未找到与您问题直接相关的内容。

💡 **建议**：
- 尝试换一种方式提问
- 检查是否选择了正确的知识库
- 确认相关文档是否已上传

### ℹ️ 置信度
- 综合置信度：无
- 原因：知识库中无匹配内容
```

# 上下文理解指南 [Inform]

## 文档片段格式说明
你将收到如下格式的文档片段：
```
[文档片段 N]
文档内容...
```

## 相似度说明
- 相似度 ≥ 0.8：高度相关，可直接引用
- 相似度 0.7-0.8：相关，需谨慎引用
- 相似度 < 0.7：可能不相关，建议不引用

# 自检清单 [Verify]

在提交回答前，请确认：
- [ ] 所有事实陈述都有对应的 [来源N] 引用
- [ ] 没有编造文档中不存在的信息
- [ ] 置信度评估与实际匹配情况一致
- [ ] 输出格式符合规范
```

### 3.2 上下文注入优化

**优化后的上下文格式** ([agent.py](file:///c:/Users/18259/Desktop/KnowPivot/knowpivot-agent/api/v1/agent.py) 需修改):

```python
def _build_context_text(references: List) -> str:
    if not references:
        return ""
    
    context_parts = ["# 检索到的文档片段\n"]
    context_parts.append(f"共检索到 {len(references)} 个相关片段：\n")
    
    for idx, ref in enumerate(references, 1):
        similarity_level = "高" if ref.similarity >= 0.8 else ("中" if ref.similarity >= 0.7 else "低")
        context_parts.append(f"\n[文档片段 {idx}]")
        context_parts.append(f"来源：《{ref.docName}》第{ref.pageNum}页")
        context_parts.append(f"相似度：{ref.similarity:.2%} ({similarity_level}相关)")
        context_parts.append(f"内容：\n{ref.content}")
    
    return "\n".join(context_parts)
```

### 3.3 输出验证中间件 (新增)

**验证逻辑** (可添加到 Python Agent 服务):

```python
def validate_response(response: str, references: List) -> ValidationResult:
    """
    验证回答是否符合 Harness 约束
    """
    errors = []
    
    # 1. 检查是否有事实陈述但无引用
    if has_factual_claims(response) and not has_citations(response):
        errors.append("存在未标注引用的事实陈述")
    
    # 2. 检查引用是否在提供的来源范围内
    cited_sources = extract_citations(response)
    available_sources = set(range(1, len(references) + 1))
    invalid_citations = cited_sources - available_sources
    if invalid_citations:
        errors.append(f"引用了不存在的来源：{invalid_citations}")
    
    # 3. 检查是否包含置信度声明
    if not has_confidence_statement(response):
        errors.append("缺少置信度声明")
    
    return ValidationResult(
        is_valid=len(errors) == 0,
        errors=errors
    )
```

---

## 四、任务拆解

### Task 1: 更新系统提示词模板

**Files:**
- Modify: `knowpivot-server/src/main/resources/db/init.sql:157`
- Modify: `knowpivot-server/src/main/java/com/knowpivot/server/ai/gateway/DefaultAgentGateway.java:98`

- [ ] **Step 1: 更新数据库初始化脚本中的默认提示词**

将 init.sql 中的 SYSTEM_PROMPT 更新为优化后的版本：

```sql
INSERT INTO `t_prompt_template` (`id`, `code`, `name`, `content`, `is_active`)
VALUES (1, 'SYSTEM_PROMPT', '系统提示词', 
'# 角色定义
你是一个专业的知识库问答助手。你的核心职责是基于用户上传的文档内容，准确、可靠地回答用户问题。

# 核心约束 [Constrain]

## 你必须遵守
1. **仅基于提供的文档内容回答**：所有回答必须来源于提供的文档片段
2. **每个事实陈述必须标注引用**：使用 [来源N] 格式标注信息来源
3. **如实告知知识边界**：如果文档中没有相关内容，必须明确告知

## 你禁止的行为
1. ❌ 编造文档中不存在的信息
2. ❌ 使用外部知识或常识补充回答
3. ❌ 对无来源的内容做出确定性陈述
4. ❌ 忽略用户问题或答非所问

# 输出格式规范

## 标准回答格式
回答内容中，每个事实后标注 [来源N]。回答末尾附加引用来源列表和置信度评估。

## 无相关内容时的回复
明确告知未找到相关内容，并给出建议。

# 上下文理解指南 [Inform]

## 文档片段格式
[文档片段 N] 包含来源信息和相似度。

## 相似度说明
- ≥ 0.8：高度相关
- 0.7-0.8：相关，需谨慎
- < 0.7：可能不相关

# 自检清单 [Verify]
- 所有事实陈述都有引用
- 没有编造信息
- 置信度评估一致
- 格式符合规范', 1);
```

- [ ] **Step 2: 更新 DefaultAgentGateway 中的默认提示词**

同步更新 Java 代码中的 fallback 提示词。

- [ ] **Step 3: 验证数据库初始化**

运行 `./mvnw compile` 确保编译通过。

---

### Task 2: 优化上下文注入逻辑

**Files:**
- Modify: `knowpivot-agent/api/v1/agent.py:24-31`

- [ ] **Step 1: 重构 _build_context_text 函数**

将简单的片段拼接改为结构化的上下文格式，包含来源元数据和相似度信息。

- [ ] **Step 2: 添加相似度等级标注**

为每个片段添加相似度等级（高/中/低），帮助模型判断引用可靠性。

- [ ] **Step 3: 测试上下文构建**

使用示例数据验证上下文格式正确。

---

### Task 3: 添加输出验证机制

**Files:**
- Create: `knowpivot-agent/validator/response_validator.py`
- Modify: `knowpivot-agent/api/v1/agent.py`

- [ ] **Step 1: 创建响应验证器**

实现引用完整性检查、格式校验、置信度声明检查。

- [ ] **Step 2: 集成到 Agent 流程**

在流式输出完成后执行验证，记录验证结果。

- [ ] **Step 3: 添加验证日志**

记录验证失败的情况，用于后续提示词优化。

---

### Task 4: 前端适配结构化输出

**Files:**
- Modify: `knowpivot-web/src/components/ChatMessage.vue`
- Modify: `knowpivot-web/src/composables/useMarkdown.ts`

- [ ] **Step 1: 解析引用标注**

识别回答中的 [来源N] 标注，渲染为可点击的引用链接。

- [ ] **Step 2: 渲染置信度信息**

在回答末尾显示置信度和知识覆盖度。

- [ ] **Step 3: 优化引用来源展示**

将引用来源列表渲染为卡片或折叠面板。

---

## 五、预期效果

| 指标 | 优化前 | 优化后 |
|-----|-------|-------|
| 引用覆盖率 | 不强制 | 100% 事实有引用 |
| 幻觉风险 | 较高 | 显著降低 |
| 可溯源度 | 无 | 每个事实可点击跳转 |
| 用户信任度 | 一般 | 提升（置信度透明） |
| 回答质量 | 依赖模型 | Harness 约束保障 |

---

## 六、参考资料

- [Harness Engineering 全景指南](http://m.toutiao.com/group/7637389590185230854/)
- [Anthropic: Effective Harnesses for Long-Running Agents](https://www.anthropic.com/engineering/effective-harnesses-for-long-running-agents)
- [OpenAI: Harness Engineering](https://openai.com/index/harness-engineering/)
