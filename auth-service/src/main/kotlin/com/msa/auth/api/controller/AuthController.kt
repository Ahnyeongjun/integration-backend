package com.msa.auth.api.controller

import com.msa.auth.application.AuthService
import com.msa.auth.application.EmailVerificationService
import com.msa.auth.application.TokenResponse
import com.msa.common.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
    private val emailVerificationService: EmailVerificationService
) {

    @Operation(summary = "이메일 회원가입")
    @PostMapping("/signup")
    fun signup(
        @Valid @RequestBody request: SignupRequest
    ): ApiResponse<TokenResponse> {
        val tokens = authService.signup(request.email, request.password, request.nickname)
        return ApiResponse.success(tokens)
    }

    @Operation(summary = "이메일 로그인")
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest
    ): ApiResponse<TokenResponse> {
        val tokens = authService.login(request.email, request.password)
        return ApiResponse.success(tokens)
    }

    @Operation(
        summary = "OAuth 로그인/회원가입 (swyp11 스타일)",
        description = """
            OAuth 인증 후 콜백.
            - 기본(requireAdditionalSignup=false): 바로 User 생성하여 로그인 완료
            - 선택(requireAdditionalSignup=true): OAuthAccount만 생성, /signup/complete 호출 필요
        """
    )
    @PostMapping("/oauth/login/{provider}")
    fun oauthLogin(
        @PathVariable provider: String,
        @RequestParam code: String,
        @RequestParam redirectUri: String,
        @RequestParam(defaultValue = "false") requireAdditionalSignup: Boolean
    ): ApiResponse<TokenResponse> {
        val tokens = authService.oauthLogin(provider, code, redirectUri, requireAdditionalSignup)
        return ApiResponse.success(tokens)
    }

    @Operation(summary = "추가 회원가입 완료", description = "OAuth 로그인 후 추가 정보 입력하여 회원가입 완료")
    @PostMapping("/signup/complete")
    fun completeSignup(
        @Valid @RequestBody request: CompleteSignupRequest
    ): ApiResponse<TokenResponse> {
        val tokens = authService.completeSignup(request.oauthAccountId, request.nickname)
        return ApiResponse.success(tokens)
    }

    @Operation(summary = "토큰 갱신 (JWT Stateless)")
    @PostMapping("/refresh")
    fun refreshToken(
        @RequestBody request: RefreshTokenRequest
    ): ApiResponse<TokenResponse> {
        val tokens = authService.refreshToken(request.refreshToken)
        return ApiResponse.success(tokens)
    }

    @Operation(summary = "로그아웃", description = "Stateless 방식이므로 클라이언트에서 토큰 삭제 필요")
    @PostMapping("/logout")
    fun logout(
        @AuthenticationPrincipal(expression = "userId") userId: Long
    ): ApiResponse<Unit> {
        authService.logout(userId)
        return ApiResponse.success()
    }

    // ========== 비밀번호 변경 API ==========

    @Operation(summary = "비밀번호 변경 (PUT)", description = "로그인된 사용자의 비밀번호 변경")
    @PutMapping("/password")
    fun changePasswordPut(
        @AuthenticationPrincipal(expression = "userId") userId: Long,
        @Valid @RequestBody request: ChangePasswordRequest
    ): ApiResponse<MessageResponse> {
        authService.changePassword(userId, request.currentPassword, request.newPassword)
        return ApiResponse.success(MessageResponse("비밀번호가 변경되었습니다"))
    }

    @Operation(summary = "비밀번호 변경 (PATCH)", description = "로그인된 사용자의 비밀번호 변경")
    @PatchMapping("/password")
    fun changePasswordPatch(
        @AuthenticationPrincipal(expression = "userId") userId: Long,
        @Valid @RequestBody request: ChangePasswordRequest
    ): ApiResponse<MessageResponse> {
        authService.changePassword(userId, request.currentPassword, request.newPassword)
        return ApiResponse.success(MessageResponse("비밀번호가 변경되었습니다"))
    }

    // ========== 이메일 인증 API ==========

    @Operation(summary = "회원가입용 이메일 인증 코드 발송")
    @PostMapping("/email/send-code")
    fun sendSignupVerificationCode(
        @Valid @RequestBody request: EmailRequest
    ): ApiResponse<EmailSentResponse> {
        emailVerificationService.sendSignupVerificationCode(request.email)
        return ApiResponse.success(EmailSentResponse(
            message = "인증 코드가 발송되었습니다",
            expiresInSeconds = 300
        ))
    }

    @Operation(summary = "회원가입용 이메일 인증 코드 검증")
    @PostMapping("/email/verify")
    fun verifySignupCode(
        @Valid @RequestBody request: VerifyCodeRequest
    ): ApiResponse<VerificationResponse> {
        val token = emailVerificationService.verifySignupCode(request.email, request.code)
        return ApiResponse.success(VerificationResponse(
            verified = true,
            token = token,
            message = "이메일 인증이 완료되었습니다"
        ))
    }

    @Operation(summary = "비밀번호 재설정용 이메일 인증 코드 발송")
    @PostMapping("/password/send-code")
    fun sendPasswordResetCode(
        @Valid @RequestBody request: EmailRequest
    ): ApiResponse<EmailSentResponse> {
        emailVerificationService.sendPasswordResetCode(request.email)
        return ApiResponse.success(EmailSentResponse(
            message = "비밀번호 재설정 코드가 발송되었습니다",
            expiresInSeconds = 300
        ))
    }

    @Operation(summary = "비밀번호 재설정 코드 검증")
    @PostMapping("/password/verify-code")
    fun verifyPasswordResetCode(
        @Valid @RequestBody request: VerifyCodeRequest
    ): ApiResponse<VerificationResponse> {
        val token = emailVerificationService.verifyPasswordResetCode(request.email, request.code)
        return ApiResponse.success(VerificationResponse(
            verified = true,
            token = token,
            message = "인증이 완료되었습니다. 새 비밀번호를 설정해주세요."
        ))
    }

    @Operation(summary = "비밀번호 재설정")
    @PostMapping("/password/reset")
    fun resetPassword(
        @Valid @RequestBody request: ResetPasswordRequest
    ): ApiResponse<MessageResponse> {
        emailVerificationService.resetPassword(request.token, request.newPassword)
        return ApiResponse.success(MessageResponse("비밀번호가 변경되었습니다"))
    }
}

