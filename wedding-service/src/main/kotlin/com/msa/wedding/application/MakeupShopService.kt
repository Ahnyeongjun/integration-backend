package com.msa.wedding.application

import com.msa.common.enums.ServiceType
import com.msa.common.exception.NotFoundException
import com.msa.wedding.api.controller.MakeupShopResponse
import com.msa.wedding.domain.entity.MakeupServiceType
import com.msa.wedding.domain.entity.MakeupShop
import com.msa.wedding.domain.entity.SortType
import com.msa.wedding.domain.repository.MakeupShopRepository
import com.msa.wedding.infrastructure.BookmarkClient
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class MakeupShopService(
    private val makeupShopRepository: MakeupShopRepository,
    private val bookmarkClient: BookmarkClient
) {
    companion object {
        private const val TARGET_TYPE = "MAKEUP_SHOP"
    }

    fun searchShops(
        name: String?,
        address: String?,
        specialty: String?,
        sort: SortType?,
        userId: Long?,
        pageable: Pageable
    ): Page<MakeupShopResponse> {
        val shops = makeupShopRepository.searchShops(name, address, specialty, pageable)
        return enrichWithBookmarkInfo(shops, sort, userId)
    }

    fun getShops(pageable: Pageable): Page<MakeupShop> =
        makeupShopRepository.findAllOrderByCreatedAtDesc(pageable)

    fun getShop(id: Long): MakeupShop =
        makeupShopRepository.findById(id)
            .orElseThrow { NotFoundException("MakeupShop", id) }

    fun getShopWithLikeStatus(id: Long, userId: Long?): MakeupShopResponse {
        val shop = getShop(id)
        val bookmarkCount = bookmarkClient.getBookmarkCount(ServiceType.WEDDING, TARGET_TYPE, id)
        val isLiked = userId?.let {
            bookmarkClient.isBookmarked(it, ServiceType.WEDDING, TARGET_TYPE, id)
        } ?: false

        return MakeupShopResponse.from(shop, bookmarkCount, isLiked)
    }

    fun getShopsByServiceType(serviceType: MakeupServiceType, pageable: Pageable): Page<MakeupShop> =
        makeupShopRepository.findByServiceType(serviceType, pageable)

    fun getOnSiteAvailableShops(pageable: Pageable): Page<MakeupShop> =
        makeupShopRepository.findOnSiteAvailable(pageable)

    @Transactional
    fun createShop(request: MakeupShopCreateRequest): MakeupShopResponse {
        val shop = MakeupShop(
            name = request.name,
            address = request.address,
            serviceType = request.serviceType,
            basePrice = request.basePrice,
            coverImage = request.coverImage,
            phone = request.phone,
            snsUrl = request.snsUrl,
            specialty = request.specialty,
            onSiteAvailable = request.onSiteAvailable ?: true
        )
        val saved = makeupShopRepository.save(shop)
        return MakeupShopResponse.from(saved)
    }

    @Transactional
    fun updateShop(id: Long, request: MakeupShopUpdateRequest): MakeupShopResponse {
        val shop = getShop(id)
        request.name?.let { shop.name = it }
        request.address?.let { shop.address = it }
        request.serviceType?.let { shop.serviceType = it }
        request.basePrice?.let { shop.basePrice = it }
        request.coverImage?.let { shop.coverImage = it }
        request.phone?.let { shop.phone = it }
        request.snsUrl?.let { shop.snsUrl = it }
        request.specialty?.let { shop.specialty = it }
        request.onSiteAvailable?.let { shop.onSiteAvailable = it }

        val saved = makeupShopRepository.save(shop)
        return MakeupShopResponse.from(saved)
    }

    @Transactional
    fun deleteShop(id: Long) {
        if (!makeupShopRepository.existsById(id)) {
            throw NotFoundException("MakeupShop", id)
        }
        makeupShopRepository.deleteById(id)
    }

    private fun enrichWithBookmarkInfo(
        shops: Page<MakeupShop>,
        sort: SortType?,
        userId: Long?
    ): Page<MakeupShopResponse> {
        if (shops.isEmpty) {
            return Page.empty(shops.pageable)
        }

        val ids = shops.content.map { it.id }
        val bookmarkCounts = bookmarkClient.getBookmarkCountBatch(ServiceType.WEDDING, TARGET_TYPE, ids)
        val likedIds = userId?.let {
            bookmarkClient.getBookmarkedIds(it, ServiceType.WEDDING, TARGET_TYPE, ids)
        } ?: emptySet()

        var responses = shops.content.map { shop ->
            MakeupShopResponse.from(
                shop,
                bookmarkCounts[shop.id] ?: 0L,
                likedIds.contains(shop.id)
            )
        }

        if (sort == SortType.FAVORITE) {
            responses = responses.sortedByDescending { it.bookmarkCount }
        }

        return PageImpl(responses, shops.pageable, shops.totalElements)
    }
}

data class MakeupShopCreateRequest(
    val name: String,
    val address: String,
    val serviceType: MakeupServiceType,
    val basePrice: Int? = null,
    val coverImage: String? = null,
    val phone: String? = null,
    val snsUrl: String? = null,
    val specialty: String? = null,
    val onSiteAvailable: Boolean? = true
)

data class MakeupShopUpdateRequest(
    val name: String? = null,
    val address: String? = null,
    val serviceType: MakeupServiceType? = null,
    val basePrice: Int? = null,
    val coverImage: String? = null,
    val phone: String? = null,
    val snsUrl: String? = null,
    val specialty: String? = null,
    val onSiteAvailable: Boolean? = null
)
