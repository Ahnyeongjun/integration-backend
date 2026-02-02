package com.msa.user.config

import com.msa.common.security.BaseSecurityConfig
import com.msa.common.security.PublicPaths
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig : BaseSecurityConfig() {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return configureWithSignupCheck(http)
            .authorizeHttpRequests { auth ->
                auth
                    // Swagger, Actuator
                    .requestMatchers(*PublicPaths.SWAGGER).permitAll()
                    .requestMatchers(*PublicPaths.ACTUATOR).permitAll()

                    // 공개 API - 사용자 조회
                    .requestMatchers(HttpMethod.GET, "/api/v1/users/{userId}").permitAll()

                    // 인증 필수 API
                    .requestMatchers(HttpMethod.GET, "/api/v1/users/me").authenticated()
                    .requestMatchers(HttpMethod.PATCH, "/api/v1/users/me").authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/users/me").authenticated()
                    .requestMatchers("/api/v1/users/experience/**").authenticated()

                    .anyRequest().authenticated()
            }
            .build()
    }
}
