import httpx
from fastapi import APIRouter, HTTPException

from infrastructure.config import settings
from schema.request import EmbeddingRequest
from schema.response import EmbeddingResponse, EmbeddingData, UsageInfo

router = APIRouter()


@router.post("/embeddings", response_model=EmbeddingResponse)
async def create_embeddings(request: EmbeddingRequest):
    url = f"{settings.EMBEDDING_BASE_URL}/embeddings"

    headers = {"Content-Type": "application/json"}
    if settings.EMBEDDING_API_KEY:
        headers["Authorization"] = f"Bearer {settings.EMBEDDING_API_KEY}"

    payload = {
        "model": request.model,
        "prompt": request.input[0],
        "dimensions": settings.EMBEDDING_DIMENSION,
    }

    async with httpx.AsyncClient(timeout=60.0) as client:
        try:
            response = await client.post(url, json=payload, headers=headers)
            response.raise_for_status()
        except httpx.HTTPStatusError as e:
            raise HTTPException(
                status_code=e.response.status_code,
                detail=f"上游 Embedding 服务返回错误: {e.response.text}",
            )
        except httpx.RequestError as e:
            raise HTTPException(
                status_code=502,
                detail=f"无法连接上游 Embedding 服务: {e}",
            )

    upstream = response.json()

    if "data" in upstream:
        # 上游返回 list
        data = [
            EmbeddingData(
                embedding=item["embedding"],
                index=item.get("index", i)
            )
            for i, item in enumerate(upstream["data"])
        ]
    elif "embedding" in upstream:
        # 上游返回单个 embedding
        data = [EmbeddingData(embedding=upstream["embedding"], index=0)]
    else:
        data = []

    usage_raw = upstream.get("usage", {})
    usage = UsageInfo(
        prompt_tokens=usage_raw.get("prompt_tokens", 0),
        total_tokens=usage_raw.get("total_tokens", 0),
    )

    return EmbeddingResponse(
        data=data,
        model=upstream.get("model", request.model),
        usage=usage,
    )
