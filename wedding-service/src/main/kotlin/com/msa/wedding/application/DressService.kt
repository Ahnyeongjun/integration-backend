package com.msa.wedding.application

import com.msa.common.exception.NotFoundException
import com.msa.wedding.api.controller.DressResponse
import com.msa.wedding.domain.entity.*
import com.msa.wedding.domain.repository.DressRepository
import com.msa.wedding.domain.repository.DressShopRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DressService(
    private val dressRepository: DressRepository,
    private val dressShopRepository: DressShopRepository
) {
    fun getDresses(
        keyword: String?,
        sort: SortType?,
        pageable: Pageable
    ): Page<DressResponse> {
        val dresses = when {
            keyword != null -> dressRepository.searchByKeyword(keyword, pageable)
            else -> dressRepository.findAllOrderByCreatedAtDesc(pageable)
        }
        return dresses.map { DressResponse.from(it) }
    }

    fun getDress(id: Long): Dress =
        dressRepository.findById(id)
            .orElseThrow { NotFoundException("Dress", id) }

    fun getDressByIdWithResponse(id: Long): DressResponse =
        DressResponse.from(getDress(id))

    fun getDressesByType(dressType: DressType, pageable: Pageable): Page<DressResponse> =
        dressRepository.findByDressTypeOrderByCreatedAtDesc(dressType, pageable)
            .map { DressResponse.from(it) }

    @Transactional
    fun createDress(request: DressCreateRequest): DressResponse {
        val shop = dressShopRepository.findById(request.dressShopId)
            .orElseThrow { NotFoundException("DressShop", request.dressShopId) }

        val dress = Dress(
            name = request.name,
            color = request.color,
            shape = request.shape,
            priceRange = request.priceRange,
            length = request.length,
            season = request.season,
            designer = request.designer,
            dressType = request.dressType,
            neckLine = request.neckLine,
            mood = request.mood,
            fabric = request.fabric,
            imageUrl = request.imageUrl,
            features = request.features,
            dressShop = shop
        )
        val saved = dressRepository.save(dress)
        return DressResponse.from(saved)
    }

    @Transactional
    fun updateDress(id: Long, request: DressUpdateRequest): DressResponse {
        val dress = getDress(id)
        request.name?.let { dress.name = it }
        request.color?.let { dress.color = it }
        request.shape?.let { dress.shape = it }
        request.priceRange?.let { dress.priceRange = it }
        request.length?.let { dress.length = it }
        request.season?.let { dress.season = it }
        request.designer?.let { dress.designer = it }
        request.dressType?.let { dress.dressType = it }
        request.neckLine?.let { dress.neckLine = it }
        request.mood?.let { dress.mood = it }
        request.fabric?.let { dress.fabric = it }
        request.imageUrl?.let { dress.imageUrl = it }
        request.features?.let { dress.features = it }

        val saved = dressRepository.save(dress)
        return DressResponse.from(saved)
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
    val dressShopId: Long,
    val name: String? = null,
    val color: String? = null,
    val shape: String? = null,
    val priceRange: String? = null,
    val length: DressLength? = null,
    val season: DressSeason? = null,
    val designer: String? = null,
    val dressType: DressType? = null,
    val neckLine: DressNeckline? = null,
    val mood: DressMood? = null,
    val fabric: String? = null,
    val imageUrl: String? = null,
    val features: String? = null
)

data class DressUpdateRequest(
    val name: String? = null,
    val color: String? = null,
    val shape: String? = null,
    val priceRange: String? = null,
    val length: DressLength? = null,
    val season: DressSeason? = null,
    val designer: String? = null,
    val dressType: DressType? = null,
    val neckLine: DressNeckline? = null,
    val mood: DressMood? = null,
    val fabric: String? = null,
    val imageUrl: String? = null,
    val features: String? = null
)
