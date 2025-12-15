package com.msa.auth.application

import com.msa.auth.domain.entity.OAuthAccount
import com.msa.auth.domain.entity.OAuthProvider
import com.msa.auth.domain.entity.RefreshToken
import com.msa.auth.domain.repository.OAuthAccountRepository
import com.msa.auth.domain.repository.RefreshTokenRepository
import com.msa.common.exception.UnauthorizedException
import com.msa.common.security.JwtTokenProvider
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class AuthService(
    private val oauthAccountRepository: OAuthAccountRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val oauthClientFactory: OAuthClientFactory
) {

    fun oauthLogin(provider: OAuthProvider, code: String): TokenResponse {
        val oauthClient = oauthClientFactory.getClient(provider)
        val oauthUser = oauthClient.getUserInfo(code)

        val oauthAccount = oauthAccountRepository.findByProviderAndProviderId(provider, oauthUser.id)

        val userId = if (oauthAccount != null) {
            oauthAccount.userId
        } else {
            // Create new user via Kafka event
            val newUserId = createUserViaKafka(oauthUser, provider)

            oauthAccountRepository.save(
                OAuthAccount(
                    userId = newUserId,
                    provider = provider,
                    providerId = oauthUser.id,
                    email = oauthUser.email
                )
            )
            newUserId
        }

        return generateTokens(userId, oauthUser.email ?: "")
    }

    fun refreshToken(refreshToken: String): TokenResponse {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw UnauthorizedException("Invalid refresh token")
        }

        val storedToken = refreshTokenRepository.findByToken(refreshToken)
            ?: throw UnauthorizedException("Refresh token not found")

        if (storedToken.isExpired()) {
            refreshTokenRepository.delete(storedToken)
            throw UnauthorizedException("Refresh token expired")
        }

        val userId = jwtTokenProvider.getUserId(refreshToken)
        val email = jwtTokenProvider.getEmail(refreshToken)

        return generateTokens(userId, email)
    }

    fun logout(userId: Long) {
        refreshTokenRepository.deleteByUserId(userId)
    }

    private fun generateTokens(userId: Long, email: String): TokenResponse {
        val accessToken = jwtTokenProvider.createAccessToken(userId, email)
        val refreshToken = jwtTokenProvider.createRefreshToken(userId, email)

        val expiresAt = LocalDateTime.now().plusDays(7)
        val existingToken = refreshTokenRepository.findByUserId(userId)

        if (existingToken != null) {
            existingToken.refresh(refreshToken, expiresAt)
            refreshTokenRepository.save(existingToken)
        } else {
            refreshTokenRepository.save(
                RefreshToken(
                    userId = userId,
                    token = refreshToken,
                    expiresAt = expiresAt
                )
            )
        }

        return TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = 3600
        )
    }

    private fun createUserViaKafka(oauthUser: OAuthUserInfo, provider: OAuthProvider): Long {
        // In real implementation, this would use a saga pattern or request-reply
        // For simplicity, we'll generate a temp ID and send create event
        val tempUserId = System.currentTimeMillis()

        kafkaTemplate.send("user-events", UserCreatedEvent(
            userId = tempUserId,
            email = oauthUser.email,
            nickname = oauthUser.nickname,
            profileImage = oauthUser.profileImage,
            provider = provider.name
        ))

        return tempUserId
    }
}

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)

data class OAuthUserInfo(
    val id: String,
    val email: String?,
    val nickname: String?,
    val profileImage: String?
)

data class UserCreatedEvent(
    val userId: Long,
    val email: String?,
    val nickname: String?,
    val profileImage: String?,
    val provider: String
)
