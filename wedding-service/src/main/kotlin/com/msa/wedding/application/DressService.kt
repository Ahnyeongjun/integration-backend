package com.msa.wedding.application

import com.msa.common.exception.NotFoundException
import com.msa.wedding.domain.entity.Dress
import com.msa.wedding.domain.entity.DressType
import com.msa.wedding.domain.repository.DressRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DressService(
    private val dressRepository: DressRepository
) {
    fun getDresses(pageable: Pageable): Page<Dress> =
        dressRepository.findAll(pageable)

    fun getDress(id: Long): Dress =
        dressRepository.findById(id)
            .orElseThrow { NotFoundException("Dress", id) }

    fun getDressesByType(dressType: DressType, pageable: Pageable): Page<Dress> =
        dressRepository.findByDressType(dressType, pageable)

    fun searchDresses(keyword: String, pageable: Pageable): Page<Dress> =
        dressRepository.searchByKeyword(keyword, pageable)

    @Transactional
    fun createDress(request: DressCreateRequest): Dress {
        val dress = Dress(
            shopName = request.shopName,
            address = request.address,
            dressType = request.dressType,
            minPrice = request.minPrice,
            maxPrice = request.maxPrice
        )
        return dressRepository.save(dress)
    }

    @Transactional
    fun updateDress(id: Long, request: DressUpdateRequest): Dress {
        val dress = getDress(id)
        request.shopName?.let { dress.shopName = it }
        request.address?.let { dress.address = it }
        request.dressType?.let { dress.dressType = it }
        request.minPrice?.let { dress.minPrice = it }
        request.maxPrice?.let { dress.maxPrice = it }
        return dressRepository.save(dress)
    }

    @Transactional
    fun deleteDress(id: Long) {
        if (!dressRepository.existsById(id)) {
            throw NotFoundException("Dress", id)
        }
        dressRepository.deleteById(id)
    }
}

data class DressCreateRequest(
    val shopName: String,
    val address: String,
    val dressType: DressType,
    val minPrice: Int? = null,
    val maxPrice: Int? = null
)

data class DressUpdateRequest(
    val shopName: String? = null,
    val address: String? = null,
    val dressType: DressType? = null,
    val minPrice: Int? = null,
    val maxPrice: Int? = null
)
