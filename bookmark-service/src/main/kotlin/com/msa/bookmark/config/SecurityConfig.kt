package com.msa.bookmark.config

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

                    // 공개 API
                    .requestMatchers(HttpMethod.GET, "/api/v1/bookmarks/count").permitAll()

                    // 인증 필수 API
                    .requestMatchers(HttpMethod.GET, "/api/v1/bookmarks/my").authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/v1/bookmarks/check").authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/v1/bookmarks/**").authenticated()
                    .requestMatchers(HttpMethod.PUT, "/api/v1/bookmarks/**").authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/bookmarks/**").authenticated()

                    .anyRequest().permitAll()
            }
            .build()
    }
}
