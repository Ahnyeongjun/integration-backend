package com.msa.user.api.controller

import com.msa.common.response.ApiResponse
import com.msa.user.application.ExperienceService
import com.msa.user.application.ProfileUpdateRequest
import com.msa.user.application.UserService
import com.msa.user.domain.entity.User
import com.msa.user.domain.entity.UserExperience
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import com.msa.common.security.UserPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@Tag(name = "User", description = "사용자 API")
@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService,
    private val experienceService: ExperienceService
) {

    @Operation(summary = "내 정보 조회")
    @GetMapping("/me")
    fun getMyProfile(@AuthenticationPrincipal principal: UserPrincipal): ApiResponse<UserResponse> {
        val user = userService.getUser(principal.userId)
        return ApiResponse.success(UserResponse.from(user))
    }

    @Operation(summary = "사용자 조회")
    @GetMapping("/{userId}")
    fun getUser(@PathVariable userId: Long): ApiResponse<UserResponse> {
        val user = userService.getUser(userId)
        return ApiResponse.success(UserResponse.from(user))
    }

    @Operation(summary = "프로필 수정 (PUT)")
    @PutMapping("/me")
    fun updateProfilePut(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: ProfileUpdateRequest
    ): ApiResponse<UserResponse> {
        val user = userService.updateProfile(principal.userId, request)
        return ApiResponse.success(UserResponse.from(user))
    }

    @Operation(summary = "프로필 수정 (PATCH)")
    @PatchMapping("/me")
    fun updateProfilePatch(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: ProfileUpdateRequest
    ): ApiResponse<UserResponse> {
        val user = userService.updateProfile(principal.userId, request)
        return ApiResponse.success(UserResponse.from(user))
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/me")
    fun deleteUser(@AuthenticationPrincipal principal: UserPrincipal): ApiResponse<Unit> {
        userService.deleteUser(principal.userId)
        return ApiResponse.success()
    }

    @Operation(summary = "사용자 경험 등록")
    @PostMapping("/experience")
    fun saveExperience(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: ExperienceRequest
    ): ApiResponse<ExperienceResponse> {
        val experience = experienceService.saveExperience(principal.userId, request.rating, request.feedback)
        return ApiResponse.success(ExperienceResponse.from(experience))
    }

    @Operation(summary = "사용자 경험 조회")
    @GetMapping("/experience")
    fun getExperience(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ApiResponse<ExperienceResponse?> {
        val experience = experienceService.getExperience(principal.userId)
        return ApiResponse.success(experience?.let { ExperienceResponse.from(it) })
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

data class ExperienceRequest(
    val rating: Int,
    val feedback: String?
)

data class ExperienceResponse(
    val success: Boolean,
    val feedback: String?,
    val rating: Int,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(experience: UserExperience) = ExperienceResponse(
            success = true,
            feedback = experience.feedback,
            rating = experience.rating,
            createdAt = experience.createdAt
        )
    }
}
