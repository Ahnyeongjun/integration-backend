package com.msa.common.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter

/**
 * 내부 서비스 간 API 호출 시 X-Internal-Token 헤더 검증 필터
 */
class InternalApiFilter(
    private val internalToken: String,
    private val internalApiPaths: List<String> = listOf("/api/v1/*/internal/**", "/internal/**")
) : OncePerRequestFilter() {

    companion object {
        const val INTERNAL_TOKEN_HEADER = "X-Internal-Token"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val requestPath = request.requestURI

        if (isInternalApiPath(requestPath)) {
            val token = request.getHeader(INTERNAL_TOKEN_HEADER)

            if (token.isNullOrBlank() || token != internalToken) {
                response.status = HttpServletResponse.SC_FORBIDDEN
                response.contentType = "application/json"
                response.characterEncoding = "UTF-8"
                response.writer.write("""{"code":"FORBIDDEN","message":"내부 API 접근 권한이 없습니다"}""")
                return
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun isInternalApiPath(path: String): Boolean {
        return internalApiPaths.any { pattern ->
            matchPath(pattern, path)
        }
    }

    private fun matchPath(pattern: String, path: String): Boolean {
        val regexPattern = pattern
            .replace("**", ".*")
            .replace("*", "[^/]*")
        return Regex(regexPattern).matches(path)
    }
}
