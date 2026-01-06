package com.msa.wedding.api.controller

import com.msa.common.response.ApiResponse
import com.msa.wedding.application.*
import com.msa.wedding.domain.entity.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "Wedding Hall", description = "웨딩홀 API")
@RestController
@RequestMapping("/api/v1/wedding-halls")
class WeddingHallController(
    private val weddingHallService: WeddingHallService
) {
    @Operation(summary = "웨딩홀 목록", description = "정렬: RECENT(최신순), FAVORITE(인기순). 로그인 시 isLiked 포함")
    @GetMapping
    fun getHalls(
        @Parameter(description = "검색 키워드") @RequestParam(required = false) keyword: String?,
        @Parameter(description = "정렬: RECENT, FAVORITE") @RequestParam(required = false) sort: SortType?,
        @AuthenticationPrincipal userId: Long?,
        @PageableDefault(size = 20) pageable: Pageable
    ): ApiResponse<Page<WeddingHallResponse>> {
        return ApiResponse.success(weddingHallService.getHalls(keyword, sort, userId, pageable))
    }

    @Operation(summary = "웨딩홀 상세", description = "로그인 시 isLiked 포함")
    @GetMapping("/{id}")
    fun getHall(
        @PathVariable id: Long,
        @AuthenticationPrincipal userId: Long?
    ): ApiResponse<WeddingHallResponse> =
        ApiResponse.success(weddingHallService.getHallWithLikeStatus(id, userId))

    @Operation(summary = "홀 유형별 목록")
    @GetMapping("/type/{hallType}")
    fun getHallsByType(
        @PathVariable hallType: HallType,
        @RequestParam(required = false) sort: SortType?,
        @AuthenticationPrincipal userId: Long?,
        @PageableDefault(size = 20) pageable: Pageable
    ): ApiResponse<Page<WeddingHallResponse>> =
        ApiResponse.success(weddingHallService.getHallsByType(hallType, sort, userId, pageable))

    @Operation(summary = "수용인원별 목록")
    @GetMapping("/capacity")
    fun getHallsByCapacity(
        @RequestParam guests: Int,
        @RequestParam(required = false) sort: SortType?,
        @AuthenticationPrincipal userId: Long?,
        @PageableDefault(size = 20) pageable: Pageable
    ): ApiResponse<Page<WeddingHallResponse>> =
        ApiResponse.success(weddingHallService.getHallsByCapacity(guests, sort, userId, pageable))

    @Operation(summary = "웨딩홀 등록")
    @PostMapping
    fun createHall(@RequestBody request: HallCreateRequest): ApiResponse<WeddingHallResponse> =
        ApiResponse.success(weddingHallService.createHall(request))

    @Operation(summary = "웨딩홀 수정")
    @PutMapping("/{id}")
    fun updateHall(
        @PathVariable id: Long,
        @RequestBody request: HallUpdateRequest
    ): ApiResponse<WeddingHallResponse> =
        ApiResponse.success(weddingHallService.updateHall(id, request))

    @Operation(summary = "웨딩홀 삭제")
    @DeleteMapping("/{id}")
    fun deleteHall(@PathVariable id: Long): ApiResponse<Unit> {
        weddingHallService.deleteHall(id)
        return ApiResponse.success()
    }
}

data class WeddingHallResponse(
    val id: Long,
    val name: String,
    val address: String,
    val hallType: HallType,
    val description: String?,
    val minGuarantee: Int?,
    val maxCapacity: Int?,
    val mealPrice: Int?,
    val hallRentalPrice: Int?,
    val coverImage: String?,
    val avgRating: Double,
    val reviewCount: Int,
    val bookmarkCount: Long = 0,
    val isLiked: Boolean = false
) {
    companion object {
        fun from(h: WeddingHall, bookmarkCount: Long = 0, isLiked: Boolean = false) = WeddingHallResponse(
            id = h.id,
            name = h.name,
            address = h.address,
            hallType = h.hallType,
            description = h.description,
            minGuarantee = h.minGuarantee,
            maxCapacity = h.maxCapacity,
            mealPrice = h.mealPrice,
            hallRentalPrice = h.hallRentalPrice,
            coverImage = h.coverImage,
            avgRating = h.avgRating,
            reviewCount = h.reviewCount,
            bookmarkCount = bookmarkCount,
            isLiked = isLiked
        )
    }
}
