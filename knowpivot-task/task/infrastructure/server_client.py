import httpx

from .config import settings


def submit_document_chunks(
    doc_id: int,
    kb_id: int,
    index_name: str,
    chunks: list[str],
    embeddings: list[list[float]],
) -> dict:
    url = f"{settings.SERVER_API_URL}/api/v1/documents/indexing/callback"

    payload = {
        "docId": doc_id,
        "kbId": kb_id,
        "indexName": index_name,
        "chunks": [
            {"content": content, "embedding": embedding, "pageNum": i}
            for i, (content, embedding) in enumerate(zip(chunks, embeddings))
        ],
    }

    with httpx.Client(timeout=60.0) as client:
        response = client.post(url, json=payload)
        response.raise_for_status()
        return response.json()
