package com.msa.travel.api.controller

import com.msa.common.exception.ForbiddenException
import com.msa.common.exception.NotFoundException
import com.msa.common.response.ApiResponse
import com.msa.travel.domain.entity.Itinerary
import com.msa.travel.domain.repository.ItineraryRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
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
        @AuthenticationPrincipal userId: Long,
        @PageableDefault(size = 10) pageable: Pageable
    ): ApiResponse<Page<ItineraryResponse>> {
        return ApiResponse.success(itineraryRepository.findByUserId(userId, pageable).map { ItineraryResponse.from(it) })
    }

    @Operation(summary = "여행 일정 상세")
    @GetMapping("/{id}")
    fun getItinerary(
        @AuthenticationPrincipal userId: Long,
        @PathVariable id: Long
    ): ApiResponse<ItineraryResponse> {
        val itinerary = itineraryRepository.findById(id).orElseThrow { NotFoundException("Itinerary", id) }
        if (itinerary.userId != userId && !itinerary.isPublic) {
            throw ForbiddenException("Cannot access this itinerary")
        }
        return ApiResponse.success(ItineraryResponse.from(itinerary))
    }

    @Operation(summary = "공개 여행 일정")
    @GetMapping("/public")
    fun getPublicItineraries(@PageableDefault(size = 10) pageable: Pageable): ApiResponse<Page<ItineraryResponse>> {
        return ApiResponse.success(itineraryRepository.findPublicItineraries(pageable).map { ItineraryResponse.from(it) })
    }
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
