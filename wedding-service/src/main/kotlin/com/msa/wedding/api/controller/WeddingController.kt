package com.msa.wedding.api.controller

import com.msa.common.response.ApiResponse
import com.msa.wedding.application.*
import com.msa.wedding.domain.entity.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@Tag(name = "Wedding Hall", description = "웨딩홀 API")
@RestController
@RequestMapping("/api/v1/halls")
class WeddingHallController(
    private val weddingHallService: WeddingHallService
) {
    @Operation(summary = "웨딩홀 목록")
    @GetMapping
    fun getHalls(
        @RequestParam(required = false) keyword: String?,
        @PageableDefault(size = 20) pageable: Pageable
    ): ApiResponse<Page<HallResponse>> {
        val halls = if (keyword != null) {
            weddingHallService.searchHalls(keyword, pageable)
        } else {
            weddingHallService.getHalls(pageable)
        }
        return ApiResponse.success(halls.map { HallResponse.from(it) })
    }

    @Operation(summary = "웨딩홀 상세")
    @GetMapping("/{id}")
    fun getHall(@PathVariable id: Long): ApiResponse<HallResponse> =
        ApiResponse.success(HallResponse.from(weddingHallService.getHall(id)))

    @Operation(summary = "홀 유형별 목록")
    @GetMapping("/type/{hallType}")
    fun getHallsByType(
        @PathVariable hallType: HallType,
        @PageableDefault(size = 20) pageable: Pageable
    ): ApiResponse<Page<HallResponse>> =
        ApiResponse.success(weddingHallService.getHallsByType(hallType, pageable).map { HallResponse.from(it) })

    @Operation(summary = "수용인원별 목록")
    @GetMapping("/capacity")
    fun getHallsByCapacity(
        @RequestParam guests: Int,
        @PageableDefault(size = 20) pageable: Pageable
    ): ApiResponse<Page<HallResponse>> =
        ApiResponse.success(weddingHallService.getHallsByCapacity(guests, pageable).map { HallResponse.from(it) })

    @Operation(summary = "웨딩홀 등록")
    @PostMapping
    fun createHall(@RequestBody request: HallCreateRequest): ApiResponse<HallResponse> =
        ApiResponse.success(HallResponse.from(weddingHallService.createHall(request)))

    @Operation(summary = "웨딩홀 수정")
    @PutMapping("/{id}")
    fun updateHall(
        @PathVariable id: Long,
        @RequestBody request: HallUpdateRequest
    ): ApiResponse<HallResponse> =
        ApiResponse.success(HallResponse.from(weddingHallService.updateHall(id, request)))

    @Operation(summary = "웨딩홀 삭제")
    @DeleteMapping("/{id}")
    fun deleteHall(@PathVariable id: Long): ApiResponse<Unit> {
        weddingHallService.deleteHall(id)
        return ApiResponse.success()
    }
}

@Tag(name = "Dress", description = "드레스 API")
@RestController
@RequestMapping("/api/v1/dresses")
class DressController(
    private val dressService: DressService
) {
    @Operation(summary = "드레스 목록")
    @GetMapping
    fun getDresses(
        @RequestParam(required = false) keyword: String?,
        @PageableDefault(size = 20) pageable: Pageable
    ): ApiResponse<Page<DressResponse>> {
        val dresses = if (keyword != null) {
            dressService.searchDresses(keyword, pageable)
        } else {
            dressService.getDresses(pageable)
        }
        return ApiResponse.success(dresses.map { DressResponse.from(it) })
    }

    @Operation(summary = "드레스 상세")
    @GetMapping("/{id}")
    fun getDress(@PathVariable id: Long): ApiResponse<DressResponse> =
        ApiResponse.success(DressResponse.from(dressService.getDress(id)))

    @Operation(summary = "드레스 유형별 목록")
    @GetMapping("/type/{dressType}")
    fun getDressesByType(
        @PathVariable dressType: DressType,
        @PageableDefault(size = 20) pageable: Pageable
    ): ApiResponse<Page<DressResponse>> =
        ApiResponse.success(dressService.getDressesByType(dressType, pageable).map { DressResponse.from(it) })

    @Operation(summary = "드레스 등록")
    @PostMapping
    fun createDress(@RequestBody request: DressCreateRequest): ApiResponse<DressResponse> =
        ApiResponse.success(DressResponse.from(dressService.createDress(request)))

    @Operation(summary = "드레스 수정")
    @PutMapping("/{id}")
    fun updateDress(
        @PathVariable id: Long,
        @RequestBody request: DressUpdateRequest
    ): ApiResponse<DressResponse> =
        ApiResponse.success(DressResponse.from(dressService.updateDress(id, request)))

    @Operation(summary = "드레스 삭제")
    @DeleteMapping("/{id}")
    fun deleteDress(@PathVariable id: Long): ApiResponse<Unit> {
        dressService.deleteDress(id)
        return ApiResponse.success()
    }
}

