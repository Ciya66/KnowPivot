from pydantic import BaseModel


class EmbeddingRequest(BaseModel):
    model: str = "qwen3-embedding:0.6b"
    input: list[str]
