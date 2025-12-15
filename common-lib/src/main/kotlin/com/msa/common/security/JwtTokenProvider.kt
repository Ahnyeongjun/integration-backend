package com.msa.common.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.util.*
import javax.crypto.SecretKey

class JwtTokenProvider(
    private val secretKey: String,
    private val accessTokenExpiry: Long = 3600000,      // 1 hour
    private val refreshTokenExpiry: Long = 604800000    // 7 days
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(secretKey.toByteArray())

    fun createAccessToken(userId: Long, email: String): String {
        return createToken(userId, email, accessTokenExpiry, "ACCESS")
    }

    fun createRefreshToken(userId: Long, email: String): String {
        return createToken(userId, email, refreshTokenExpiry, "REFRESH")
    }

    private fun createToken(userId: Long, email: String, expiry: Long, type: String): String {
        val now = Date()
        val expiryDate = Date(now.time + expiry)

        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .claim("type", type)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            val claims = getClaims(token)
            !claims.expiration.before(Date())
        } catch (e: Exception) {
            false
        }
    }

    fun getUserId(token: String): Long {
        return getClaims(token).subject.toLong()
    }

    fun getEmail(token: String): String {
        return getClaims(token)["email"] as String
    }

    fun getTokenType(token: String): String {
        return getClaims(token)["type"] as String
    }

    private fun getClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}
