import os

from task.infrastructure.minio import download_file
from task.infrastructure.kafka import get_consumer
from task.infrastructure.embedding import get_embeddings
from task.infrastructure.server_client import submit_document_chunks
from task.core.document.parser import extract_text_from_file
from task.core.document.chunker import split_text


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
            index_name = data.get("indexName")
            kb_id = data.get("kbId")

            local_path = f"temp/{doc_id}/{file_name}"

            os.makedirs(f"temp/{doc_id}", exist_ok=True)
            download_file(bucket_name, storage_path, local_path)

            text = extract_text_from_file(local_path)
            chunks = split_text(text)

            # 使用向量模型为每个切片计算向量
            embeddings = get_embeddings(chunks)

            # 提交切片到 Java 后端服务器
            result = submit_document_chunks(doc_id, kb_id, index_name, chunks, embeddings)

            print(f"Document {doc_id}: {len(chunks)} chunks submitted to server, response={result}")

        except Exception as e:
            print(f"Error processing message: {e}")
            continue


if __name__ == "__main__":
    main()
