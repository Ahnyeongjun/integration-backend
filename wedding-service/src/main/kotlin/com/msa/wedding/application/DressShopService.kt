package com.msa.wedding.application

import com.msa.common.enums.ServiceType
import com.msa.common.exception.NotFoundException
import com.msa.wedding.api.controller.DressResponse
import com.msa.wedding.api.controller.DressShopCreateRequest
import com.msa.wedding.api.controller.DressShopResponse
import com.msa.wedding.api.controller.DressShopUpdateRequest
import com.msa.wedding.domain.entity.DressShop
import com.msa.wedding.domain.entity.SortType
import com.msa.wedding.domain.repository.DressRepository
import com.msa.wedding.domain.repository.DressShopRepository
import com.msa.wedding.infrastructure.BookmarkClient
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DressShopService(
    private val dressShopRepository: DressShopRepository,
    private val dressRepository: DressRepository,
    private val bookmarkClient: BookmarkClient
) {
    companion object {
        private const val TARGET_TYPE = "DRESS_SHOP"
    }

    fun searchShops(
        shopName: String?,
        address: String?,
        specialty: String?,
        sort: SortType?,
        userId: Long?,
        pageable: Pageable
    ): Page<DressShopResponse> {
        val shops = dressShopRepository.searchShops(shopName, address, specialty, pageable)
        return enrichWithBookmarkInfo(shops, sort, userId)
    }

    fun getShop(id: Long): DressShop =
        dressShopRepository.findById(id)
            .orElseThrow { NotFoundException("DressShop", id) }

    fun getShopWithLikeStatus(id: Long, userId: Long?): DressShopResponse {
        val shop = getShop(id)
        val bookmarkCount = bookmarkClient.getBookmarkCount(ServiceType.WEDDING, TARGET_TYPE, id)
        val isLiked = userId?.let {
            bookmarkClient.isBookmarked(it, ServiceType.WEDDING, TARGET_TYPE, id)
        } ?: false

        return DressShopResponse.from(shop, bookmarkCount, isLiked)
    }

    @Transactional
    fun createShop(request: DressShopCreateRequest): DressShopResponse {
        val shop = DressShop(
            shopName = request.shopName,
            description = request.description,
            address = request.address,
            phone = request.phone,
            snsUrl = request.snsUrl,
            imageUrl = request.imageUrl,
            specialty = request.specialty,
            features = request.features
        )
        val saved = dressShopRepository.save(shop)
        return DressShopResponse.from(saved)
    }

    @Transactional
    fun updateShop(id: Long, request: DressShopUpdateRequest): DressShopResponse {
        val shop = getShop(id)
        request.shopName?.let { shop.shopName = it }
        request.description?.let { shop.description = it }
        request.address?.let { shop.address = it }
        request.phone?.let { shop.phone = it }
        request.snsUrl?.let { shop.snsUrl = it }
        request.imageUrl?.let { shop.imageUrl = it }
        request.specialty?.let { shop.specialty = it }
        request.features?.let { shop.features = it }

        val saved = dressShopRepository.save(shop)
        return DressShopResponse.from(saved)
    }

    @Transactional
    fun deleteShop(id: Long) {
        if (!dressShopRepository.existsById(id)) {
            throw NotFoundException("DressShop", id)
        }
        dressShopRepository.deleteById(id)
    }

    fun getDressesByShop(shopId: Long, pageable: Pageable): Page<DressResponse> {
        if (!dressShopRepository.existsById(shopId)) {
            throw NotFoundException("DressShop", shopId)
        }
        return dressRepository.findByDressShopId(shopId, pageable)
            .map { DressResponse.from(it) }
    }

    private fun enrichWithBookmarkInfo(
        shops: Page<DressShop>,
        sort: SortType?,
        userId: Long?
    ): Page<DressShopResponse> {
        if (shops.isEmpty) {
            return Page.empty(shops.pageable)
        }

        val ids = shops.content.map { it.id }
        val bookmarkCounts = bookmarkClient.getBookmarkCountBatch(ServiceType.WEDDING, TARGET_TYPE, ids)
        val likedIds = userId?.let {
            bookmarkClient.getBookmarkedIds(it, ServiceType.WEDDING, TARGET_TYPE, ids)
        } ?: emptySet()

        var responses = shops.content.map { shop ->
            DressShopResponse.from(
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
