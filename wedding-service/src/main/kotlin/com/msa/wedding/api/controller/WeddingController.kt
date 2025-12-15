package com.msa.wedding.api.controller

import com.msa.common.exception.NotFoundException
import com.msa.common.response.ApiResponse
import com.msa.wedding.domain.entity.*
import com.msa.wedding.domain.repository.*
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
class WeddingHallController(private val weddingHallRepository: WeddingHallRepository) {
    @GetMapping
    fun getHalls(@PageableDefault(size = 20) pageable: Pageable): ApiResponse<Page<HallResponse>> =
        ApiResponse.success(weddingHallRepository.findAll(pageable).map { HallResponse.from(it) })

    @GetMapping("/{id}")
    fun getHall(@PathVariable id: Long): ApiResponse<HallResponse> =
        ApiResponse.success(HallResponse.from(weddingHallRepository.findById(id).orElseThrow { NotFoundException("WeddingHall", id) }))

    @GetMapping("/type/{hallType}")
    fun getHallsByType(@PathVariable hallType: HallType, @PageableDefault(size = 20) pageable: Pageable): ApiResponse<Page<HallResponse>> =
        ApiResponse.success(weddingHallRepository.findByHallType(hallType, pageable).map { HallResponse.from(it) })

    @GetMapping("/capacity")
    fun getHallsByCapacity(@RequestParam guests: Int, @PageableDefault(size = 20) pageable: Pageable): ApiResponse<Page<HallResponse>> =
        ApiResponse.success(weddingHallRepository.findByCapacity(guests, pageable).map { HallResponse.from(it) })
}

@Tag(name = "Dress", description = "드레스 API")
@RestController
@RequestMapping("/api/v1/dresses")
class DressController(private val dressRepository: DressRepository) {
    @GetMapping
    fun getDresses(@PageableDefault(size = 20) pageable: Pageable): ApiResponse<Page<DressResponse>> =
        ApiResponse.success(dressRepository.findAll(pageable).map { DressResponse.from(it) })

    @GetMapping("/{id}")
    fun getDress(@PathVariable id: Long): ApiResponse<DressResponse> =
        ApiResponse.success(DressResponse.from(dressRepository.findById(id).orElseThrow { NotFoundException("Dress", id) }))

    @GetMapping("/type/{dressType}")
    fun getDressesByType(@PathVariable dressType: DressType, @PageableDefault(size = 20) pageable: Pageable): ApiResponse<Page<DressResponse>> =
        ApiResponse.success(dressRepository.findByDressType(dressType, pageable).map { DressResponse.from(it) })
}

@Tag(name = "Makeup Shop", description = "메이크업샵 API")
@RestController
@RequestMapping("/api/v1/makeup-shops")
class MakeupShopController(private val makeupShopRepository: MakeupShopRepository) {
    @GetMapping
    fun getShops(@PageableDefault(size = 20) pageable: Pageable): ApiResponse<Page<MakeupShopResponse>> =
        ApiResponse.success(makeupShopRepository.findAll(pageable).map { MakeupShopResponse.from(it) })

    @GetMapping("/{id}")
    fun getShop(@PathVariable id: Long): ApiResponse<MakeupShopResponse> =
        ApiResponse.success(MakeupShopResponse.from(makeupShopRepository.findById(id).orElseThrow { NotFoundException("MakeupShop", id) }))
}

@Tag(name = "Wedding Plan", description = "웨딩 플랜 API")
@RestController
@RequestMapping("/api/v1/plans")
class WeddingPlanController(private val weddingPlanRepository: WeddingPlanRepository) {
    @GetMapping
    fun getMyPlans(@AuthenticationPrincipal userId: Long, @PageableDefault(size = 10) pageable: Pageable): ApiResponse<Page<PlanResponse>> =
        ApiResponse.success(weddingPlanRepository.findByUserId(userId, pageable).map { PlanResponse.from(it) })

    @GetMapping("/{planId}")
    fun getPlan(@AuthenticationPrincipal userId: Long, @PathVariable planId: Long): ApiResponse<PlanResponse> =
        ApiResponse.success(PlanResponse.from(weddingPlanRepository.findByUserIdAndId(userId, planId) ?: throw NotFoundException("WeddingPlan", planId)))

    @PostMapping
    fun createPlan(@AuthenticationPrincipal userId: Long, @RequestBody request: PlanCreateRequest): ApiResponse<PlanResponse> =
        ApiResponse.success(PlanResponse.from(weddingPlanRepository.save(WeddingPlan(userId = userId, title = request.title, weddingDate = request.weddingDate, budget = request.budget, expectedGuests = request.expectedGuests))))
}

// DTOs
data class HallResponse(val id: Long, val name: String, val address: String, val hallType: HallType, val minGuarantee: Int?, val maxCapacity: Int?, val mealPrice: Int?, val avgRating: Double) {
    companion object { fun from(h: WeddingHall) = HallResponse(h.id, h.name, h.address, h.hallType, h.minGuarantee, h.maxCapacity, h.mealPrice, h.avgRating) }
}
data class DressResponse(val id: Long, val shopName: String, val address: String, val dressType: DressType, val minPrice: Int?, val maxPrice: Int?, val avgRating: Double) {
    companion object { fun from(d: Dress) = DressResponse(d.id, d.shopName, d.address, d.dressType, d.minPrice, d.maxPrice, d.avgRating) }
}
data class MakeupShopResponse(val id: Long, val name: String, val address: String, val serviceType: MakeupServiceType, val basePrice: Int?, val avgRating: Double, val onSiteAvailable: Boolean) {
    companion object { fun from(m: MakeupShop) = MakeupShopResponse(m.id, m.name, m.address, m.serviceType, m.basePrice, m.avgRating, m.onSiteAvailable) }
}
data class PlanResponse(val id: Long, val title: String, val weddingDate: LocalDate?, val budget: Long?, val expectedGuests: Int?, val totalEstimate: Long) {
    companion object { fun from(p: WeddingPlan) = PlanResponse(p.id, p.title, p.weddingDate, p.budget, p.expectedGuests, p.calculateTotalEstimate()) }
}
data class PlanCreateRequest(val title: String, val weddingDate: LocalDate?, val budget: Long?, val expectedGuests: Int?)
