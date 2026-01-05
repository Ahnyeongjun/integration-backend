package com.msa.common.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**
 * Spring Security Principal - Gateway에서 전달받은 사용자 정보
 */
data class UserPrincipal(
    val userId: Long,
    val email: String,
    val nickname: String,
    val signupCompleted: Boolean = true
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority("ROLE_USER"))
    }

    override fun getPassword(): String = ""

    override fun getUsername(): String = userId.toString()

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = signupCompleted
}
