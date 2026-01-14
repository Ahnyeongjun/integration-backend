package com.msa.travel.api.dto

import java.time.LocalDate

/**
 * 추천 여행지 요청 DTO
 */
data class RecommendRequest(
    val feeling: String?,
    val atmosphere: String?,
    val activities: String?
)

/**
 * 추천 여행지 응답 DTO
 */
data class RecommendResponse(
    val name: String,
    val theme: String,
    val address: String,
    val imageUrl: String,
    val latitude: Double,
    val longitude: Double
)

/**
 * 여행 일정 생성 요청 DTO
 */
data class CreateItineraryRequest(
    val travelWith: String,
    val startDate: LocalDate,
    val duration: Int,
    val theme: String,
    val latitude: Double,
    val longitude: Double,
    val wantedDto: WantedDto
)

data class WantedDto(
    val feeling: String?,
    val atmosphere: String?,
    val activities: String?
)

/**
 * 여행 일정 생성 응답 DTO
 */
data class CreateItineraryResponse(
    val id: Long,
    val title: String,
    val createdBy: Long,
    val createdAt: Long,
    val isPublic: Boolean,
    val isSaved: Boolean,
    val dailyScheduleDtos: List<DailyScheduleDto>
)

data class DailyScheduleDto(
    val dayDate: Int,
    val attractions: List<AttractionDto>
)

data class AttractionDto(
    val id: Long,
    val type: String, // "place" | "meal"
    val name: String,
    val address: String,
    val description: String,
    val coverImage: String,
    val businessTime: String,
    val rating: Double,
    val latitude: Double,
    val longitude: Double,
    val travelWalkTime: String? = null,
    val travelCarTime: String? = null,
    val travelDistance: String? = null
)

/**
 * 명소 변경 요청 DTO
 */
data class ChangeAttractionRequest(
    val id: Long,
    val type: String,
    val name: String,
    val address: String,
    val description: String,
    val coverImage: String,
    val businessTime: String,
    val rating: Double,
    val latitude: Double,
    val longitude: Double
)

/**
 * 추천 텍스트 응답 DTO
 */
data class RecommendTextResponse(
    val feeling: String,
    val atmosphere: String,
    val activities: String
)

/**
 * 공개 여행 일정 응답 DTO
 */
data class PublicItineraryResponse(
    val id: Long,
    val title: String,
    val image_url: String
) {
    companion object {
        fun from(itinerary: com.msa.travel.domain.entity.Itinerary) = PublicItineraryResponse(
            id = itinerary.id,
            title = itinerary.title,
            image_url = itinerary.coverImage ?: ""
        )
    }
}

/**
 * 일정 저장 응답 DTO
 */
data class SaveItineraryResponse(
    val itineraryId: Long
)
