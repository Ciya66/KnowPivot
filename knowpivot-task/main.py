import os

from task.infrastructure.minio import download_file
from task.infrastructure.kafka import get_consumer
from task.core.document.parser import extract_text_from_file


def main():
    consumer = get_consumer("document-parsing", "python-document-group")
    bucket_name = "knowpivot"

    print("knowpivot-task consumer started, waiting for messages...")

    for message in consumer:
        try:
            data = message.value

            doc_id = data.get("docId")
            storage_path = data.get("storagePath")
            file_name = data.get("fileName")

            local_path = f"temp/{doc_id}/{file_name}"

            os.makedirs(f"temp/{doc_id}", exist_ok=True)
            download_file(bucket_name, storage_path, local_path)

            text = extract_text_from_file(local_path)

            # TODO: text chunking, embedding generation, Redis storage



        except Exception as e:
            print(f"Error processing message: {e}")
            continue


if __name__ == "__main__":
    main()
