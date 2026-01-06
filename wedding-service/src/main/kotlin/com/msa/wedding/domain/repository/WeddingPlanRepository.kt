package com.msa.wedding.domain.repository

import com.msa.wedding.domain.entity.WeddingPlan
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface WeddingPlanRepository : JpaRepository<WeddingPlan, Long> {

    fun findByUserId(userId: Long, pageable: Pageable): Page<WeddingPlan>

    fun findByUserIdAndId(userId: Long, id: Long): WeddingPlan?
}
