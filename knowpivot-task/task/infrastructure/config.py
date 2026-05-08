from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    KAFKA_BROKER: str = "localhost:9092"
    REDIS_HOST: str = "localhost"
    REDIS_PORT: int = 6379
    MINIO_ENDPOINT: str = "localhost:9000"
    MINIO_ACCESS_KEY: str = ""
    MINIO_SECRET_KEY: str = ""

    model_config = {"env_file": ".env", "env_file_encoding": "utf-8"}


settings = Settings()
