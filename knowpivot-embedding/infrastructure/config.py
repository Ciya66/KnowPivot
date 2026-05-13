from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    EMBEDDING_BASE_URL: str = "http://localhost:11434/api"
    EMBEDDING_API_KEY: str = ""
    EMBEDDING_MODEL: str = "qwen3-embedding:0.6b"
    EMBEDDING_DIMENSION: int = 1536
    SERVER_PORT: int = 8001

    model_config = {"env_file": ".env", "env_file_encoding": "utf-8"}


settings = Settings()
