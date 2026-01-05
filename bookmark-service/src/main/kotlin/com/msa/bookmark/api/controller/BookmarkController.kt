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

    @Operation(summary = "내 북마크 목록")
    @GetMapping("/my")
    fun getMyBookmarks(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestParam(required = false) serviceType: ServiceType?,
        @PageableDefault(size = 20) pageable: Pageable
    ): ApiResponse<Page<BookmarkDto>> {
        val bookmarks = bookmarkService.getMyBookmarks(principal.userId, serviceType, pageable)
        return ApiResponse.success(bookmarks.map { BookmarkDto.from(it) })
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
