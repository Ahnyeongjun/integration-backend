package com.msa.common.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Gateway에서 전달한 헤더를 SecurityContext에 설정하는 필터
 *
 * Gateway가 JWT 검증 후 다음 헤더를 추가:
 * - X-User-Id
 * - X-User-Email
 * - X-User-Nickname
 * - X-Signup-Completed
 */
class HeaderAuthenticationFilter : OncePerRequestFilter() {

    companion object {
        const val HEADER_USER_ID = "X-User-Id"
        const val HEADER_USER_EMAIL = "X-User-Email"
        const val HEADER_USER_NICKNAME = "X-User-Nickname"
        const val HEADER_SIGNUP_COMPLETED = "X-Signup-Completed"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val userId = request.getHeader(HEADER_USER_ID)?.toLongOrNull()

        if (userId != null) {
            val email = request.getHeader(HEADER_USER_EMAIL) ?: ""
            val nickname = request.getHeader(HEADER_USER_NICKNAME) ?: ""
            val signupCompleted = request.getHeader(HEADER_SIGNUP_COMPLETED)?.toBoolean() ?: true

            val principal = UserPrincipal(
                userId = userId,
                email = email,
                nickname = nickname,
                signupCompleted = signupCompleted
            )

            val authentication = UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.authorities
            )

            SecurityContextHolder.getContext().authentication = authentication
        }

        filterChain.doFilter(request, response)
    }
}
