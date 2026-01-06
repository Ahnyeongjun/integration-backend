package com.msa.wedding.application

import com.msa.wedding.domain.entity.Hall
import com.msa.wedding.domain.repository.HallRepository
import com.msa.wedding.domain.repository.WeddingHallRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class HallService(
    private val hallRepository: HallRepository,
    private val weddingHallRepository: WeddingHallRepository
) {

    @Transactional(readOnly = true)
    fun getAllHalls(): List<Hall> = hallRepository.findAll()

    @Transactional(readOnly = true)
    fun getHall(id: Long): Hall =
        hallRepository.findById(id).orElseThrow { NoSuchElementException("Hall not found: $id") }

    @Transactional(readOnly = true)
    fun getHallsByWeddingHall(weddingHallId: Long): List<Hall> =
        hallRepository.findByWeddingHallId(weddingHallId)

    fun createHall(weddingHallId: Long, request: HallCreateRequest): Hall {
        val weddingHall = weddingHallRepository.findById(weddingHallId)
            .orElseThrow { NoSuchElementException("WeddingHall not found: $weddingHallId") }

        val hall = Hall(
            weddingHall = weddingHall,
            name = request.name,
            floor = request.floor,
            minCapacity = request.minCapacity,
            maxCapacity = request.maxCapacity,
            rentalPrice = request.rentalPrice,
            mealPrice = request.mealPrice,
            description = request.description,
            imageUrl = request.imageUrl
        )
        return hallRepository.save(hall)
    }

    fun updateHall(id: Long, request: HallUpdateRequest): Hall {
        val hall = getHall(id)
        request.name?.let { hall.name = it }
        request.floor?.let { hall.floor = it }
        request.minCapacity?.let { hall.minCapacity = it }
        request.maxCapacity?.let { hall.maxCapacity = it }
        request.rentalPrice?.let { hall.rentalPrice = it }
        request.mealPrice?.let { hall.mealPrice = it }
        request.description?.let { hall.description = it }
        request.imageUrl?.let { hall.imageUrl = it }
        request.isAvailable?.let { hall.isAvailable = it }
        return hallRepository.save(hall)
    }

    fun deleteHall(id: Long) {
        hallRepository.deleteById(id)
    }
}

data class HallCreateRequest(
    val name: String,
    val floor: Int? = null,
    val minCapacity: Int? = null,
    val maxCapacity: Int? = null,
    val rentalPrice: Int? = null,
    val mealPrice: Int? = null,
    val description: String? = null,
    val imageUrl: String? = null
)

data class HallUpdateRequest(
    val name: String? = null,
    val floor: Int? = null,
    val minCapacity: Int? = null,
    val maxCapacity: Int? = null,
    val rentalPrice: Int? = null,
    val mealPrice: Int? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val isAvailable: Boolean? = null
)
