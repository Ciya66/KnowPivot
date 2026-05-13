import httpx

from .config import settings


def get_embeddings(texts: list[str]) -> list[list[float]]:
    url = f"{settings.EMBEDDING_BASE_URL}/embeddings"

    headers = {"Content-Type": "application/json"}
    if settings.EMBEDDING_API_KEY:
        headers["Authorization"] = f"Bearer {settings.EMBEDDING_API_KEY}"

    response = httpx.post(
        url,
        json={"model": settings.EMBEDDING_MODEL, "input": texts},
        headers=headers,
        timeout=60.0,
    )
    response.raise_for_status()

    data = response.json()
    return [item["embedding"] for item in data["data"]]
