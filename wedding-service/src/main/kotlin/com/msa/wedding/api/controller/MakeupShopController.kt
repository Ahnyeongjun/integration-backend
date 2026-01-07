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

@Tag(name = "Makeup Shop", description = "메이크업샵 API")
@RestController
@RequestMapping("/api/v1/makeup-shops")
class MakeupShopController(
    private val makeupShopService: MakeupShopService
) {
    @Operation(summary = "메이크업샵 목록", description = "정렬: RECENT(최신순), FAVORITE(인기순). 로그인 시 isLiked 포함")
    @GetMapping
    fun getShops(
        @Parameter(description = "샵 이름 검색") @RequestParam(required = false) name: String?,
        @Parameter(description = "주소/지역 검색") @RequestParam(required = false) address: String?,
        @Parameter(description = "전문분야 검색") @RequestParam(required = false) specialty: String?,
        @Parameter(description = "정렬: RECENT, FAVORITE") @RequestParam(required = false) sortType: SortType?,
        @AuthenticationPrincipal userId: Long?,
        @PageableDefault(size = 20) pageable: Pageable
    ): ApiResponse<Page<MakeupShopResponse>> {
        return ApiResponse.success(makeupShopService.searchShops(name, address, specialty, sortType, userId, pageable))
    }

    @Operation(summary = "메이크업샵 상세", description = "로그인 시 isLiked 포함")
    @GetMapping("/{id}")
    fun getShop(
        @PathVariable id: Long,
        @AuthenticationPrincipal userId: Long?
    ): ApiResponse<MakeupShopResponse> =
        ApiResponse.success(makeupShopService.getShopWithLikeStatus(id, userId))

    @Operation(summary = "서비스 유형별 목록")
    @GetMapping("/type/{serviceType}")
    fun getShopsByServiceType(
        @PathVariable serviceType: MakeupServiceType,
        @PageableDefault(size = 20) pageable: Pageable
    ): ApiResponse<Page<MakeupShopResponse>> =
        ApiResponse.success(makeupShopService.getShopsByServiceType(serviceType, pageable).map { MakeupShopResponse.from(it) })

    @Operation(summary = "출장 가능 샵 목록")
    @GetMapping("/on-site")
    fun getOnSiteAvailableShops(@PageableDefault(size = 20) pageable: Pageable): ApiResponse<Page<MakeupShopResponse>> =
        ApiResponse.success(makeupShopService.getOnSiteAvailableShops(pageable).map { MakeupShopResponse.from(it) })

    @Operation(summary = "메이크업샵 등록")
    @PostMapping
    fun createShop(@RequestBody request: MakeupShopCreateRequest): ApiResponse<MakeupShopResponse> =
        ApiResponse.success(makeupShopService.createShop(request))

    @Operation(summary = "메이크업샵 수정")
    @PutMapping("/{id}")
    fun updateShop(
        @PathVariable id: Long,
        @RequestBody request: MakeupShopUpdateRequest
    ): ApiResponse<MakeupShopResponse> =
        ApiResponse.success(makeupShopService.updateShop(id, request))

    @Operation(summary = "메이크업샵 삭제")
    @DeleteMapping("/{id}")
    fun deleteShop(@PathVariable id: Long): ApiResponse<Unit> {
        makeupShopService.deleteShop(id)
        return ApiResponse.success()
    }
}

data class MakeupShopResponse(
    val id: Long,
    val name: String,
    val address: String,
    val serviceType: MakeupServiceType,
    val basePrice: Int?,
    val coverImage: String?,
    val phone: String?,
    val snsUrl: String?,
    val specialty: String?,
    val avgRating: Double,
    val onSiteAvailable: Boolean,
    val bookmarkCount: Long = 0,
    val isLiked: Boolean = false
) {
    companion object {
        fun from(m: MakeupShop, bookmarkCount: Long = 0, isLiked: Boolean = false) = MakeupShopResponse(
            id = m.id,
            name = m.name,
            address = m.address,
            serviceType = m.serviceType,
            basePrice = m.basePrice,
            coverImage = m.coverImage,
            phone = m.phone,
            snsUrl = m.snsUrl,
            specialty = m.specialty,
            avgRating = m.avgRating,
            onSiteAvailable = m.onSiteAvailable,
            bookmarkCount = bookmarkCount,
            isLiked = isLiked
        )
    }
}
