package com.msa.wedding.application

import com.msa.common.exception.NotFoundException
import com.msa.wedding.domain.entity.MakeupServiceType
import com.msa.wedding.domain.entity.MakeupShop
import com.msa.wedding.domain.repository.MakeupShopRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class MakeupShopService(
    private val makeupShopRepository: MakeupShopRepository
) {
    fun getShops(pageable: Pageable): Page<MakeupShop> =
        makeupShopRepository.findAll(pageable)

    fun getShop(id: Long): MakeupShop =
        makeupShopRepository.findById(id)
            .orElseThrow { NotFoundException("MakeupShop", id) }

    fun getShopsByServiceType(serviceType: MakeupServiceType, pageable: Pageable): Page<MakeupShop> =
        makeupShopRepository.findByServiceType(serviceType, pageable)

    fun getOnSiteAvailableShops(pageable: Pageable): Page<MakeupShop> =
        makeupShopRepository.findOnSiteAvailable(pageable)

    @Transactional
    fun createShop(request: MakeupShopCreateRequest): MakeupShop {
        val shop = MakeupShop(
            name = request.name,
            address = request.address,
            serviceType = request.serviceType,
            basePrice = request.basePrice,
            onSiteAvailable = request.onSiteAvailable ?: true
        )
        return makeupShopRepository.save(shop)
    }

    @Transactional
    fun updateShop(id: Long, request: MakeupShopUpdateRequest): MakeupShop {
        val shop = getShop(id)
        request.name?.let { shop.name = it }
        request.address?.let { shop.address = it }
        request.serviceType?.let { shop.serviceType = it }
        request.basePrice?.let { shop.basePrice = it }
        request.onSiteAvailable?.let { shop.onSiteAvailable = it }
        return makeupShopRepository.save(shop)
    }

    @Transactional
    fun deleteShop(id: Long) {
        if (!makeupShopRepository.existsById(id)) {
            throw NotFoundException("MakeupShop", id)
        }
        makeupShopRepository.deleteById(id)
    }
}

data class MakeupShopCreateRequest(
    val name: String,
    val address: String,
    val serviceType: MakeupServiceType,
    val basePrice: Int? = null,
    val onSiteAvailable: Boolean? = true
)

data class MakeupShopUpdateRequest(
    val name: String? = null,
    val address: String? = null,
    val serviceType: MakeupServiceType? = null,
    val basePrice: Int? = null,
    val onSiteAvailable: Boolean? = null
)
