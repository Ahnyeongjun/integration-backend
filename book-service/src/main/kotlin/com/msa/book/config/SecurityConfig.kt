package com.msa.book.config

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
        return configure(http)
            .authorizeHttpRequests { auth ->
                auth
                    // Swagger, Actuator
                    .requestMatchers(*PublicPaths.SWAGGER).permitAll()
                    .requestMatchers(*PublicPaths.ACTUATOR).permitAll()

                    // 책 조회 API는 모두 공개
                    .requestMatchers(HttpMethod.GET, "/api/v1/books/**").permitAll()

                    // 관리자 API (있을 경우)는 인증 필수
                    .requestMatchers(HttpMethod.POST, "/api/v1/books/**").authenticated()
                    .requestMatchers(HttpMethod.PATCH, "/api/v1/books/**").authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/books/**").authenticated()

                    .anyRequest().permitAll()
            }
            .build()
    }
}
