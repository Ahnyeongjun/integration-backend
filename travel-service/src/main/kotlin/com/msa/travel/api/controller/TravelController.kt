package com.msa.travel.api.controller

import com.msa.common.response.ApiResponse
import com.msa.travel.domain.entity.Itinerary
import com.msa.travel.domain.repository.ItineraryRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import com.msa.common.security.UserPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@Tag(name = "Itinerary", description = "여행 일정 API")
@RestController
@RequestMapping("/api/v1/itineraries")
class ItineraryController(
    private val itineraryRepository: ItineraryRepository
) {
    @Operation(summary = "내 여행 일정 목록")
    @GetMapping
    fun getMyItineraries(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PageableDefault(size = 10) pageable: Pageable
    ): ApiResponse<Page<ItineraryResponse>> {
        return ApiResponse.success(itineraryRepository.findByUserId(principal.userId, pageable).map { ItineraryResponse.from(it) })
    }

    // MockItineraryController에서 처리: GET /{id}, GET /public, GET /list
}

data class ItineraryResponse(
    val id: Long,
    val title: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalDays: Int,
    val isPublic: Boolean,
    val coverImage: String?
) {
    companion object {
        fun from(i: Itinerary) = ItineraryResponse(
            id = i.id, title = i.title, startDate = i.startDate, endDate = i.endDate,
            totalDays = i.getTotalDays(), isPublic = i.isPublic, coverImage = i.coverImage
        )
    }
}

/**
 * 프론트엔드 레거시 호환용 DTO
 * 원본 API: { id, title, image_url[] }
 */
data class ItineraryListResponse(
    val id: Long,
    val title: String,
    val image_url: List<String>
) {
    companion object {
        fun from(i: Itinerary) = ItineraryListResponse(
            id = i.id,
            title = i.title,
            image_url = if (i.coverImage != null) listOf(i.coverImage!!) else emptyList()
        )
    }
}
