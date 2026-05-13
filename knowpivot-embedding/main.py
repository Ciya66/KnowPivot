import uvicorn
from fastapi import FastAPI

from api.v1.embedding import router as embedding_router
from infrastructure.config import settings

app = FastAPI(title="KnowPivot Embedding Service")
app.include_router(embedding_router, prefix="/v1")

if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=settings.SERVER_PORT, reload=True)
