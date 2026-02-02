package com.msa.common.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

/**
 * OAuth 회원가입 미완료 사용자(signupCompleted=false)의 접근을 제한하는 필터
 * 허용된 경로만 접근 가능하고, 나머지는 403 Forbidden 반환
 */
class SignupCompletedFilter(
    private val allowedPaths: List<String> = DEFAULT_ALLOWED_PATHS
) : OncePerRequestFilter() {

    companion object {
        val DEFAULT_ALLOWED_PATHS = listOf(
            "/api/v1/auth/signup/complete",
            "/api/v1/users/me"
        )
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authentication = SecurityContextHolder.getContext().authentication
        val principal = authentication?.principal

        if (principal is UserPrincipal && !principal.signupCompleted) {
            val requestPath = request.requestURI
            val requestMethod = request.method

            // GET /api/v1/users/me 또는 POST /api/v1/auth/signup/complete만 허용
            val isAllowed = allowedPaths.any { allowedPath ->
                if (allowedPath == "/api/v1/users/me") {
                    requestPath == allowedPath && requestMethod == "GET"
                } else {
                    requestPath == allowedPath
                }
            }

            if (!isAllowed) {
                response.status = HttpServletResponse.SC_FORBIDDEN
                response.contentType = "application/json"
                response.characterEncoding = "UTF-8"
                response.writer.write("""{"code":"SIGNUP_INCOMPLETE","message":"회원가입을 완료해주세요"}""")
                return
            }
        }

        filterChain.doFilter(request, response)
    }
}
