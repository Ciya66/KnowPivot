from fastapi import FastAPI

app = FastAPI(title="Python AI Agent")

from api.v1.agent import router as agent_router
app.include_router(agent_router, prefix="/api/v1/agent")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)