package com.msa.festival.api.controller

import com.msa.common.exception.NotFoundException
import com.msa.common.response.ApiResponse
import com.msa.festival.domain.entity.Festival
import com.msa.festival.domain.entity.FestivalCategory
import com.msa.festival.domain.repository.FestivalRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@Tag(name = "Festival", description = "축제/행사 API")
@RestController
@RequestMapping("/api/v1/festivals")
class FestivalController(
    private val festivalRepository: FestivalRepository
) {
    @Operation(summary = "축제 상세")
    @GetMapping("/{id}")
    fun getFestival(@PathVariable id: Long): ApiResponse<FestivalResponse> {
        val festival = festivalRepository.findById(id).orElseThrow { NotFoundException("Festival", id) }
        festival.viewCount++
        festivalRepository.save(festival)
        return ApiResponse.success(FestivalResponse.from(festival))
    }

    @Operation(summary = "진행 중인 축제")
    @GetMapping("/ongoing")
    fun getOngoingFestivals(@PageableDefault(size = 20) pageable: Pageable): ApiResponse<Page<FestivalResponse>> {
        return ApiResponse.success(festivalRepository.findOngoingFestivals(LocalDate.now(), pageable).map { FestivalResponse.from(it) })
    }

    @Operation(summary = "카테고리별 축제")
    @GetMapping("/category/{category}")
    fun getFestivalsByCategory(
        @PathVariable category: FestivalCategory,
        @PageableDefault(size = 20) pageable: Pageable
    ): ApiResponse<Page<FestivalResponse>> {
        return ApiResponse.success(festivalRepository.findByCategory(category, pageable).map { FestivalResponse.from(it) })
    }

    @Operation(summary = "축제 검색")
    @GetMapping("/search")
    fun searchFestivals(
        @RequestParam keyword: String,
        @PageableDefault(size = 20) pageable: Pageable
    ): ApiResponse<Page<FestivalResponse>> {
        return ApiResponse.success(festivalRepository.searchByKeyword(keyword, pageable).map { FestivalResponse.from(it) })
    }

    @Operation(summary = "인기 축제")
    @GetMapping("/popular")
    fun getPopularFestivals(@PageableDefault(size = 20) pageable: Pageable): ApiResponse<Page<FestivalResponse>> {
        return ApiResponse.success(festivalRepository.findPopular(pageable).map { FestivalResponse.from(it) })
    }
}

data class FestivalResponse(
    val id: Long, val title: String, val category: FestivalCategory, val address: String?,
    val startDate: LocalDate?, val endDate: LocalDate?, val coverImage: String?,
    val avgRating: Double, val reviewCount: Int, val isOngoing: Boolean
) {
    companion object {
        fun from(f: Festival) = FestivalResponse(
            id = f.id, title = f.title, category = f.category, address = f.address,
            startDate = f.startDate, endDate = f.endDate, coverImage = f.coverImage,
            avgRating = f.avgRating, reviewCount = f.reviewCount, isOngoing = f.isOngoing()
        )
    }
}
