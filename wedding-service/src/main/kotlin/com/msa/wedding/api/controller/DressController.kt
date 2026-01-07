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
import org.springframework.web.bind.annotation.*

@Tag(name = "Dress", description = "드레스 API")
@RestController
@RequestMapping("/api/v1/dresses")
class DressController(
    private val dressService: DressService
) {
    @Operation(summary = "드레스 목록")
    @GetMapping
    fun getDresses(
        @Parameter(description = "검색 키워드") @RequestParam(required = false) keyword: String?,
        @Parameter(description = "정렬: RECENT, FAVORITE") @RequestParam(required = false) sortType: SortType?,
        @PageableDefault(size = 20) pageable: Pageable
    ): ApiResponse<Page<DressResponse>> {
        return ApiResponse.success(dressService.getDresses(keyword, sortType, pageable))
    }

    @Operation(summary = "드레스 상세")
    @GetMapping("/{id}")
    fun getDress(@PathVariable id: Long): ApiResponse<DressResponse> =
        ApiResponse.success(dressService.getDressByIdWithResponse(id))

    @Operation(summary = "드레스 유형별 목록")
    @GetMapping("/type/{dressType}")
    fun getDressesByType(
        @PathVariable dressType: DressType,
        @PageableDefault(size = 20) pageable: Pageable
    ): ApiResponse<Page<DressResponse>> =
        ApiResponse.success(dressService.getDressesByType(dressType, pageable))

    @Operation(summary = "드레스 등록")
    @PostMapping
    fun createDress(@RequestBody request: DressCreateRequest): ApiResponse<DressResponse> =
        ApiResponse.success(dressService.createDress(request))

    @Operation(summary = "드레스 수정")
    @PutMapping("/{id}")
    fun updateDress(
        @PathVariable id: Long,
        @RequestBody request: DressUpdateRequest
    ): ApiResponse<DressResponse> =
        ApiResponse.success(dressService.updateDress(id, request))

    @Operation(summary = "드레스 삭제")
    @DeleteMapping("/{id}")
    fun deleteDress(@PathVariable id: Long): ApiResponse<Unit> {
        dressService.deleteDress(id)
        return ApiResponse.success()
    }
}

data class DressResponse(
    val id: Long,
    val shopName: String,
    val name: String?,
    val color: String?,
    val shape: String?,
    val priceRange: String?,
    val length: DressLength?,
    val season: DressSeason?,
    val designer: String?,
    val dressType: DressType?,
    val neckLine: DressNeckline?,
    val mood: DressMood?,
    val fabric: String?,
    val imageUrl: String?,
    val features: String?
) {
    companion object {
        fun from(d: Dress) = DressResponse(
            id = d.id,
            shopName = d.shopName,
            name = d.name,
            color = d.color,
            shape = d.shape,
            priceRange = d.priceRange,
            length = d.length,
            season = d.season,
            designer = d.designer,
            dressType = d.dressType,
            neckLine = d.neckLine,
            mood = d.mood,
            fabric = d.fabric,
            imageUrl = d.imageUrl,
            features = d.features
        )
    }
}
