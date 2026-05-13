from fastapi import APIRouter
from schema.request import AgentRunRequest
from schema.response import AgentResponse, SourceReference as ResponseSourceReference
from fastapi.responses import StreamingResponse
from infrastructure.config import settings
from openai import AsyncOpenAI
import json
from typing import AsyncGenerator, List

router = APIRouter()

# 初始化 OpenAI 客户端
openai_client = AsyncOpenAI(
    api_key=settings.OPENAI_API_KEY,
    base_url=settings.OPENAI_BASE_URL
)


def _sse(event: str, **kwargs) -> str:
    data = AgentResponse(event=event, **kwargs).model_dump(exclude_none=True)
    return f'event: {event}\ndata: {json.dumps(data, ensure_ascii=False)}\n\n'


def _build_context_text(references: List) -> str:
    if not references:
        return ""
    context_parts = []
    for idx, ref in enumerate(references, 1):
        context_parts.append(f"[文档片段 {idx}]")
        context_parts.append(f"{ref.content}")
    return "\n\n".join(context_parts)


async def _call_llm_stream(
    system_prompt: str,
    query: str,
    history: List,
    context_text: str
) -> AsyncGenerator[str, None]:
    messages = []
    
    # System prompt
    full_system_prompt = system_prompt
    if context_text:
        full_system_prompt += "\n\n以下是相关的知识库内容，请基于这些内容回答用户问题：\n" + context_text
    messages.append({"role": "system", "content": full_system_prompt})
    
    # History
    if history:
        for msg in history:
            messages.append({"role": msg.role, "content": msg.content})
    
    # User query
    messages.append({"role": "user", "content": query})
    
    stream = await openai_client.chat.completions.create(
        model=settings.OPENAI_MODEL,
        messages=messages,
        stream=True
    )
    
    prompt_tokens = 0
    completion_tokens = 0
    
    async for chunk in stream:
        if chunk.choices and chunk.choices[0].delta.content:
            yield chunk.choices[0].delta.content
        if chunk.usage:
            prompt_tokens = chunk.usage.prompt_tokens or 0
            completion_tokens = chunk.usage.completion_tokens or 0
            total_tokens = prompt_tokens + completion_tokens
            yield {"total": total_tokens, "prompt": prompt_tokens, "completion": completion_tokens}


@router.post("/run")
async def run_agent(request: AgentRunRequest):
    async def event_stream():
        try:
            # 1. 发送 references 事件（先发送）
            if request.references:
                sources = [
                    ResponseSourceReference(
                        docName=f"文档_{ref.docId}" if ref.docId else None,
                        segmentId=ref.segmentId,
                        content=ref.content,
                        pageNum=ref.pageNum
                    )
                    for ref in request.references
                ]
                yield _sse("references", sources=sources)
            
            # 2. 构建上下文
            context_text = _build_context_text(request.references)
            system_prompt = request.systemPrompt
            
            # 3. 调用 LLM 流式输出
            total_tokens = 0
            async for token in _call_llm_stream(
                system_prompt=system_prompt,
                query=request.query,
                history=request.history,
                context_text=context_text
            ):
                if isinstance(token, str):
                    yield _sse("message", delta=token)
                elif isinstance(token, dict):
                    total_tokens = token.get("total", 0)
            
            # 4. done 事件
            yield _sse("done", messageId=request.sessionId, tokenCount=total_tokens, finishReason="stop")
            
        except Exception as e:
            yield _sse("error", errorMessage=str(e))
    
    return StreamingResponse(
        event_stream(),
        media_type="text/event-stream"
    )