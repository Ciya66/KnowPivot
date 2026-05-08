from typing import List

import redis

from .config import settings

redis_client = redis.Redis(
    host=settings.REDIS_HOST,
    port=settings.REDIS_PORT,
    db=0,
    decode_responses=True,
    socket_timeout=5,
)


def save_document_embedding(
    file_id: str,
    chunk_index: int,
    content: str,
    embedding: List[float],
):
    key = f"document:embedding:{file_id}:{chunk_index}"
    embedding_str = ",".join(str(num) for num in embedding)
    redis_client.hset(
        key,
        mapping={
            "content": content,
            "embedding": embedding_str,
            "chunk_index": str(chunk_index),
        },
    )


def get_document_embeddings(file_id: str):
    keys = redis_client.keys(f"document:embedding:{file_id}:*")
    result = []
    for key in keys:
        data = redis_client.hgetall(key)
        embedding = [float(num) for num in data["embedding"].split(",")]
        result.append(
            {
                "content": data["content"],
                "embedding": embedding,
                "chunk_index": int(data["chunk_index"]),
            }
        )
    return result


def delete_document_embeddings(file_id: str):
    keys = redis_client.keys(f"document:embedding:{file_id}:*")
    if keys:
        redis_client.delete(*keys)
