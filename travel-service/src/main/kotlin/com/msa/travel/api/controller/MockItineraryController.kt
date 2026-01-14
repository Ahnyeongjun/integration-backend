package com.msa.travel.api.controller

import com.msa.common.response.ApiResponse
import com.msa.travel.api.dto.*
import com.msa.travel.domain.entity.Itinerary
import com.msa.travel.domain.repository.ItineraryRepository
import com.msa.travel.mock.MockTravelData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap

/**
 * Mock Itinerary Controller
 * MCP 기능 미지원으로 인한 Mock API
 * 원본 SWYP_BACK API와 동일한 경로 및 응답 형식 유지
 */
@Tag(name = "Mock Itinerary", description = "여행 일정 Mock API (MCP 미지원)")
@RestController
@RequestMapping("/api/v1/itinerary")
class MockItineraryController(
    private val itineraryRepository: ItineraryRepository
) {
    // 임시 저장소 (실제로는 DB 사용)
    private val tempItineraryStore = ConcurrentHashMap<Long, CreateItineraryResponse>()

    @Operation(summary = "추천 여행지 조회", description = "사용자 입력 기반 추천 장소 3개 (Mock)")
    @GetMapping("/preview")
    fun getRecommendedDestinations(
        @RequestParam(required = false) feeling: String?,
        @RequestParam(required = false) atmosphere: String?,
        @RequestParam(required = false) activities: String?
    ): ResponseEntity<List<RecommendResponse>> {
        val destinations = MockTravelData.getRandomDestinations(3)
        return ResponseEntity.ok(destinations)
    }

    @Operation(summary = "여행 일정 생성", description = "사용자 입력 기반 여행 일정 생성 (Mock)")
    @PostMapping("/create")
    fun createItinerary(
        @RequestBody request: CreateItineraryRequest
    ): ResponseEntity<CreateItineraryResponse> {
        val closestRegion = MockTravelData.findClosestRegion(request.latitude, request.longitude)

        val title = if (request.duration == 1) {
            "${closestRegion} 하루"
        } else {
            "${closestRegion} ${request.duration - 1}박 ${request.duration}일"
        }

        val dailySchedules = MockTravelData.generateMockItinerary(request.duration, closestRegion)
        val id = System.currentTimeMillis()

        val response = CreateItineraryResponse(
            id = id,
            title = title,
            createdBy = 0,
            createdAt = System.currentTimeMillis(),
            isPublic = false,
            isSaved = false,
            dailyScheduleDtos = dailySchedules
        )

        // 임시 저장
        tempItineraryStore[id] = response

        return ResponseEntity.ok(response)
    }

    @Operation(summary = "명소 변경", description = "다른 명소로 변경 (Mock)")
    @PostMapping("/change/attraction")
    fun changeAttraction(
        @RequestBody request: ChangeAttractionRequest
    ): ResponseEntity<AttractionDto> {
        val newAttraction = MockTravelData.getRandomAttraction(request.id)
        return ResponseEntity.ok(newAttraction)
    }

    @Operation(summary = "추천 텍스트 조회", description = "입력 필드 플레이스홀더 텍스트 (Mock)")
    @GetMapping("/recommend/text")
    fun getRecommendText(
        @RequestParam(required = false) feeling: String?,
        @RequestParam(required = false) atmosphere: String?,
        @RequestParam(required = false) activities: String?
    ): ResponseEntity<RecommendTextResponse> {
        val suggestion = MockTravelData.getRandomSuggestion()
        return ResponseEntity.ok(suggestion)
    }

    @Operation(summary = "공개 여행 일정 목록", description = "공개된 여행 일정 목록 조회")
    @GetMapping("/public")
    fun getPublicItineraries(
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<List<PublicItineraryResponse>> {
        val pageable = Pageable.ofSize(limit)
        val itineraries = itineraryRepository.findPublicItineraries(pageable)
        val response = itineraries.content.map { PublicItineraryResponse.from(it) }
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "여행 일정 상세 조회", description = "ID로 여행 일정 상세 조회")
    @GetMapping("/{id}")
    fun getItineraryDetail(
        @PathVariable id: Long
    ): ResponseEntity<CreateItineraryResponse> {
        // 임시 저장소에서 조회
        val itinerary = tempItineraryStore[id]
        if (itinerary != null) {
            return ResponseEntity.ok(itinerary)
        }

        // DB에서 조회 (Mock 데이터로 변환)
        val dbItinerary = itineraryRepository.findById(id).orElse(null)
        if (dbItinerary != null) {
            val mockResponse = createMockResponseFromEntity(dbItinerary)
            return ResponseEntity.ok(mockResponse)
        }

        return ResponseEntity.notFound().build()
    }

    @Operation(summary = "여행 일정 목록 조회 (사용자별)", description = "사용자의 저장된 여행 일정 목록 조회")
    @GetMapping("/lists/{userId}")
    fun getItineraryLists(
        @PathVariable userId: Long
    ): ResponseEntity<List<CreateItineraryResponse>> {
        val pageable = Pageable.ofSize(100)
        val itineraries = itineraryRepository.findByUserId(userId, pageable)
        val response = itineraries.content.map { createMockResponseFromEntity(it) }
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "여행 일정 저장", description = "생성된 여행 일정 저장")
    @PatchMapping
    fun saveItinerary(
        @AuthenticationPrincipal userId: Long?,
        @RequestBody request: CreateItineraryResponse
    ): ResponseEntity<SaveItineraryResponse> {
        // 실제 DB 저장 로직
        val effectiveUserId = userId ?: 0L
        val startDate = LocalDate.now()
        val endDate = startDate.plusDays((request.dailyScheduleDtos.size - 1).toLong())

        val itinerary = Itinerary(
            userId = effectiveUserId,
            title = request.title,
            startDate = startDate,
            endDate = endDate,
            isPublic = request.isPublic,
            coverImage = request.dailyScheduleDtos.firstOrNull()?.attractions?.firstOrNull()?.coverImage
        )
        val saved = itineraryRepository.save(itinerary)

        // 임시 저장소 업데이트
        tempItineraryStore[saved.id] = request.copy(id = saved.id, isSaved = true)

        return ResponseEntity.ok(SaveItineraryResponse(itineraryId = saved.id))
    }

    @Operation(summary = "여행 일정 삭제", description = "여행 일정 삭제")
    @DeleteMapping("/{id}")
    fun deleteItinerary(
        @AuthenticationPrincipal userId: Long?,
        @PathVariable id: Long
    ): ResponseEntity<Boolean> {
        tempItineraryStore.remove(id)
        if (itineraryRepository.existsById(id)) {
            itineraryRepository.deleteById(id)
        }
        return ResponseEntity.ok(true)
    }

    private fun createMockResponseFromEntity(itinerary: Itinerary): CreateItineraryResponse {
        val duration = itinerary.getTotalDays()
        val dailySchedules = MockTravelData.generateMockItinerary(duration, "여행")

        return CreateItineraryResponse(
            id = itinerary.id,
            title = itinerary.title,
            createdBy = itinerary.userId,
            createdAt = System.currentTimeMillis(),
            isPublic = itinerary.isPublic,
            isSaved = true,
            dailyScheduleDtos = dailySchedules
        )
    }
}
