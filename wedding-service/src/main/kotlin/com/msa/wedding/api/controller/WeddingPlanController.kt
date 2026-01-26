package com.msa.wedding.api.controller

import com.msa.common.response.ApiResponse
import com.msa.wedding.application.*
import com.msa.wedding.domain.entity.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import com.msa.common.security.UserPrincipal
import org.springframework.data.web.PageableDefault
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@Tag(name = "Wedding Plan", description = "웨딩 플랜 API")
@RestController
@RequestMapping("/api/v1/plans")
class WeddingPlanController(
    private val weddingPlanService: WeddingPlanService
) {
    @Operation(summary = "내 웨딩 플랜 목록")
    @GetMapping
    fun getMyPlans(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PageableDefault(size = 10) pageable: Pageable
    ): ApiResponse<Page<WeddingPlanResponse>> =
        ApiResponse.success(weddingPlanService.getMyPlans(principal.userId, pageable).map { WeddingPlanResponse.from(it) })

    @Operation(summary = "웨딩 플랜 상세")
    @GetMapping("/{planId}")
    fun getPlan(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable planId: Long
    ): ApiResponse<WeddingPlanResponse> =
        ApiResponse.success(WeddingPlanResponse.from(weddingPlanService.getPlan(principal.userId, planId)))

    @Operation(summary = "웨딩 플랜 생성")
    @PostMapping
    fun createPlan(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: PlanCreateRequest
    ): ApiResponse<WeddingPlanResponse> =
        ApiResponse.success(WeddingPlanResponse.from(weddingPlanService.createPlan(principal.userId, request)))

    @Operation(summary = "웨딩 플랜 수정")
    @PutMapping("/{planId}")
    fun updatePlan(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable planId: Long,
        @RequestBody request: PlanUpdateRequest
    ): ApiResponse<WeddingPlanResponse> =
        ApiResponse.success(WeddingPlanResponse.from(weddingPlanService.updatePlan(principal.userId, planId, request)))

    @Operation(summary = "웨딩 플랜 삭제")
    @DeleteMapping("/{planId}")
    fun deletePlan(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable planId: Long
    ): ApiResponse<Unit> {
        weddingPlanService.deletePlan(principal.userId, planId)
        return ApiResponse.success()
    }
}

data class WeddingPlanResponse(
    val id: Long,
    val title: String,
    val weddingDate: LocalDate?,
    val budget: Long?,
    val expectedGuests: Int?,
    val totalEstimate: Long,
    val selectedHall: WeddingHallResponse?,
    val selectedDressShop: DressShopResponse?,
    val selectedMakeupShop: MakeupShopResponse?
) {
    companion object {
        fun from(p: WeddingPlan) = WeddingPlanResponse(
            id = p.id,
            title = p.title,
            weddingDate = p.weddingDate,
            budget = p.budget,
            expectedGuests = p.expectedGuests,
            totalEstimate = p.calculateTotalEstimate(),
            selectedHall = p.selectedHall?.let { WeddingHallResponse.from(it) },
            selectedDressShop = p.selectedDressShop?.let { DressShopResponse.from(it) },
            selectedMakeupShop = p.selectedMakeupShop?.let { MakeupShopResponse.from(it) }
        )
    }
}