@Tag(name = "Makeup Shop", description = "메이크업샵 API")
@RestController
@RequestMapping("/api/v1/makeup-shops")
class MakeupShopController(
    private val makeupShopService: MakeupShopService
) {
    @Operation(summary = "메이크업샵 목록")
    @GetMapping
    fun getShops(@PageableDefault(size = 20) pageable: Pageable): ApiResponse<Page<MakeupShopResponse>> =
        ApiResponse.success(makeupShopService.getShops(pageable).map { MakeupShopResponse.from(it) })

    @Operation(summary = "메이크업샵 상세")
    @GetMapping("/{id}")
    fun getShop(@PathVariable id: Long): ApiResponse<MakeupShopResponse> =
        ApiResponse.success(MakeupShopResponse.from(makeupShopService.getShop(id)))

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
        ApiResponse.success(MakeupShopResponse.from(makeupShopService.createShop(request)))

    @Operation(summary = "메이크업샵 수정")
    @PutMapping("/{id}")
    fun updateShop(
        @PathVariable id: Long,
        @RequestBody request: MakeupShopUpdateRequest
    ): ApiResponse<MakeupShopResponse> =
        ApiResponse.success(MakeupShopResponse.from(makeupShopService.updateShop(id, request)))

    @Operation(summary = "메이크업샵 삭제")
    @DeleteMapping("/{id}")
    fun deleteShop(@PathVariable id: Long): ApiResponse<Unit> {
        makeupShopService.deleteShop(id)
        return ApiResponse.success()
    }
}

@Tag(name = "Wedding Plan", description = "웨딩 플랜 API")
@RestController
@RequestMapping("/api/v1/plans")
class WeddingPlanController(
    private val weddingPlanService: WeddingPlanService
) {
    @Operation(summary = "내 웨딩 플랜 목록")
    @GetMapping
    fun getMyPlans(
        @AuthenticationPrincipal userId: Long,
        @PageableDefault(size = 10) pageable: Pageable
    ): ApiResponse<Page<PlanResponse>> =
        ApiResponse.success(weddingPlanService.getMyPlans(userId, pageable).map { PlanResponse.from(it) })

    @Operation(summary = "웨딩 플랜 상세")
    @GetMapping("/{planId}")
    fun getPlan(
        @AuthenticationPrincipal userId: Long,
        @PathVariable planId: Long
    ): ApiResponse<PlanResponse> =
        ApiResponse.success(PlanResponse.from(weddingPlanService.getPlan(userId, planId)))

    @Operation(summary = "웨딩 플랜 생성")
    @PostMapping
    fun createPlan(
        @AuthenticationPrincipal userId: Long,
        @RequestBody request: PlanCreateRequest
    ): ApiResponse<PlanResponse> =
        ApiResponse.success(PlanResponse.from(weddingPlanService.createPlan(userId, request)))

    @Operation(summary = "웨딩 플랜 수정")
    @PutMapping("/{planId}")
    fun updatePlan(
        @AuthenticationPrincipal userId: Long,
        @PathVariable planId: Long,
        @RequestBody request: PlanUpdateRequest
    ): ApiResponse<PlanResponse> =
        ApiResponse.success(PlanResponse.from(weddingPlanService.updatePlan(userId, planId, request)))

    @Operation(summary = "웨딩 플랜 삭제")
    @DeleteMapping("/{planId}")
    fun deletePlan(
        @AuthenticationPrincipal userId: Long,
        @PathVariable planId: Long
    ): ApiResponse<Unit> {
        weddingPlanService.deletePlan(userId, planId)
        return ApiResponse.success()
    }
}

// Response DTOs
data class HallResponse(
    val id: Long,
    val name: String,
    val address: String,
    val hallType: HallType,
    val description: String?,
    val minGuarantee: Int?,
    val maxCapacity: Int?,
    val mealPrice: Int?,
    val hallRentalPrice: Int?,
    val avgRating: Double
) {
    companion object {
        fun from(h: WeddingHall) = HallResponse(
            h.id, h.name, h.address, h.hallType, h.description,
            h.minGuarantee, h.maxCapacity, h.mealPrice, h.hallRentalPrice, h.avgRating
        )
    }
}

data class DressResponse(
    val id: Long,
    val shopName: String,
    val address: String,
    val dressType: DressType,
    val minPrice: Int?,
    val maxPrice: Int?,
    val avgRating: Double
) {
    companion object {
        fun from(d: Dress) = DressResponse(
            d.id, d.shopName, d.address, d.dressType, d.minPrice, d.maxPrice, d.avgRating
        )
    }
}

data class MakeupShopResponse(
    val id: Long,
    val name: String,
    val address: String,
    val serviceType: MakeupServiceType,
    val basePrice: Int?,
    val avgRating: Double,
    val onSiteAvailable: Boolean
) {
    companion object {
        fun from(m: MakeupShop) = MakeupShopResponse(
            m.id, m.name, m.address, m.serviceType, m.basePrice, m.avgRating, m.onSiteAvailable
        )
    }
}

data class PlanResponse(
    val id: Long,
    val title: String,
    val weddingDate: LocalDate?,
    val budget: Long?,
    val expectedGuests: Int?,
    val totalEstimate: Long
) {
    companion object {
        fun from(p: WeddingPlan) = PlanResponse(
            p.id, p.title, p.weddingDate, p.budget, p.expectedGuests, p.calculateTotalEstimate()
        )
    }
}
