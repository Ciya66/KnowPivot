from openai import OpenAI

from .config import settings

client = OpenAI(
    base_url=settings.EMBEDDING_BASE_URL,
    api_key=settings.EMBEDDING_API_KEY,
)


def get_embeddings(texts: list[str]) -> list[list[float]]:
    response = client.embeddings.create(
        model=settings.EMBEDDING_MODEL,
        input=texts,
    )
    return [item.embedding for item in response.data]
