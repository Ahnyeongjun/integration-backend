package com.msa.auth.domain.entity

import com.msa.common.entity.BaseTimeEntity
import jakarta.persistence.*

/**
 * 로그인 타입 (이메일 또는 OAuth 제공자)
 */
enum class LoginType {
    EMAIL, KAKAO, GOOGLE, NAVER
}

/**
 * 이메일/패스워드 로그인용 계정
 */
@Entity
@Table(name = "local_accounts")
class LocalAccount(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true, length = 100)
    val email: String,

    @Column(nullable = false, length = 200)
    var password: String,

    @Column(name = "user_id", nullable = false, unique = true)
    val userId: Long,

    @Column(name = "is_verified", nullable = false)
    var isVerified: Boolean = false

) : BaseTimeEntity()

/**
 * OAuth 계정 (swyp10 방식 - provider 토큰 저장 안함)
 * JWT Stateless 방식으로 RefreshToken DB 저장 없음
 */
@Entity
@Table(name = "oauth_accounts")
class OAuthAccount(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "oauth_id")
    val oauthId: Long = 0,

    @Column(name = "user_id")
    var userId: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val provider: LoginType,

    @Column(name = "provider_user_id", nullable = false, length = 100)
    val providerUserId: String,

    @Column(name = "provider_email", length = 100)
    var providerEmail: String? = null,

    @Column(name = "provider_nickname", length = 100)
    var providerNickname: String? = null,

    @Column(name = "provider_profile_image", columnDefinition = "TEXT")
    var providerProfileImage: String? = null

) : BaseTimeEntity() {

    fun linkUser(userId: Long) {
        this.userId = userId
    }

    fun isLinkedToUser(): Boolean = userId != null
}
