package com.msa.bookmark.application

import com.msa.bookmark.domain.entity.Bookmark
import com.msa.bookmark.domain.entity.TargetType
import com.msa.bookmark.domain.repository.BookmarkRepository
import com.msa.common.enums.ServiceType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class BookmarkService(
    private val bookmarkRepository: BookmarkRepository
) {

    fun toggleBookmark(userId: Long, request: BookmarkRequest): BookmarkResponse {
        val existing = bookmarkRepository.findByUserAndTarget(
            userId, request.serviceType, request.targetType, request.targetId
        )

        return if (existing != null) {
            if (existing.isDeleted()) {
                existing.restore()
                bookmarkRepository.save(existing)
                BookmarkResponse(bookmarked = true, count = getCount(request))
            } else {
                existing.delete()
                bookmarkRepository.save(existing)
                BookmarkResponse(bookmarked = false, count = getCount(request))
            }
        } else {
            bookmarkRepository.save(
                Bookmark(
                    userId = userId,
                    serviceType = request.serviceType,
                    targetType = request.targetType,
                    targetId = request.targetId
                )
            )
            BookmarkResponse(bookmarked = true, count = getCount(request))
        }
    }

    @Transactional(readOnly = true)
    fun getMyBookmarks(userId: Long, serviceType: ServiceType?, pageable: Pageable): Page<Bookmark> {
        return if (serviceType != null) {
            bookmarkRepository.findByUserIdAndServiceType(userId, serviceType, pageable)
        } else {
            bookmarkRepository.findByUserId(userId, pageable)
        }
    }

    @Transactional(readOnly = true)
    fun isBookmarked(userId: Long, serviceType: ServiceType, targetType: TargetType, targetId: Long): Boolean {
        return bookmarkRepository.existsByUserAndTarget(userId, serviceType, targetType, targetId)
    }

    @Transactional(readOnly = true)
    fun getBookmarkCount(serviceType: ServiceType, targetType: TargetType, targetId: Long): Long {
        return bookmarkRepository.countByTarget(serviceType, targetType, targetId)
    }

    private fun getCount(request: BookmarkRequest): Long {
        return bookmarkRepository.countByTarget(request.serviceType, request.targetType, request.targetId)
    }

    // Internal API methods for service-to-service communication
    @Transactional(readOnly = true)
    fun isBookmarkedInternal(userId: Long, serviceType: ServiceType, targetType: String, targetId: Long): Boolean {
        val type = TargetType.valueOf(targetType)
        return bookmarkRepository.existsByUserAndTarget(userId, serviceType, type, targetId)
    }

    @Transactional(readOnly = true)
    fun getBookmarkCountInternal(serviceType: ServiceType, targetType: String, targetId: Long): Long {
        val type = TargetType.valueOf(targetType)
        return bookmarkRepository.countByTarget(serviceType, type, targetId)
    }

    @Transactional(readOnly = true)
    fun getBookmarkedIds(userId: Long, serviceType: ServiceType, targetType: String, targetIds: List<Long>): Set<Long> {
        if (targetIds.isEmpty()) return emptySet()
        val type = TargetType.valueOf(targetType)
        return bookmarkRepository.findBookmarkedTargetIds(userId, serviceType, type, targetIds).toSet()
    }

    @Transactional(readOnly = true)
    fun getBookmarkCountBatch(serviceType: ServiceType, targetType: String, targetIds: List<Long>): Map<Long, Long> {
        if (targetIds.isEmpty()) return emptyMap()
        val type = TargetType.valueOf(targetType)
        return bookmarkRepository.countByTargetIds(serviceType, type, targetIds)
            .associate { (it[0] as Long) to (it[1] as Long) }
    }
}

data class BookmarkRequest(
    val serviceType: ServiceType,
    val targetType: TargetType,
    val targetId: Long
)

data class BookmarkResponse(
    val bookmarked: Boolean,
    val count: Long
)
