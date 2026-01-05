package com.msa.wedding.application

import com.msa.common.exception.NotFoundException
import com.msa.wedding.domain.entity.HallType
import com.msa.wedding.domain.entity.WeddingHall
import com.msa.wedding.domain.repository.WeddingHallRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class WeddingHallService(
    private val weddingHallRepository: WeddingHallRepository
) {
    fun getHalls(pageable: Pageable): Page<WeddingHall> =
        weddingHallRepository.findAll(pageable)

    fun getHall(id: Long): WeddingHall =
        weddingHallRepository.findById(id)
            .orElseThrow { NotFoundException("WeddingHall", id) }

    fun getHallsByType(hallType: HallType, pageable: Pageable): Page<WeddingHall> =
        weddingHallRepository.findByHallType(hallType, pageable)

    fun getHallsByCapacity(guests: Int, pageable: Pageable): Page<WeddingHall> =
        weddingHallRepository.findByCapacity(guests, pageable)

    fun searchHalls(keyword: String, pageable: Pageable): Page<WeddingHall> =
        weddingHallRepository.searchByKeyword(keyword, pageable)

    @Transactional
    fun createHall(request: HallCreateRequest): WeddingHall {
        val hall = WeddingHall(
            name = request.name,
            address = request.address,
            hallType = request.hallType,
            description = request.description,
            minGuarantee = request.minGuarantee,
            maxCapacity = request.maxCapacity,
            mealPrice = request.mealPrice,
            hallRentalPrice = request.hallRentalPrice
        )
        return weddingHallRepository.save(hall)
    }

    @Transactional
    fun updateHall(id: Long, request: HallUpdateRequest): WeddingHall {
        val hall = getHall(id)
        request.name?.let { hall.name = it }
        request.address?.let { hall.address = it }
        request.hallType?.let { hall.hallType = it }
        request.description?.let { hall.description = it }
        request.minGuarantee?.let { hall.minGuarantee = it }
        request.maxCapacity?.let { hall.maxCapacity = it }
        request.mealPrice?.let { hall.mealPrice = it }
        request.hallRentalPrice?.let { hall.hallRentalPrice = it }
        return weddingHallRepository.save(hall)
    }

    @Transactional
    fun deleteHall(id: Long) {
        if (!weddingHallRepository.existsById(id)) {
            throw NotFoundException("WeddingHall", id)
        }
        weddingHallRepository.deleteById(id)
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
    val hallRentalPrice: Int? = null
)

data class HallUpdateRequest(
    val name: String? = null,
    val address: String? = null,
    val hallType: HallType? = null,
    val description: String? = null,
    val minGuarantee: Int? = null,
    val maxCapacity: Int? = null,
    val mealPrice: Int? = null,
    val hallRentalPrice: Int? = null
)
