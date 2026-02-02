package com.msa.auth.config

import com.msa.common.security.BaseSecurityConfig
import com.msa.common.security.JwtTokenProvider
import com.msa.common.security.PublicPaths
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig(
    @Value("\${jwt.secret}") private val jwtSecret: String,
    @Value("\${jwt.access-token-expiry:3600000}") private val accessTokenExpiry: Long,
    @Value("\${jwt.refresh-token-expiry:604800000}") private val refreshTokenExpiry: Long
) : BaseSecurityConfig() {

    @Bean
    fun jwtTokenProvider(): JwtTokenProvider {
        return JwtTokenProvider(jwtSecret, accessTokenExpiry, refreshTokenExpiry)
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return configure(http)
            .authorizeHttpRequests { auth ->
                auth
                    // Auth API는 대부분 공개
                    .requestMatchers(*PublicPaths.AUTH).permitAll()
                    .requestMatchers(*PublicPaths.SWAGGER).permitAll()
                    .requestMatchers(*PublicPaths.ACTUATOR).permitAll()
                    // 인증 필요 API
                    .requestMatchers("/api/v1/auth/logout").authenticated()
                    .requestMatchers("/api/v1/auth/password").authenticated()
                    .anyRequest().permitAll()
            }
            .build()
    }
}
