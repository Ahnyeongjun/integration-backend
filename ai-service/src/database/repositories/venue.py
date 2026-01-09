"""Venue recommendation repository"""
from sqlalchemy import select, update
from sqlalchemy.ext.asyncio import AsyncSession
from datetime import datetime
from typing import Optional

from src.database.models import VenueQuery


class VenueRepository:
    """Repository for venue recommendation queries"""

    async def get_by_hash(self, db: AsyncSession, query_hash: str) -> Optional[VenueQuery]:
        """Get recommendation by hash"""
        result = await db.execute(
            select(VenueQuery).where(VenueQuery.query_hash == query_hash)
        )
        record = result.scalar_one_or_none()

        if record:
            # Update access count and last accessed time
            await db.execute(
                update(VenueQuery)
                .where(VenueQuery.query_hash == query_hash)
                .values(
                    access_count=VenueQuery.access_count + 1,
                    last_accessed=datetime.utcnow()
                )
            )
            await db.commit()

        return record

    async def create(
        self,
        db: AsyncSession,
        query_hash: str,
        guest_count: str,
        budget: str,
        region: str,
        style_preference: str,
        season: str,
        recommendation: dict
    ) -> VenueQuery:
        """Create new recommendation record"""
        db_record = VenueQuery(
            query_hash=query_hash,
            guest_count=guest_count,
            budget=budget,
            region=region,
            style_preference=style_preference,
            season=season,
            recommendation=recommendation
        )
        db.add(db_record)
        await db.commit()
        await db.refresh(db_record)
        return db_record


# Global repository instance
venue_repo = VenueRepository()
