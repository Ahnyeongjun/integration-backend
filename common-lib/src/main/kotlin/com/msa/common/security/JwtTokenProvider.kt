package com.msa.common.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.util.*
import javax.crypto.SecretKey

/**
 * JWT 토큰 통합 관리 (swyp10 방식 - Stateless)
 */
class JwtTokenProvider(
    private val secretKey: String,
    private val accessTokenExpiry: Long = 3600000,      // 1시간
    private val refreshTokenExpiry: Long = 604800000    // 7일
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(secretKey.toByteArray())

    companion object {
        // 클레임 키 상수
        const val CLAIM_EMAIL = "email"
        const val CLAIM_NICKNAME = "nickname"
        const val CLAIM_SIGNUP_COMPLETED = "signupCompleted"
        const val CLAIM_TOKEN_TYPE = "tokenType"
        const val CLAIM_PROVIDER = "provider"
        const val CLAIM_OAUTH_ACCOUNT_ID = "oauthAccountId"

        // 토큰 타입
        const val TOKEN_TYPE_USER = "USER"
        const val TOKEN_TYPE_OAUTH = "OAUTH"
        const val TOKEN_TYPE_ACCESS = "ACCESS"
        const val TOKEN_TYPE_REFRESH = "REFRESH"
    }

    // === 사용자 토큰 생성 ===

    /**
     * Access Token 생성 (완료된 사용자용)
     */
    fun createAccessToken(userId: Long, email: String, nickname: String, signupCompleted: Boolean): String {
        return Jwts.builder()
            .subject(userId.toString())
            .claim(CLAIM_EMAIL, email)
            .claim(CLAIM_NICKNAME, nickname)
            .claim(CLAIM_SIGNUP_COMPLETED, signupCompleted)
            .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_USER)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + accessTokenExpiry))
            .signWith(key)
            .compact()
    }

    /**
     * Refresh Token 생성 (완료된 사용자용)
     */
    fun createRefreshToken(userId: Long, email: String, nickname: String, signupCompleted: Boolean): String {
        return Jwts.builder()
            .subject(userId.toString())
            .claim(CLAIM_EMAIL, email)
            .claim(CLAIM_NICKNAME, nickname)
            .claim(CLAIM_SIGNUP_COMPLETED, signupCompleted)
            .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_USER)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + refreshTokenExpiry))
            .signWith(key)
            .compact()
    }

    /**
     * OAuth 임시 토큰 생성 (추가 회원가입 필요)
     */
    fun createOAuthToken(oauthAccountId: Long, provider: String, providerNickname: String?): String {
        return Jwts.builder()
            .subject(oauthAccountId.toString())
            .claim(CLAIM_PROVIDER, provider)
            .claim(CLAIM_NICKNAME, providerNickname ?: "사용자")
            .claim(CLAIM_SIGNUP_COMPLETED, false)
            .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_OAUTH)
            .claim(CLAIM_OAUTH_ACCOUNT_ID, oauthAccountId)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + accessTokenExpiry))
            .signWith(key)
            .compact()
    }

    // === 토큰 검증 ===

    fun validateToken(token: String): Boolean {
        return try {
            val claims = getClaims(token)
            !claims.expiration.before(Date())
        } catch (e: Exception) {
            false
        }
    }

    // === 클레임 추출 ===

    fun getUserId(token: String): Long {
        return getClaims(token).subject.toLong()
    }

    fun getEmail(token: String): String {
        return getClaims(token)[CLAIM_EMAIL] as? String ?: ""
    }

    fun getNickname(token: String): String {
        return getClaims(token)[CLAIM_NICKNAME] as? String ?: ""
    }

    fun isSignupCompleted(token: String): Boolean {
        return getClaims(token)[CLAIM_SIGNUP_COMPLETED] as? Boolean ?: false
    }

    fun getTokenType(token: String): String {
        return getClaims(token)[CLAIM_TOKEN_TYPE] as? String ?: TOKEN_TYPE_USER
    }

    fun getProvider(token: String): String? {
        return getClaims(token)[CLAIM_PROVIDER] as? String
    }

    fun getOAuthAccountId(token: String): Long? {
        val value = getClaims(token)[CLAIM_OAUTH_ACCOUNT_ID]
        return when (value) {
            is Long -> value
            is Int -> value.toLong()
            is String -> value.toLongOrNull()
            else -> null
        }
    }

    /**
     * OAuth 토큰인지 확인
     */
    fun isOAuthToken(token: String): Boolean {
        return getTokenType(token) == TOKEN_TYPE_OAUTH
    }

    private fun getClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}
