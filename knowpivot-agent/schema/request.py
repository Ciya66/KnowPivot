from pydantic import BaseModel, Field
from typing import List, Dict, Any

class ChatMessage(BaseModel):
    role: str
    content: str

class AgentContext(BaseModel):
    conversationId: int | None = None
    kbId: int | None = None
    indexName: str | None = None
    history: List[ChatMessage] | None = None
    config: Dict[str, Any] | None = None

class AgentRunRequest(BaseModel):
    """
    运行智能体请求模型
    """
    sessionId: str
    query: str
    history: List[ChatMessage] | None = None
    config: Dict[str, Any] | None = None
    indexName: str | None = None