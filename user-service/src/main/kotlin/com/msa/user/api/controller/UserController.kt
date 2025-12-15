package com.msa.user.api.controller

import com.msa.common.response.ApiResponse
import com.msa.user.application.ProfileUpdateRequest
import com.msa.user.application.UserService
import com.msa.user.domain.entity.User
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "User", description = "사용자 API")
@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService
) {

    @Operation(summary = "내 정보 조회")
    @GetMapping("/me")
    fun getMyProfile(@AuthenticationPrincipal userId: Long): ApiResponse<UserResponse> {
        val user = userService.getUser(userId)
        return ApiResponse.success(UserResponse.from(user))
    }

    @Operation(summary = "사용자 조회")
    @GetMapping("/{userId}")
    fun getUser(@PathVariable userId: Long): ApiResponse<UserResponse> {
        val user = userService.getUser(userId)
        return ApiResponse.success(UserResponse.from(user))
    }

    @Operation(summary = "프로필 수정")
    @PatchMapping("/me")
    fun updateProfile(
        @AuthenticationPrincipal userId: Long,
        @RequestBody request: ProfileUpdateRequest
    ): ApiResponse<UserResponse> {
        val user = userService.updateProfile(userId, request)
        return ApiResponse.success(UserResponse.from(user))
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/me")
    fun deleteUser(@AuthenticationPrincipal userId: Long): ApiResponse<Unit> {
        userService.deleteUser(userId)
        return ApiResponse.success()
    }
}

data class UserResponse(
    val id: Long,
    val email: String?,
    val nickname: String?,
    val profileImage: String?,
    val bio: String?
) {
    companion object {
        fun from(user: User) = UserResponse(
            id = user.id,
            email = user.email,
            nickname = user.nickname,
            profileImage = user.profileImage,
            bio = user.bio
        )
    }
}
