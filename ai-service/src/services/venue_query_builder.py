"""Wedding venue SQL query builder - Parameterized"""
from typing import Tuple


def build_venue_query(
    guest_count: str,
    budget: str,
    region: str,
    style_preference: str,
    season: str,
    num_recommendations: int = 3
) -> Tuple[str, dict]:
    """
    Build SQL query for venue search

    Returns:
        Tuple[str, dict]: (query string, parameters dict)
    """
    conditions = []
    params = {}

    # 1. region -> address
    if region and region != "상관없음":
        conditions.append("address LIKE :region_pattern")
        params["region_pattern"] = f"%{region}%"

    # 2. style_preference -> venueType
    style_to_venue_type = {
        "럭셔리": ["HOTEL"],
        "모던": ["HOTEL", "WEDDING_HALL"],
        "클래식": ["HOTEL", "WEDDING_HALL"],
        "자연친화": ["GARDEN", "OUTDOOR"],
        "야외정원": ["GARDEN", "OUTDOOR"],
        "미니멀": ["HOUSE_STUDIO", "RESTAURANT"],
        "유니크": ["HOUSE_STUDIO", "RESTAURANT", "OTHER"],
    }
    venue_types = style_to_venue_type.get(style_preference, [])

    # 3. season -> venueType filter
    if season in ["여름", "겨울"]:
        indoor_types = ["HOTEL", "WEDDING_HALL", "RESTAURANT", "HOUSE_STUDIO"]
        if venue_types:
            venue_types = [vt for vt in venue_types if vt in indoor_types]
        else:
            venue_types = indoor_types

    if venue_types:
        type_placeholders = [f":venue_type_{i}" for i in range(len(venue_types))]
        conditions.append(f"venueType IN ({', '.join(type_placeholders)})")
        for i, vt in enumerate(venue_types):
            params[f"venue_type_{i}"] = vt

    # 4. guest_count -> parking
    parking_map = {
        "소규모": (0, 50),
        "중규모": (30, 150),
        "대규모": (100, None),
    }
    if guest_count in parking_map:
        min_val, max_val = parking_map[guest_count]
        conditions.append("parking >= :parking_min")
        params["parking_min"] = min_val
        if max_val:
            conditions.append("parking <= :parking_max")
            params["parking_max"] = max_val

    # 5. budget -> venueType filter
    if budget == "저":
        conditions.append("venueType != :excluded_type")
        params["excluded_type"] = "HOTEL"

    # Build query
    query = "SELECT * FROM tb_wedding_hall"
    if conditions:
        query += " WHERE " + " AND ".join(conditions)

    # ORDER BY
    if budget == "고":
        query += " ORDER BY CASE WHEN venueType = 'HOTEL' THEN 0 ELSE 1 END"

    query += " LIMIT :limit"
    params["limit"] = num_recommendations

    return query, params
