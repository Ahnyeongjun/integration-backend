package com.msa.common.security

import org.springframework.context.annotation.Bean
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
 *
 * 사용법:
 * ```
 * @Configuration
 * @EnableWebSecurity
 * class SecurityConfig : BaseSecurityConfig() {
 *
 *     @Bean
 *     fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
 *         return configure(http)
 *             .authorizeHttpRequests { auth ->
 *                 auth
 *                     .requestMatchers(HttpMethod.GET, "/api/v1/books/**").permitAll()
 *                     .requestMatchers(HttpMethod.POST, "/api/v1/books/**").authenticated()
 *                     .anyRequest().permitAll()
 *             }
 *             .build()
 *     }
 * }
 * ```
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
            .addFilterBefore(
                HeaderAuthenticationFilter(),
                UsernamePasswordAuthenticationFilter::class.java
            )
    }

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
