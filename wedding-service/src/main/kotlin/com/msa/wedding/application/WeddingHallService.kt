package com.msa.wedding.application

import com.msa.common.enums.ServiceType
import com.msa.common.exception.NotFoundException
import com.msa.wedding.api.controller.WeddingHallResponse
import com.msa.wedding.domain.entity.HallType
import com.msa.wedding.domain.entity.SortType
import com.msa.wedding.domain.entity.WeddingHall
import com.msa.wedding.domain.repository.WeddingHallRepository
import com.msa.wedding.infrastructure.BookmarkClient
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class WeddingHallService(
    private val weddingHallRepository: WeddingHallRepository,
    private val bookmarkClient: BookmarkClient
) {
    companion object {
        private const val TARGET_TYPE = "WEDDING_HALL"
    }

    fun getHalls(
        keyword: String?,
        sort: SortType?,
        userId: Long?,
        pageable: Pageable
    ): Page<WeddingHallResponse> {
        val halls = when {
            keyword != null -> weddingHallRepository.searchByKeywordOrderByCreatedAtDesc(keyword, pageable)
            else -> weddingHallRepository.findAllOrderByCreatedAtDesc(pageable)
        }

        return enrichWithBookmarkInfo(halls, sort, userId)
    }

    fun getHall(id: Long): WeddingHall =
        weddingHallRepository.findById(id)
            .orElseThrow { NotFoundException("WeddingHall", id) }

    fun getHallWithLikeStatus(id: Long, userId: Long?): WeddingHallResponse {
        val hall = getHall(id)
        val bookmarkCount = bookmarkClient.getBookmarkCount(ServiceType.WEDDING, TARGET_TYPE, id)
        val isLiked = userId?.let {
            bookmarkClient.isBookmarked(it, ServiceType.WEDDING, TARGET_TYPE, id)
        } ?: false

        return WeddingHallResponse.from(hall, bookmarkCount, isLiked)
    }

    fun getHallsByType(
        hallType: HallType,
        sort: SortType?,
        userId: Long?,
        pageable: Pageable
    ): Page<WeddingHallResponse> {
        val halls = weddingHallRepository.findByHallTypeOrderByCreatedAtDesc(hallType, pageable)
        return enrichWithBookmarkInfo(halls, sort, userId)
    }

    fun getHallsByCapacity(
        guests: Int,
        sort: SortType?,
        userId: Long?,
        pageable: Pageable
    ): Page<WeddingHallResponse> {
        val halls = weddingHallRepository.findByCapacityOrderByCreatedAtDesc(guests, pageable)
        return enrichWithBookmarkInfo(halls, sort, userId)
    }

    @Transactional
    fun createHall(request: HallCreateRequest): WeddingHallResponse {
        val hall = WeddingHall(
            name = request.name,
            address = request.address,
            hallType = request.hallType,
            description = request.description,
            minGuarantee = request.minGuarantee,
            maxCapacity = request.maxCapacity,
            mealPrice = request.mealPrice,
            hallRentalPrice = request.hallRentalPrice,
            phone = request.phone,
            email = request.email,
            parking = request.parking
        )
        val saved = weddingHallRepository.save(hall)
        return WeddingHallResponse.from(saved)
    }

    @Transactional
    fun updateHall(id: Long, request: HallUpdateRequest): WeddingHallResponse {
        val hall = getHall(id)
        request.name?.let { hall.name = it }
        request.address?.let { hall.address = it }
        request.hallType?.let { hall.hallType = it }
        request.description?.let { hall.description = it }
        request.minGuarantee?.let { hall.minGuarantee = it }
        request.maxCapacity?.let { hall.maxCapacity = it }
        request.mealPrice?.let { hall.mealPrice = it }
        request.hallRentalPrice?.let { hall.hallRentalPrice = it }
        request.phone?.let { hall.phone = it }
        request.email?.let { hall.email = it }
        request.parking?.let { hall.parking = it }

        val saved = weddingHallRepository.save(hall)
        return WeddingHallResponse.from(saved)
    }

    @Transactional
    fun deleteHall(id: Long) {
        if (!weddingHallRepository.existsById(id)) {
            throw NotFoundException("WeddingHall", id)
        }
        weddingHallRepository.deleteById(id)
    }

    private fun enrichWithBookmarkInfo(
        halls: Page<WeddingHall>,
        sort: SortType?,
        userId: Long?
    ): Page<WeddingHallResponse> {
        if (halls.isEmpty) {
            return Page.empty(halls.pageable)
        }

        val ids = halls.content.map { it.id }
        val bookmarkCounts = bookmarkClient.getBookmarkCountBatch(ServiceType.WEDDING, TARGET_TYPE, ids)
        val likedIds = userId?.let {
            bookmarkClient.getBookmarkedIds(it, ServiceType.WEDDING, TARGET_TYPE, ids)
        } ?: emptySet()

        var responses = halls.content.map { hall ->
            WeddingHallResponse.from(
                hall,
                bookmarkCounts[hall.id] ?: 0L,
                likedIds.contains(hall.id)
            )
        }

        // FAVORITE 정렬: 북마크 수 기준 내림차순
        if (sort == SortType.FAVORITE) {
            responses = responses.sortedByDescending { it.bookmarkCount }
        }

        return PageImpl(responses, halls.pageable, halls.totalElements)
    }
}

data class HallCreateRequest(
    val name: String,
    val address: String,
    val hallType: HallType,
    val description: String? = null,
    val minGuarantee: Int? = null,
    val maxCapacity: Int? = null,
    val mealPrice: Int? = null,
    val hallRentalPrice: Int? = null,
    val phone: String? = null,
    val email: String? = null,
    val parking: Int? = null
)

data class HallUpdateRequest(
    val name: String? = null,
    val address: String? = null,
    val hallType: HallType? = null,
    val description: String? = null,
    val minGuarantee: Int? = null,
    val maxCapacity: Int? = null,
    val mealPrice: Int? = null,
    val hallRentalPrice: Int? = null,
    val phone: String? = null,
    val email: String? = null,
    val parking: Int? = null
)
