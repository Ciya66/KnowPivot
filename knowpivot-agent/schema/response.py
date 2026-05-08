from pydantic import BaseModel
from typing import List

class SourceReference(BaseModel):
    docName: str | None = None
    segmentId: str | None = None
    content: str | None = None
    pageNum: int | None = None

class AgentResponse(BaseModel):
    """
    智能体响应模型
    """
    # 事件类型：message / references / done / error
    event: str

    # 流式增量内容
    delta: str | None = None

    # 引用来源
    sources: List[SourceReference] | None = None

    # 结束时的字段
    messageId: str | None = None
    tokenCount: int | None = None
    finishReason: str | None = None

    # 错误信息
    errorMessage: str | None = None