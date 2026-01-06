package com.msa.bookmark.api.controller

import com.msa.bookmark.application.BookmarkRequest
import com.msa.bookmark.application.BookmarkResponse
import com.msa.bookmark.application.BookmarkService
import com.msa.bookmark.domain.entity.Bookmark
import com.msa.bookmark.domain.entity.TargetType
import com.msa.common.enums.ServiceType
import com.msa.common.response.ApiResponse
import com.msa.common.security.UserPrincipal
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "Bookmark", description = "북마크/찜 API")
@RestController
@RequestMapping("/api/v1/bookmarks")
class BookmarkController(
    private val bookmarkService: BookmarkService
) {

    @Operation(summary = "북마크 토글")
    @PostMapping
    fun toggleBookmark(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: BookmarkRequest
    ): ApiResponse<BookmarkResponse> {
        return ApiResponse.success(bookmarkService.toggleBookmark(principal.userId, request))
    }

    @Operation(summary = "카테고리/아이템별 북마크 추가 (프론트엔드 호환)")
    @PostMapping("/{category}/{postId}")
    fun addBookmark(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable category: String,
        @PathVariable postId: Long
    ): ApiResponse<Boolean> {
        val targetType = TargetType.fromCategory(category)
        val request = BookmarkRequest(
            serviceType = ServiceType.WEDDING,
            targetType = targetType,
            targetId = postId
        )
        val result = bookmarkService.toggleBookmark(principal.userId, request)
        return ApiResponse.success(result.bookmarked)
    }

    @Operation(summary = "카테고리/아이템별 북마크 삭제 (프론트엔드 호환)")
    @DeleteMapping("/{category}/{postId}")
    fun removeBookmark(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable category: String,
        @PathVariable postId: Long
    ): ApiResponse<Boolean> {
        val targetType = TargetType.fromCategory(category)
        bookmarkService.removeBookmark(principal.userId, ServiceType.WEDDING, targetType, postId)
        return ApiResponse.success(true)
    }

    @Operation(summary = "내 북마크 전체 목록 (프론트엔드 호환)")
    @GetMapping
    fun getAllMyBookmarks(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ApiResponse<List<LikesResponseDto>> {
        val bookmarks = bookmarkService.getAllMyBookmarks(principal.userId)
        return ApiResponse.success(bookmarks.map { LikesResponseDto.from(it) })
    }

    @Operation(summary = "내 북마크 목록 (페이징)")
    @GetMapping("/my")
    fun getMyBookmarks(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestParam(required = false) serviceType: ServiceType?,
        @PageableDefault(size = 20) pageable: Pageable
    ): ApiResponse<Page<BookmarkDto>> {
        val bookmarks = bookmarkService.getMyBookmarks(principal.userId, serviceType, pageable)
        return ApiResponse.success(bookmarks.map { BookmarkDto.from(it) })
    }

    @Operation(summary = "카테고리별 북마크 목록 (프론트엔드 호환)")
    @GetMapping("/category/{category}")
    fun getBookmarksByCategory(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable category: String
    ): ApiResponse<List<LikesResponseDto>> {
        val targetType = TargetType.fromCategory(category)
        val bookmarks = bookmarkService.getBookmarksByTargetType(principal.userId, targetType)
        return ApiResponse.success(bookmarks.map { LikesResponseDto.from(it) })
    }

    @Operation(summary = "북마크 여부 확인 (로그인 필수)")
    @GetMapping("/check")
    fun isBookmarked(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestParam serviceType: ServiceType,
        @RequestParam targetType: TargetType,
        @RequestParam targetId: Long
    ): ApiResponse<Boolean> {
        return ApiResponse.success(bookmarkService.isBookmarked(principal.userId, serviceType, targetType, targetId))
    }

    @Operation(summary = "북마크 수 조회 (공개)")
    @GetMapping("/count")
    fun getBookmarkCount(
        @RequestParam serviceType: ServiceType,
        @RequestParam targetType: TargetType,
        @RequestParam targetId: Long
    ): ApiResponse<Long> {
        return ApiResponse.success(bookmarkService.getBookmarkCount(serviceType, targetType, targetId))
    }
}

data class BookmarkDto(
    val id: Long,
    val serviceType: ServiceType,
    val targetType: TargetType,
    val targetId: Long
) {
    companion object {
        fun from(bookmark: Bookmark) = BookmarkDto(
            id = bookmark.id,
            serviceType = bookmark.serviceType,
            targetType = bookmark.targetType,
            targetId = bookmark.targetId
        )
    }
}

// 프론트엔드 LikesResponse와 호환되는 DTO
data class LikesResponseDto(
    val id: Long,
    val likesType: String,
    val targetId: Long,
    val updateDt: String,
    val itemDetails: Any? = null
) {
    companion object {
        fun from(bookmark: Bookmark) = LikesResponseDto(
            id = bookmark.id,
            likesType = bookmark.targetType.name,
            targetId = bookmark.targetId,
            updateDt = bookmark.updatedAt?.toString() ?: bookmark.createdAt?.toString() ?: ""
        )
    }
}

// Internal API Controller for service-to-service communication
@Tag(name = "Bookmark Internal", description = "북마크 내부 API (서비스 간 통신)")
@RestController
@RequestMapping("/api/v1/bookmarks/internal")
class BookmarkInternalController(
    private val bookmarkService: BookmarkService
) {
    @Operation(summary = "북마크 여부 확인 (내부용)")
    @GetMapping("/check")
    fun isBookmarked(
        @RequestParam userId: Long,
        @RequestParam serviceType: ServiceType,
        @RequestParam targetType: String,
        @RequestParam targetId: Long
    ): Boolean = bookmarkService.isBookmarkedInternal(userId, serviceType, targetType, targetId)

    @Operation(summary = "북마크 수 조회 (내부용)")
    @GetMapping("/count")
    fun getBookmarkCount(
        @RequestParam serviceType: ServiceType,
        @RequestParam targetType: String,
        @RequestParam targetId: Long
    ): Long = bookmarkService.getBookmarkCountInternal(serviceType, targetType, targetId)

    @Operation(summary = "북마크 여부 배치 확인 (내부용)")
    @PostMapping("/check-batch")
    fun checkBatch(@RequestBody request: BatchCheckRequest): Set<Long> =
        bookmarkService.getBookmarkedIds(request.userId, request.serviceType, request.targetType, request.targetIds)

    @Operation(summary = "북마크 수 배치 조회 (내부용)")
    @PostMapping("/count-batch")
    fun countBatch(@RequestBody request: BatchCountRequest): Map<Long, Long> =
        bookmarkService.getBookmarkCountBatch(request.serviceType, request.targetType, request.targetIds)
}

data class BatchCheckRequest(
    val userId: Long,
    val serviceType: ServiceType,
    val targetType: String,
    val targetIds: List<Long>
)

data class BatchCountRequest(
    val serviceType: ServiceType,
    val targetType: String,
    val targetIds: List<Long>
)
