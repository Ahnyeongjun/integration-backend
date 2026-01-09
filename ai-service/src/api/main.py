"""FastAPI Application"""
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager

from src.database import init_db
from src.config import settings
from src.api.routes import dress_recommend, health, venue_recommend


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Application lifespan events"""
    # Startup
    await init_db()
    print("AI Service started")

    yield

    # Shutdown
    print("AI Service shutdown")


# FastAPI app
app = FastAPI(
    title="AI Recommendation Service",
    description="AI-powered wedding dress and venue recommendation service",
    version="1.0.0",
    lifespan=lifespan
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include routers
app.include_router(dress_recommend.router)
app.include_router(venue_recommend.router)
app.include_router(health.router)


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "src.api.main:app",
        host=settings.app_host,
        port=settings.app_port,
        reload=True
    )
