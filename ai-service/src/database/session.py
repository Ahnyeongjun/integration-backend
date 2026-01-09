"""Database session management"""
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession, async_sessionmaker
from sqlalchemy.orm import declarative_base
from src.config import settings

# SQLAlchemy setup with aiomysql
engine = create_async_engine(
    settings.mysql_url,
    echo=True,
    pool_size=10,
    max_overflow=20
)

AsyncSessionLocal = async_sessionmaker(
    engine,
    class_=AsyncSession,
    expire_on_commit=False
)

Base = declarative_base()


async def get_db():
    """Get database session"""
    async with AsyncSessionLocal() as session:
        try:
            yield session
        finally:
            await session.close()


async def init_db():
    """Initialize database tables"""
    from src.database.models import RecommendationQuery, VenueQuery
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
