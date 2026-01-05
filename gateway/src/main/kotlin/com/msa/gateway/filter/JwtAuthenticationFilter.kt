package com.msa.gateway.filter

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import javax.crypto.SecretKey

/**
 * Gateway JWT 인증 필터
 *
 * 역할: 토큰이 있으면 검증하고 헤더로 전달, 없으면 그냥 통과
 * 각 서비스에서 @RequireAuth, @OptionalAuth 등으로 필수/선택 결정
 */
@Component
class JwtAuthenticationFilter(
    @Value("\${jwt.secret}") private val secretKey: String
) : GlobalFilter, Ordered {

    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(secretKey.toByteArray())
    }

    companion object {
        // 헤더 키
        const val HEADER_USER_ID = "X-User-Id"
        const val HEADER_USER_EMAIL = "X-User-Email"
        const val HEADER_USER_NICKNAME = "X-User-Nickname"
        const val HEADER_SIGNUP_COMPLETED = "X-Signup-Completed"
        const val HEADER_TOKEN_TYPE = "X-Token-Type"
    }

    override fun getOrder(): Int = -1

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val request = exchange.request
        val authHeader = request.headers.getFirst("Authorization")

        // 토큰 없으면 그냥 통과 (각 서비스에서 필수 여부 판단)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange)
        }

        val token = authHeader.removePrefix("Bearer ")

        return try {
            val claims = validateAndGetClaims(token)
            val modifiedRequest = addUserInfoHeaders(request, claims)
            chain.filter(exchange.mutate().request(modifiedRequest).build())
        } catch (e: Exception) {
            // 토큰이 있는데 유효하지 않으면 401
            onError(exchange, "Invalid token: ${e.message}", HttpStatus.UNAUTHORIZED)
        }
    }

    private fun validateAndGetClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    private fun addUserInfoHeaders(request: ServerHttpRequest, claims: Claims): ServerHttpRequest {
        val userId = claims.subject
        val email = claims["email"] as? String ?: ""
        val nickname = claims["nickname"] as? String ?: ""
        val signupCompleted = claims["signupCompleted"] as? Boolean ?: false
        val tokenType = claims["tokenType"] as? String ?: "USER"

        return request.mutate()
            .header(HEADER_USER_ID, userId)
            .header(HEADER_USER_EMAIL, email)
            .header(HEADER_USER_NICKNAME, nickname)
            .header(HEADER_SIGNUP_COMPLETED, signupCompleted.toString())
            .header(HEADER_TOKEN_TYPE, tokenType)
            .build()
    }

    private fun onError(exchange: ServerWebExchange, message: String, status: HttpStatus): Mono<Void> {
        val response = exchange.response
        response.statusCode = status
        return response.setComplete()
    }
}
