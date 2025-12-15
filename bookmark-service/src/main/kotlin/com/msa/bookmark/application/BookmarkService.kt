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