data class SignupRequest(
    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    val email: String,

    @field:NotBlank(message = "비밀번호는 필수입니다")
    @field:Size(min = 8, max = 20, message = "비밀번호는 8~20자여야 합니다")
    val password: String,

    @field:NotBlank(message = "닉네임은 필수입니다")
    @field:Size(min = 2, max = 20, message = "닉네임은 2~20자여야 합니다")
    val nickname: String
)

data class LoginRequest(
    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    val email: String,

    @field:NotBlank(message = "비밀번호는 필수입니다")
    val password: String
)

data class CompleteSignupRequest(
    val oauthAccountId: Long,

    @field:NotBlank(message = "닉네임은 필수입니다")
    @field:Size(min = 2, max = 20, message = "닉네임은 2~20자여야 합니다")
    val nickname: String
)

data class RefreshTokenRequest(
    val refreshToken: String
)

// 이메일 인증 관련 DTOs
data class EmailRequest(
    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    val email: String
)

data class VerifyCodeRequest(
    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    val email: String,

    @field:NotBlank(message = "인증 코드는 필수입니다")
    @field:Size(min = 6, max = 6, message = "인증 코드는 6자리입니다")
    val code: String
)

data class ResetPasswordRequest(
    @field:NotBlank(message = "토큰은 필수입니다")
    val token: String,

    @field:NotBlank(message = "비밀번호는 필수입니다")
    @field:Size(min = 8, max = 20, message = "비밀번호는 8~20자여야 합니다")
    val newPassword: String
)

data class EmailSentResponse(
    val message: String,
    val expiresInSeconds: Int
)

data class VerificationResponse(
    val verified: Boolean,
    val token: String,
    val message: String
)

data class MessageResponse(
    val message: String
)

data class ChangePasswordRequest(
    @field:NotBlank(message = "현재 비밀번호는 필수입니다")
    val currentPassword: String,

    @field:NotBlank(message = "새 비밀번호는 필수입니다")
    @field:Size(min = 8, max = 20, message = "비밀번호는 8~20자여야 합니다")
    val newPassword: String
)
