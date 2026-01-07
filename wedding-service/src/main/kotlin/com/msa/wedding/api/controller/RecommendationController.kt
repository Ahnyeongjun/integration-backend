package com.msa.wedding.api.controller

import com.msa.common.response.ApiResponse
import com.msa.wedding.application.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@Tag(name = "Recommendation", description = "추천 API")
@RestController
@RequestMapping("/api/v1/recommendations")
class RecommendationController(
    private val dressRecommendationService: DressRecommendationService,
    private val venueRecommendationService: VenueRecommendationService
) {
    // =====================================================
    // 드레스 추천 API
    // =====================================================

    @Operation(
        summary = "체형 기반 드레스 추천",
        description = "체형 정보(팔 길이, 다리 길이, 목 길이, 얼굴형, 체형)를 기반으로 어울리는 드레스를 추천합니다."
    )
    @PostMapping("/dress")
    fun getDressRecommendation(
        @RequestBody request: DressRecommendationRequest
    ): ApiResponse<DressRecommendationResponse> {
        return ApiResponse.success(dressRecommendationService.getRecommendation(request))
    }

    @Operation(
        summary = "드레스 추천 결과 조회",
        description = "저장된 드레스 추천 결과를 ID로 조회합니다."
    )
    @GetMapping("/dress/{id}")
    fun getDressRecommendationById(
        @PathVariable id: Long
    ): ApiResponse<DressRecommendationResponse> {
        return ApiResponse.success(dressRecommendationService.getRecommendationById(id))
    }

    @Operation(
        summary = "인기 드레스 추천 목록",
        description = "가장 많이 조회된 드레스 추천 결과 목록을 반환합니다."
    )
    @GetMapping("/dress/popular")
    fun getPopularDressRecommendations(
        @Parameter(description = "조회할 개수 (기본: 10)")
        @RequestParam(defaultValue = "10") limit: Int
    ): ApiResponse<List<DressRecommendationResponse>> {
        return ApiResponse.success(dressRecommendationService.getPopularRecommendations(limit))
    }

    @Operation(
        summary = "체형 기반 드레스 추천 (GET)",
        description = "쿼리 파라미터를 통해 체형 기반 드레스를 추천받습니다."
    )
    @GetMapping("/dress")
    fun getDressRecommendationByParams(
        @Parameter(description = "팔 길이 (SHORT, MEDIUM, LONG)", required = true)
        @RequestParam armLength: String,
        @Parameter(description = "다리 길이 (SHORT, MEDIUM, LONG)", required = true)
        @RequestParam legLength: String,
        @Parameter(description = "목 길이 (SHORT, MEDIUM, LONG)", required = true)
        @RequestParam neckLength: String,
        @Parameter(description = "얼굴형 (OVAL, ROUND, SQUARE, HEART, LONG, DIAMOND)", required = true)
        @RequestParam faceShape: String,
        @Parameter(description = "체형 (SLIM, STANDARD, CURVY, PLUS_SIZE)")
        @RequestParam(required = false) bodyType: String?
    ): ApiResponse<DressRecommendationResponse> {
        val request = DressRecommendationRequest(
            armLength = armLength,
            legLength = legLength,
            neckLength = neckLength,
            faceShape = faceShape,
            bodyType = bodyType
        )
        return ApiResponse.success(dressRecommendationService.getRecommendation(request))
    }

    // =====================================================
    // 웨딩홀 추천 API
    // =====================================================

    @Operation(
        summary = "조건 기반 웨딩홀 추천",
        description = "조건(하객 수, 예산, 지역, 스타일, 시즌)을 기반으로 어울리는 웨딩홀을 추천합니다."
    )
    @PostMapping("/venue")
    fun getVenueRecommendation(
        @RequestBody request: VenueRecommendationRequest
    ): ApiResponse<VenueRecommendationResponse> {
        return ApiResponse.success(venueRecommendationService.getRecommendation(request))
    }

    @Operation(
        summary = "웨딩홀 추천 결과 조회",
        description = "저장된 웨딩홀 추천 결과를 ID로 조회합니다."
    )
    @GetMapping("/venue/{id}")
    fun getVenueRecommendationById(
        @PathVariable id: Long
    ): ApiResponse<VenueRecommendationResponse> {
        return ApiResponse.success(venueRecommendationService.getRecommendationById(id))
    }

    @Operation(
        summary = "인기 웨딩홀 추천 목록",
        description = "가장 많이 조회된 웨딩홀 추천 결과 목록을 반환합니다."
    )
    @GetMapping("/venue/popular")
    fun getPopularVenueRecommendations(
        @Parameter(description = "조회할 개수 (기본: 10)")
        @RequestParam(defaultValue = "10") limit: Int
    ): ApiResponse<List<VenueRecommendationResponse>> {
        return ApiResponse.success(venueRecommendationService.getPopularRecommendations(limit))
    }

    @Operation(
        summary = "조건 기반 웨딩홀 추천 (GET)",
        description = "쿼리 파라미터를 통해 조건 기반 웨딩홀을 추천받습니다."
    )
    @GetMapping("/venue")
    fun getVenueRecommendationByParams(
        @Parameter(description = "하객 수 (SMALL, MEDIUM, LARGE, EXTRA_LARGE)", required = true)
        @RequestParam guestCount: String,
        @Parameter(description = "예산 (ECONOMY, STANDARD, PREMIUM, LUXURY)", required = true)
        @RequestParam budget: String,
        @Parameter(description = "지역 (SEOUL, GYEONGGI, INCHEON, BUSAN, DAEGU, DAEJEON, GWANGJU, JEJU, OTHER)", required = true)
        @RequestParam region: String,
        @Parameter(description = "스타일 (MODERN, CLASSIC, ROMANTIC, NATURAL, LUXURY, MINIMAL)", required = true)
        @RequestParam stylePreference: String,
        @Parameter(description = "시즌 (SPRING, SUMMER, FALL, WINTER)", required = true)
        @RequestParam season: String
    ): ApiResponse<VenueRecommendationResponse> {
        val request = VenueRecommendationRequest(
            guestCount = guestCount,
            budget = budget,
            region = region,
            stylePreference = stylePreference,
            season = season
        )
        return ApiResponse.success(venueRecommendationService.getRecommendation(request))
    }
}
