package com.msa.user.domain.repository

import com.msa.user.domain.entity.UserExperience
import org.springframework.data.jpa.repository.JpaRepository

interface UserExperienceRepository : JpaRepository<UserExperience, Long> {
    fun findByUserId(userId: Long): UserExperience?
    fun existsByUserId(userId: Long): Boolean
}
