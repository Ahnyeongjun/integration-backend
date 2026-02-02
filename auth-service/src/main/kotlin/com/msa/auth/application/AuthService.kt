package com.msa.auth.application

import com.msa.auth.domain.entity.LocalAccount
import com.msa.auth.domain.entity.LoginType
import com.msa.auth.domain.entity.OAuthAccount
import com.msa.auth.domain.repository.LocalAccountRepository
import com.msa.auth.domain.repository.OAuthAccountRepository
import com.msa.auth.oauth.OAuthClient
import com.msa.common.exception.BadRequestException
import com.msa.common.exception.UnauthorizedException
import com.msa.common.security.JwtTokenProvider
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 인증 서비스 (swyp11 스타일 - JWT Stateless)
 * RefreshToken을 DB에 저장하지 않고 JWT 자체로 검증
 */
@Service
@Transactional
class AuthService(
    private val localAccountRepository: LocalAccountRepository,
    private val oauthAccountRepository: OAuthAccountRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val oauthClients: List<OAuthClient>,  // swyp11 스타일: 모든 OAuthClient 자동 주입
    private val passwordEncoder: PasswordEncoder
) {

    companion object {
        private const val ACCESS_TOKEN_EXPIRES_IN = 3600L      // 1시간
        private const val REFRESH_TOKEN_EXPIRES_IN = 604800L   // 7일
    }

    /**
     * 이메일 회원가입
     */
    fun signup(email: String, password: String, nickname: String): TokenResponse {
        if (localAccountRepository.existsByEmail(email)) {
            throw BadRequestException("이미 존재하는 이메일입니다")
        }

        val userId = System.currentTimeMillis()
        val encodedPassword = passwordEncoder.encode(password)

        localAccountRepository.save(
            LocalAccount(
                email = email,
                password = encodedPassword,
                userId = userId,
                isVerified = true
            )
        )

        // user-service에 사용자 생성 이벤트 발행
        kafkaTemplate.send("user-events", UserCreatedEvent(
            userId = userId,
            email = email,
            nickname = nickname,
            profileImage = null,
            provider = LoginType.EMAIL.name
        ))

        return generateTokens(userId, email, nickname, signupCompleted = true)
    }

    /**
     * 이메일 로그인
     */
    fun login(email: String, password: String): TokenResponse {
        val account = localAccountRepository.findByEmail(email)
            ?: throw UnauthorizedException("이메일 또는 비밀번호가 일치하지 않습니다")

        if (!passwordEncoder.matches(password, account.password)) {
            throw UnauthorizedException("이메일 또는 비밀번호가 일치하지 않습니다")
        }

        // TODO: user-service에서 닉네임 조회 (현재는 email 사용)
        return generateTokens(account.userId, email, email, signupCompleted = true)
    }

    /**
     * OAuth 로그인/회원가입 (swyp11 스타일)
     *
     * @param provider OAuth 제공자 (google, kakao, naver)
     * @param code OAuth 인증 코드
     * @param redirectUri OAuth 콜백 URI
     * @param requireAdditionalSignup true면 2단계(추가 회원가입 필요), false면 바로 User 생성 (기본값)
     */
    fun oauthLogin(provider: String, code: String, redirectUri: String, requireAdditionalSignup: Boolean = false): TokenResponse {
        // swyp11 스타일: provider 이름으로 알맞은 OAuthClient 선택
        val oauthClient = oauthClients.find { it.getProvider().equals(provider, ignoreCase = true) }
            ?: throw BadRequestException("지원하지 않는 provider: $provider")

        val oauthUser = oauthClient.getUserInfo(code, redirectUri)
        val loginType = LoginType.valueOf(provider.uppercase())

        val existingAccount = oauthAccountRepository.findByProviderAndProviderUserId(loginType, oauthUser.providerId)

        return if (existingAccount != null) {
            // 기존 OAuth 계정 존재
            if (existingAccount.isLinkedToUser()) {
                // User와 연결됨 → 정상 로그인
                val userId = requireNotNull(existingAccount.userId) { "연결된 User ID가 없습니다" }
                generateTokens(
                    userId = userId,
                    email = existingAccount.providerEmail ?: "",
                    nickname = existingAccount.providerNickname ?: "",
                    signupCompleted = true
                )
            } else if (requireAdditionalSignup) {
                // User 미연결 & 추가 회원가입 요청 → 임시 토큰 반환
                generateOAuthToken(existingAccount)
            } else {
                // User 미연결 & 바로 생성 → User 생성 후 연결
                createUserAndLink(existingAccount, oauthUser.name ?: "사용자")
            }
        } else {
            // 신규 OAuth 계정
            val newOAuthAccount = oauthAccountRepository.save(
                OAuthAccount(
                    provider = loginType,
                    providerUserId = oauthUser.providerId,
                    providerEmail = oauthUser.email,
                    providerNickname = oauthUser.name,
                    providerProfileImage = oauthUser.profileImage
                )
            )

            if (requireAdditionalSignup) {
                // 추가 회원가입 요청 → 임시 토큰 반환
                generateOAuthToken(newOAuthAccount)
            } else {
                // 바로 User 생성 (기본)
                createUserAndLink(newOAuthAccount, oauthUser.name ?: "사용자")
            }
        }
    }

    /**
     * OAuthAccount에 User 생성 및 연결 (옵션2: 바로 가입)
     */
    private fun createUserAndLink(oauthAccount: OAuthAccount, nickname: String): TokenResponse {
        val userId = System.currentTimeMillis()
        oauthAccount.linkUser(userId)
        oauthAccountRepository.save(oauthAccount)

        // user-service에 사용자 생성 이벤트 발행
        kafkaTemplate.send("user-events", UserCreatedEvent(
            userId = userId,
            email = oauthAccount.providerEmail,
            nickname = nickname,
            profileImage = oauthAccount.providerProfileImage,
            provider = oauthAccount.provider.name
        ))

        return generateTokens(userId, oauthAccount.providerEmail ?: "", nickname, signupCompleted = true)
    }

    /**
     * 추가 회원가입 완료 (OAuth → User 연결)
     */
    fun completeSignup(oauthAccountId: Long, nickname: String): TokenResponse {
        val oauthAccount = oauthAccountRepository.findById(oauthAccountId)
            .orElseThrow { BadRequestException("OAuth 계정을 찾을 수 없습니다") }

        if (oauthAccount.isLinkedToUser()) {
            throw BadRequestException("이미 회원가입이 완료된 계정입니다")
        }

        val userId = System.currentTimeMillis()
        oauthAccount.linkUser(userId)
        oauthAccountRepository.save(oauthAccount)

        // user-service에 사용자 생성 이벤트 발행
        kafkaTemplate.send("user-events", UserCreatedEvent(
            userId = userId,
            email = oauthAccount.providerEmail,
            nickname = nickname,
            profileImage = oauthAccount.providerProfileImage,
            provider = oauthAccount.provider.name
        ))

        return generateTokens(userId, oauthAccount.providerEmail ?: "", nickname, signupCompleted = true)
    }

    /**
     * 토큰 갱신 (JWT Stateless 방식)
     * DB 조회 없이 JWT 자체만 검증
     */
    fun refreshToken(refreshToken: String): TokenResponse {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw UnauthorizedException("유효하지 않은 토큰입니다")
        }

        val userId = jwtTokenProvider.getUserId(refreshToken)
        val email = jwtTokenProvider.getEmail(refreshToken)
        val nickname = jwtTokenProvider.getNickname(refreshToken)
        val signupCompleted = jwtTokenProvider.isSignupCompleted(refreshToken)

        return generateTokens(userId, email, nickname, signupCompleted)
    }

    /**
     * 로그아웃 (Stateless라 서버에서 할 일 없음)
     * 클라이언트에서 토큰 삭제 필요
     */
    fun logout(userId: Long) {
        // JWT Stateless 방식이므로 서버에서 할 일 없음
        // 클라이언트에서 토큰을 삭제하면 됨
    }

    /**
     * 로그인된 사용자의 비밀번호 변경
     */
    fun changePassword(userId: Long, currentPassword: String, newPassword: String) {
        val account = localAccountRepository.findByUserId(userId)
            ?: throw BadRequestException("계정을 찾을 수 없습니다")

        if (!passwordEncoder.matches(currentPassword, account.password)) {
            throw UnauthorizedException("현재 비밀번호가 일치하지 않습니다")
        }

        account.password = passwordEncoder.encode(newPassword)
        localAccountRepository.save(account)
    }

    /**
     * 완료된 사용자용 토큰 생성
     */
    private fun generateTokens(userId: Long, email: String, nickname: String, signupCompleted: Boolean): TokenResponse {
        val accessToken = jwtTokenProvider.createAccessToken(userId, email, nickname, signupCompleted)
        val refreshToken = jwtTokenProvider.createRefreshToken(userId, email, nickname, signupCompleted)

        return TokenResponse.of(
            accessToken = accessToken,
            refreshToken = refreshToken,
            userId = userId,
            nickname = nickname,
            needsAdditionalSignup = !signupCompleted
        )
    }

    /**
     * OAuth 임시 토큰 생성 (추가 회원가입 필요)
     */
    private fun generateOAuthToken(oauthAccount: OAuthAccount): TokenResponse {
        val accessToken = jwtTokenProvider.createOAuthToken(
            oauthAccountId = oauthAccount.oauthId,
            provider = oauthAccount.provider.name,
            providerNickname = oauthAccount.providerNickname
        )

        return TokenResponse.ofOAuth(
            accessToken = accessToken,
            oauthAccountId = oauthAccount.oauthId,
            nickname = oauthAccount.providerNickname ?: "사용자"
        )
    }
}

/**
 * 토큰 응답 DTO
 */
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String?,
    val tokenType: String,
    val expiresIn: Long,
    val userId: Long?,
    val nickname: String,
    val needsAdditionalSignup: Boolean
) {
    companion object {
        fun of(accessToken: String, refreshToken: String, userId: Long, nickname: String, needsAdditionalSignup: Boolean) = TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            tokenType = "Bearer",
            expiresIn = 3600L,
            userId = userId,
            nickname = nickname,
            needsAdditionalSignup = needsAdditionalSignup
        )

        fun ofOAuth(accessToken: String, oauthAccountId: Long, nickname: String) = TokenResponse(
            accessToken = accessToken,
            refreshToken = null,
            tokenType = "Bearer",
            expiresIn = 3600L,
            userId = null,
            nickname = nickname,
            needsAdditionalSignup = true
        )
    }
}

data class UserCreatedEvent(
    val userId: Long,
    val email: String?,
    val nickname: String?,
    val profileImage: String?,
    val provider: String
)
