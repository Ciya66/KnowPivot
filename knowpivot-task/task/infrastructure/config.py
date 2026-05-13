from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    KAFKA_BROKER: str = "localhost:9092"
    MINIO_ENDPOINT: str = "localhost:9000"
    MINIO_ACCESS_KEY: str = ""
    MINIO_SECRET_KEY: str = ""
    EMBEDDING_BASE_URL: str = "http://localhost:8001/v1"
    EMBEDDING_API_KEY: str = ""
    EMBEDDING_MODEL: str = "text-embedding-3-small"
    EMBEDDING_DIMENSION: int = 1536
    SERVER_API_URL: str = "http://localhost:8080"

    model_config = {"env_file": ".env", "env_file_encoding": "utf-8"}


settings = Settings()
