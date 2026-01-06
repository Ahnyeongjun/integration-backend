package com.msa.wedding.domain.repository

import com.msa.wedding.domain.entity.Hall
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface HallRepository : JpaRepository<Hall, Long> {

    @Query("SELECT h FROM Hall h WHERE h.weddingHall.id = :weddingHallId ORDER BY h.floor, h.name")
    fun findByWeddingHallId(weddingHallId: Long): List<Hall>

    fun findByWeddingHallIdAndIsAvailableTrue(weddingHallId: Long): List<Hall>
}
