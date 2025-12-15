package com.msa.auth.domain.entity

import com.msa.common.entity.BaseTimeEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "oauth_accounts")
class OAuthAccount(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val provider: OAuthProvider,

    @Column(name = "provider_id", nullable = false, length = 100)
    val providerId: String,

    @Column(length = 100)
    var email: String? = null,

    @Column(name = "access_token", length = 500)
    var accessToken: String? = null,

    @Column(name = "refresh_token", length = 500)
    var refreshToken: String? = null

) : BaseTimeEntity()

enum class OAuthProvider {
    KAKAO, GOOGLE, NAVER
}

@Entity
@Table(name = "refresh_tokens")
class RefreshToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_id", nullable = false, unique = true)
    val userId: Long,

    @Column(nullable = false, length = 500)
    var token: String,

    @Column(name = "expires_at", nullable = false)
    var expiresAt: LocalDateTime

) : BaseTimeEntity() {

    fun isExpired(): Boolean = LocalDateTime.now().isAfter(expiresAt)

    fun refresh(newToken: String, newExpiresAt: LocalDateTime) {
        this.token = newToken
        this.expiresAt = newExpiresAt
    }
}
