package com.msa.auth.domain.repository

import com.msa.auth.domain.entity.LocalAccount
import com.msa.auth.domain.entity.LoginType
import com.msa.auth.domain.entity.OAuthAccount
import org.springframework.data.jpa.repository.JpaRepository

interface LocalAccountRepository : JpaRepository<LocalAccount, Long> {
    fun findByEmail(email: String): LocalAccount?
    fun existsByEmail(email: String): Boolean
    fun findByUserId(userId: Long): LocalAccount?
}

interface OAuthAccountRepository : JpaRepository<OAuthAccount, Long> {
    fun findByProviderAndProviderUserId(provider: LoginType, providerUserId: String): OAuthAccount?
    fun findByUserId(userId: Long): List<OAuthAccount>
    fun existsByProviderAndProviderUserId(provider: LoginType, providerUserId: String): Boolean
}
