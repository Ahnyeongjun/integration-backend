package com.msa.auth.domain.repository

import com.msa.auth.domain.entity.OAuthAccount
import com.msa.auth.domain.entity.OAuthProvider
import com.msa.auth.domain.entity.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository

interface OAuthAccountRepository : JpaRepository<OAuthAccount, Long> {
    fun findByProviderAndProviderId(provider: OAuthProvider, providerId: String): OAuthAccount?
    fun findByUserId(userId: Long): List<OAuthAccount>
    fun existsByProviderAndProviderId(provider: OAuthProvider, providerId: String): Boolean
}

interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    fun findByUserId(userId: Long): RefreshToken?
    fun findByToken(token: String): RefreshToken?
    fun deleteByUserId(userId: Long)
}
