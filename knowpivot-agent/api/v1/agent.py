from fastapi import APIRouter
from schema.request import AgentRunRequest
from schema.response import AgentResponse, SourceReference
from fastapi.responses import StreamingResponse
import asyncio
import json

router = APIRouter()


def _sse(event: str, **kwargs) -> str:
    data = AgentResponse(event=event, **kwargs).model_dump(exclude_none=True)
    return f'event: {event}\ndata: {json.dumps(data)}\n\n'


@router.post("/run")
async def run_agent(request: AgentRunRequest):
    async def event_stream():
        # 1. message 事件 — 分块模拟流式输出
        for chunk in ["你好，", "我是AI助手，", "很高兴为你服务！"]:
            yield _sse("message", delta=chunk)
            await asyncio.sleep(0.15)

        # 2. references 事件
        yield _sse("references", sources=[
            SourceReference(
                docName="知识库文档.pdf",
                segmentId="seg_001",
                content="这是从知识库中检索到的相关内容片段。",
            ),
        ])
        await asyncio.sleep(0.1)

        # 3. done 事件
        yield _sse("done", messageId=request.sessionId, tokenCount=23, finishReason="stop")

    return StreamingResponse(
        event_stream(),
        media_type="text/event-stream"
    )