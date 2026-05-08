import json

from kafka import KafkaConsumer

from .config import settings


def get_consumer(topic: str, group_id: str) -> KafkaConsumer:
    return KafkaConsumer(
        topic,
        bootstrap_servers=settings.KAFKA_BROKER,
        group_id=group_id,
        auto_offset_reset="earliest",
        key_deserializer=lambda x: x.decode("utf-8") if x else None,
        value_deserializer=lambda x: json.loads(x.decode("utf-8")),
    )
