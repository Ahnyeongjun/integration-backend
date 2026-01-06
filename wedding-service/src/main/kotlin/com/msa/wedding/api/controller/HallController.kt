package com.msa.wedding.api.controller

import com.msa.common.response.ApiResponse
import com.msa.wedding.application.*
import com.msa.wedding.domain.entity.Hall
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@Tag(name = "Hall", description = "홀(개별 홀룸) API")
@RestController
@RequestMapping("/api/v1/halls")
class HallController(
    private val hallService: HallService
) {
    @Operation(summary = "전체 홀 목록")
    @GetMapping
    fun getAllHalls(): ApiResponse<List<HallResponse>> =
        ApiResponse.success(hallService.getAllHalls().map { HallResponse.from(it) })

    @Operation(summary = "홀 상세")
    @GetMapping("/{id}")
    fun getHall(@PathVariable id: Long): ApiResponse<HallResponse> =
        ApiResponse.success(HallResponse.from(hallService.getHall(id)))

    @Operation(summary = "홀 등록")
    @PostMapping
    fun createHall(
        @RequestParam weddingHallId: Long,
        @RequestBody request: HallCreateRequest
    ): ApiResponse<HallResponse> =
        ApiResponse.success(HallResponse.from(hallService.createHall(weddingHallId, request)))

    @Operation(summary = "홀 수정")
    @PutMapping("/{id}")
    fun updateHall(
        @PathVariable id: Long,
        @RequestBody request: HallUpdateRequest
    ): ApiResponse<HallResponse> =
        ApiResponse.success(HallResponse.from(hallService.updateHall(id, request)))

    @Operation(summary = "홀 삭제")
    @DeleteMapping("/{id}")
    fun deleteHall(@PathVariable id: Long): ApiResponse<Unit> {
        hallService.deleteHall(id)
        return ApiResponse.success()
    }
}

// WeddingHall 하위의 Halls를 조회하는 컨트롤러
@Tag(name = "Wedding Hall - Halls", description = "웨딩홀별 홀 목록 API")
@RestController
@RequestMapping("/api/v1/wedding-halls")
class WeddingHallHallsController(
    private val hallService: HallService
) {
    @Operation(summary = "웨딩홀의 홀 목록")
    @GetMapping("/{weddingHallId}/halls")
    fun getHallsByWeddingHall(@PathVariable weddingHallId: Long): ApiResponse<List<HallResponse>> =
        ApiResponse.success(hallService.getHallsByWeddingHall(weddingHallId).map { HallResponse.from(it) })

    @Operation(summary = "웨딩홀에 홀 추가")
    @PostMapping("/{weddingHallId}/halls")
    fun createHallInWeddingHall(
        @PathVariable weddingHallId: Long,
        @RequestBody request: HallCreateRequest
    ): ApiResponse<HallResponse> =
        ApiResponse.success(HallResponse.from(hallService.createHall(weddingHallId, request)))
}

data class HallResponse(
    val id: Long,
    val weddingHallId: Long,
    val name: String,
    val floor: Int?,
    val minCapacity: Int?,
    val maxCapacity: Int?,
    val rentalPrice: Int?,
    val mealPrice: Int?,
    val description: String?,
    val imageUrl: String?,
    val isAvailable: Boolean
) {
    companion object {
        fun from(h: Hall) = HallResponse(
            id = h.id,
            weddingHallId = h.weddingHall.id,
            name = h.name,
            floor = h.floor,
            minCapacity = h.minCapacity,
            maxCapacity = h.maxCapacity,
            rentalPrice = h.rentalPrice,
            mealPrice = h.mealPrice,
            description = h.description,
            imageUrl = h.imageUrl,
            isAvailable = h.isAvailable
        )
    }
}
