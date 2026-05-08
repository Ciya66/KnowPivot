from minio import Minio

from .config import settings

minio_client = Minio(
    settings.MINIO_ENDPOINT,
    access_key=settings.MINIO_ACCESS_KEY,
    secret_key=settings.MINIO_SECRET_KEY,
    secure=False,
)


def download_file(bucket_name: str, object_name: str, local_path: str):
    minio_client.fget_object(bucket_name, object_name, local_path)
