package com.msa.ticketing.config

import com.msa.common.security.BaseSecurityConfig
import com.msa.common.security.PublicPaths
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig(
    @Value("\${internal.token:}") private val internalToken: String
) : BaseSecurityConfig() {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        val configuredHttp = if (internalToken.isNotBlank()) {
            configureWithAllFilters(http, internalToken)
        } else {
            configureWithSignupCheck(http)
        }

        return configuredHttp
            .authorizeHttpRequests { auth ->
                auth
                    // Swagger, Actuator
                    .requestMatchers(*PublicPaths.SWAGGER).permitAll()
                    .requestMatchers(*PublicPaths.ACTUATOR).permitAll()

                    // 내부 서비스 API
                    .requestMatchers("/api/v1/ticketing/internal/**").permitAll()

                    // 공개 API - 조회
                    .requestMatchers(HttpMethod.GET, "/api/v1/ticketing/**").permitAll()

                    // 인증 필수 API - 예매
                    .requestMatchers(HttpMethod.POST, "/api/v1/ticketing/**").authenticated()
                    .requestMatchers(HttpMethod.PATCH, "/api/v1/ticketing/**").authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/ticketing/**").authenticated()

                    .anyRequest().authenticated()
            }
            .build()
    }
}
