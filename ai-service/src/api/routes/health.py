"""Health check routes"""
from fastapi import APIRouter, HTTPException
from sqlalchemy import text
from src.database import AsyncSessionLocal

router = APIRouter(prefix="/health", tags=["health"])


@router.get("/")
async def health_check():
    """Basic health check endpoint"""
    return {
        "status": "healthy",
        "message": "API is running"
    }


@router.get("/db")
async def database_health():
    """Database connection test endpoint"""
    result = {
        "mysql": {"status": "unknown", "message": ""}
    }

    try:
        async with AsyncSessionLocal() as db:
            await db.execute(text("SELECT 1"))
            result["mysql"]["status"] = "connected"
            result["mysql"]["message"] = "MySQL connection successful"
    except Exception as e:
        result["mysql"]["status"] = "failed"
        result["mysql"]["message"] = f"MySQL connection failed: {str(e)}"

    all_connected = all(
        service["status"] == "connected"
        for service in result.values()
    )

    if not all_connected:
        raise HTTPException(status_code=503, detail=result)

    return {
        "status": "healthy",
        "services": result
    }
