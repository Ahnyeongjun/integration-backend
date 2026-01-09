"""SQLAlchemy database models"""
from sqlalchemy import Column, Integer, String, DateTime, JSON, Index
from datetime import datetime
from src.database.session import Base


class RecommendationQuery(Base):
    """Store recommendation queries and results (for caching)"""
    __tablename__ = "recommendation_queries"

    id = Column(Integer, primary_key=True, index=True)
    query_hash = Column(String(64), unique=True, index=True, nullable=False)

    # Input parameters
    arm_length = Column(String(20), nullable=False)
    leg_length = Column(String(20), nullable=False)
    neck_length = Column(String(20), nullable=False)
    face_shape = Column(String(20), nullable=False)
    body_type = Column(String(20), nullable=False)

    # Result
    recommendation = Column(JSON, nullable=False)

    # Metadata
    created_at = Column(DateTime, default=datetime.utcnow, index=True)
    access_count = Column(Integer, default=0)
    last_accessed = Column(DateTime, default=datetime.utcnow)

    __table_args__ = (
        Index('idx_query_params', 'arm_length', 'leg_length', 'neck_length', 'face_shape', 'body_type'),
    )


class VenueQuery(Base):
    """Store venue recommendation queries and results (for caching)"""
    __tablename__ = "venue_queries"

    id = Column(Integer, primary_key=True, index=True)
    query_hash = Column(String(64), unique=True, index=True, nullable=False)

    # Input parameters
    guest_count = Column(String(20), nullable=False)
    budget = Column(String(20), nullable=False)
    region = Column(String(20), nullable=False)
    style_preference = Column(String(20), nullable=False)
    season = Column(String(20), nullable=False)

    # Result
    recommendation = Column(JSON, nullable=False)

    # Metadata
    created_at = Column(DateTime, default=datetime.utcnow, index=True)
    access_count = Column(Integer, default=0)
    last_accessed = Column(DateTime, default=datetime.utcnow)

    __table_args__ = (
        Index('idx_venue_params', 'guest_count', 'budget', 'region', 'style_preference', 'season'),
    )
