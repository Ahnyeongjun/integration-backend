package com.msa.user.domain.repository

import com.msa.user.domain.entity.User
import com.msa.user.domain.entity.UserStatus
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
    fun findByIdAndStatus(id: Long, status: UserStatus): User?
    fun existsByEmail(email: String): Boolean
}
