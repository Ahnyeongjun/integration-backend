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

@Tag(name = "Dress Shop", description = "드레스샵 API")
@RestController
@RequestMapping("/api/v1/dress-shops")
class DressShopController(
    private val dressShopService: DressShopService
) {
    @Operation(summary = "드레스샵 목록", description = "정렬: RECENT(최신순), FAVORITE(인기순). 로그인 시 isLiked 포함")
    @GetMapping
    fun getShops(
        @Parameter(description = "샵 이름 검색") @RequestParam(required = false) shopName: String?,
        @Parameter(description = "주소/지역 검색") @RequestParam(required = false) address: String?,
        @Parameter(description = "전문분야 검색") @RequestParam(required = false) specialty: String?,
        @Parameter(description = "정렬: RECENT, FAVORITE") @RequestParam(required = false) sortType: SortType?,
        @AuthenticationPrincipal(expression = "userId") userId: Long?,
        @PageableDefault(size = 20) pageable: Pageable
    ): ApiResponse<Page<DressShopResponse>> {
        return ApiResponse.success(dressShopService.searchShops(shopName, address, specialty, sortType, userId, pageable))
    }

    @Operation(summary = "드레스샵 상세", description = "로그인 시 isLiked 포함")
    @GetMapping("/{id}")
    fun getShop(
        @PathVariable id: Long,
        @AuthenticationPrincipal(expression = "userId") userId: Long?
    ): ApiResponse<DressShopResponse> =
        ApiResponse.success(dressShopService.getShopWithLikeStatus(id, userId))

    @Operation(summary = "드레스샵 등록")
    @PostMapping
    fun createShop(@RequestBody request: DressShopCreateRequest): ApiResponse<DressShopResponse> =
        ApiResponse.success(dressShopService.createShop(request))

    @Operation(summary = "드레스샵 수정")
    @PutMapping("/{id}")
    fun updateShop(
        @PathVariable id: Long,
        @RequestBody request: DressShopUpdateRequest
    ): ApiResponse<DressShopResponse> =
        ApiResponse.success(dressShopService.updateShop(id, request))

    @Operation(summary = "드레스샵 삭제")
    @DeleteMapping("/{id}")
    fun deleteShop(@PathVariable id: Long): ApiResponse<Unit> {
        dressShopService.deleteShop(id)
        return ApiResponse.success()
    }

    @Operation(summary = "샵의 드레스 목록")
    @GetMapping("/{id}/dresses")
    fun getDressesByShop(
        @PathVariable id: Long,
        @PageableDefault(size = 20) pageable: Pageable
    ): ApiResponse<Page<DressResponse>> =
        ApiResponse.success(dressShopService.getDressesByShop(id, pageable))
}

data class DressShopResponse(
    val id: Long,
    val shopName: String,
    val description: String?,
    val address: String?,
    val phone: String?,
    val snsUrl: String?,
    val imageUrl: String?,
    val specialty: String?,
    val features: String?,
    val avgRating: Double,
    val bookmarkCount: Long = 0,
    val isLiked: Boolean = false
) {
    companion object {
        fun from(s: DressShop, bookmarkCount: Long = 0, isLiked: Boolean = false) = DressShopResponse(
            id = s.id,
            shopName = s.shopName,
            description = s.description,
            address = s.address,
            phone = s.phone,
            snsUrl = s.snsUrl,
            imageUrl = s.imageUrl,
            specialty = s.specialty,
            features = s.features,
            avgRating = s.avgRating,
            bookmarkCount = bookmarkCount,
            isLiked = isLiked
        )
    }
}

data class DressShopCreateRequest(
    val shopName: String,
    val description: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val snsUrl: String? = null,
    val imageUrl: String? = null,
    val specialty: String? = null,
    val features: String? = null
)

data class DressShopUpdateRequest(
    val shopName: String? = null,
    val description: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val snsUrl: String? = null,
    val imageUrl: String? = null,
    val specialty: String? = null,
    val features: String? = null
)
