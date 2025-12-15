package com.msa.auth.api.controller

import com.msa.auth.application.AuthService
import com.msa.auth.application.TokenResponse
import com.msa.auth.domain.entity.OAuthProvider
import com.msa.common.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService
) {

    @Operation(summary = "OAuth 로그인")
    @GetMapping("/oauth/{provider}/callback")
    fun oauthCallback(
        @PathVariable provider: String,
        @RequestParam code: String
    ): ApiResponse<TokenResponse> {
        val oauthProvider = OAuthProvider.valueOf(provider.uppercase())
        val tokens = authService.oauthLogin(oauthProvider, code)
        return ApiResponse.success(tokens)
    }

    @Operation(summary = "토큰 갱신")
    @PostMapping("/refresh")
    fun refreshToken(
        @RequestBody request: RefreshTokenRequest
    ): ApiResponse<TokenResponse> {
        val tokens = authService.refreshToken(request.refreshToken)
        return ApiResponse.success(tokens)
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    fun logout(
        @AuthenticationPrincipal userId: Long
    ): ApiResponse<Unit> {
        authService.logout(userId)
        return ApiResponse.success()
    }
}

data class RefreshTokenRequest(
    val refreshToken: String
)
