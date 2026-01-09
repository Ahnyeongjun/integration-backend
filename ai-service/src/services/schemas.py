"""Pydantic schemas for request/response"""
from pydantic import BaseModel, Field
from enum import Enum
from typing import List, Optional
from datetime import date, datetime
from decimal import Decimal


class ArmLength(str, Enum):
    SHORT = "short"
    MEDIUM = "medium"
    LONG = "long"


class LegLength(str, Enum):
    SHORT = "short"
    MEDIUM = "medium"
    LONG = "long"


class NeckLength(str, Enum):
    SHORT = "short"
    MEDIUM = "medium"
    LONG = "long"


class FaceShape(str, Enum):
    OVAL = "oval"
    WIDE = "wide"
    ANGULAR = "angular"
    LONG = "long"


class BodyType(str, Enum):
    THIN = "thin"
    MEDIUM = "medium"
    HEAVY = "heavy"


class RecommendationRequest(BaseModel):
    """Wedding dress recommendation request"""
    arm_length: ArmLength = Field(..., description="Arm length")
    leg_length: LegLength = Field(..., description="Leg length")
    neck_length: NeckLength = Field(..., description="Neck length")
    face_shape: FaceShape = Field(..., description="Face shape")
    body_type: BodyType = Field(..., description="Body type")
    num_recommendations: int = Field(
        default=3,
        ge=1,
        le=5,
        description="Number of recommendations (1-5)"
    )

    class Config:
        json_schema_extra = {
            "example": {
                "arm_length": "medium",
                "leg_length": "long",
                "neck_length": "medium",
                "face_shape": "oval",
                "body_type": "medium",
                "num_recommendations": 3
            }
        }


class DressRecommendation(BaseModel):
    """Single dress recommendation"""
    style_name: str = Field(..., description="Dress style name")
    description: str = Field(..., description="Dress description")
    why_recommended: str = Field(..., description="Why recommended")
    styling_tips: List[str] = Field(default_factory=list, description="Styling tips")


class RecommendationResponse(BaseModel):
    """Wedding dress recommendation response"""
    request_params: RecommendationRequest
    recommendations: List[DressRecommendation] = Field(..., description="Dress recommendations")
    overall_advice: str = Field(..., description="Overall advice")
    cached: bool = Field(default=False, description="Whether cached result")
    source: str = Field(default="ai_generated", description="Result source")


# Wedding Venue Schemas
class GuestCount(str, Enum):
    SMALL = "소규모"
    MEDIUM = "중규모"
    LARGE = "대규모"


class Budget(str, Enum):
    LOW = "저"
    MEDIUM = "중"
    HIGH = "고"


class Region(str, Enum):
    SEOUL = "서울"
    GYEONGGI = "경기"
    INCHEON = "인천"
    ANYWHERE = "상관없음"


class VenueStyle(str, Enum):
    LUXURY = "럭셔리"
    MODERN = "모던"
    CLASSIC = "클래식"
    NATURE = "자연친화"
    GARDEN = "야외정원"
    MINIMAL = "미니멀"
    UNIQUE = "유니크"


class Season(str, Enum):
    SPRING = "봄"
    SUMMER = "여름"
    FALL = "가을"
    WINTER = "겨울"


class VenueRecommendationRequest(BaseModel):
    """Wedding venue recommendation request"""
    guest_count: GuestCount = Field(..., description="Expected number of guests")
    budget: Budget = Field(..., description="Budget range")
    region: Region = Field(..., description="Preferred region")
    style_preference: VenueStyle = Field(..., description="Preferred venue style")
    season: Season = Field(..., description="Wedding season")
    num_recommendations: int = Field(
        default=3,
        ge=1,
        le=5,
        description="Number of recommendations (1-5)"
    )

    class Config:
        json_schema_extra = {
            "example": {
                "guest_count": "중규모",
                "budget": "중",
                "region": "서울",
                "style_preference": "모던",
                "season": "가을",
                "num_recommendations": 3
            }
        }


class VenueRecommendation(BaseModel):
    """Single venue recommendation"""
    venue_name: str = Field(..., description="Venue name")
    description: str = Field(..., description="Venue description")
    capacity: str = Field(..., description="Guest capacity range")
    location: str = Field(..., description="Venue location")
    price_range: str = Field(..., description="Price range")
    estimated_cost: str = Field(..., description="Estimated cost")
    why_recommended: str = Field(..., description="Why recommended")
    pros: List[str] = Field(default_factory=list, description="Advantages")
    cons: List[str] = Field(default_factory=list, description="Disadvantages")
    amenities: List[str] = Field(default_factory=list, description="Amenities")
    food_style: List[str] = Field(default_factory=list, description="Food service styles")
    booking_tips: List[str] = Field(default_factory=list, description="Booking tips")


class VenueRecommendationResponse(BaseModel):
    """Wedding venue recommendation response"""
    request_params: VenueRecommendationRequest
    recommendations: List[VenueRecommendation] = Field(..., description="Venue recommendations")
    overall_advice: str = Field(..., description="Overall advice")
    cached: bool = Field(default=False, description="Whether cached result")
    source: str = Field(default="ai_generated", description="Result source")
