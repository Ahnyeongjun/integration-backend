package com.msa.festival.domain.repository

import com.msa.festival.domain.entity.Festival
import com.msa.festival.domain.entity.FestivalCategory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface FestivalRepository : JpaRepository<Festival, Long> {
    fun findByContentId(contentId: String): Festival?
    fun findByCategory(category: FestivalCategory, pageable: Pageable): Page<Festival>
    @Query("SELECT f FROM Festival f WHERE f.startDate <= :today AND f.endDate >= :today ORDER BY f.viewCount DESC")
    fun findOngoingFestivals(today: LocalDate, pageable: Pageable): Page<Festival>
    @Query("SELECT f FROM Festival f WHERE f.title LIKE %:keyword% OR f.description LIKE %:keyword%")
    fun searchByKeyword(keyword: String, pageable: Pageable): Page<Festival>
    @Query("SELECT f FROM Festival f ORDER BY f.viewCount DESC")
    fun findPopular(pageable: Pageable): Page<Festival>
}
