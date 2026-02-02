package com.msa.common.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

/**
 * 기본 Security 설정
 * 각 서비스에서 상속받아 사용하거나 참고
 */
@EnableMethodSecurity(prePostEnabled = true)
abstract class BaseSecurityConfig {

    /**
     * 공통 Security 설정 적용
     */
    protected fun configure(http: HttpSecurity): HttpSecurity {
        return http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .anonymous { it.disable() }
            .addFilterBefore(
                HeaderAuthenticationFilter(),
                UsernamePasswordAuthenticationFilter::class.java
            )
    }

    /**
     * 내부 API 토큰 검증 필터 포함 Security 설정
     */
    protected fun configureWithInternalApi(http: HttpSecurity, internalToken: String): HttpSecurity {
        return configure(http)
            .addFilterBefore(
                InternalApiFilter(internalToken),
                HeaderAuthenticationFilter::class.java
            )
    }

    /**
     * OAuth 미완료 사용자 접근 제한 필터 포함 Security 설정
     */
    protected fun configureWithSignupCheck(http: HttpSecurity): HttpSecurity {
        return configure(http)
            .addFilterAfter(
                SignupCompletedFilter(),
                HeaderAuthenticationFilter::class.java
            )
    }

    /**
     * 모든 필터 포함 Security 설정 (내부 API + 회원가입 체크)
     */
    protected fun configureWithAllFilters(http: HttpSecurity, internalToken: String): HttpSecurity {
        return configure(http)
            .addFilterBefore(
                InternalApiFilter(internalToken),
                HeaderAuthenticationFilter::class.java
            )
            .addFilterAfter(
                SignupCompletedFilter(),
                HeaderAuthenticationFilter::class.java
            )
    }
}

/**
 * PasswordEncoder 설정 (별도 Config로 분리)
 */
@Configuration
class PasswordEncoderConfig {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}

/**
 * 기본 공개 경로
 */
object PublicPaths {
    val SWAGGER = arrayOf(
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/v3/api-docs/**",
        "/swagger-resources/**"
    )

    val ACTUATOR = arrayOf(
        "/actuator/**"
    )

    val AUTH = arrayOf(
        "/api/v1/auth/login",
        "/api/v1/auth/signup",
        "/api/v1/auth/signup/complete",
        "/api/v1/auth/oauth/**",
        "/api/v1/auth/refresh",
        "/api/v1/auth/email/**",
        "/api/v1/auth/password/**"
    )

    val ALL_PUBLIC = SWAGGER + ACTUATOR + AUTH
}
