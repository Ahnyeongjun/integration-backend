package com.msa.wedding.domain.repository

import com.msa.wedding.domain.entity.HallType
import com.msa.wedding.domain.entity.WeddingHall
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface WeddingHallRepository : JpaRepository<WeddingHall, Long> {

    fun findByHallType(hallType: HallType, pageable: Pageable): Page<WeddingHall>

    @Query("SELECT h FROM WeddingHall h WHERE h.name LIKE %:keyword% OR h.address LIKE %:keyword%")
    fun searchByKeyword(keyword: String, pageable: Pageable): Page<WeddingHall>

    @Query("SELECT h FROM WeddingHall h WHERE h.minGuarantee <= :guests AND h.maxCapacity >= :guests")
    fun findByCapacity(guests: Int, pageable: Pageable): Page<WeddingHall>

    // 정렬 쿼리들
    @Query("SELECT h FROM WeddingHall h ORDER BY h.createdAt DESC")
    fun findAllOrderByCreatedAtDesc(pageable: Pageable): Page<WeddingHall>

    @Query("SELECT h FROM WeddingHall h WHERE h.hallType = :hallType ORDER BY h.createdAt DESC")
    fun findByHallTypeOrderByCreatedAtDesc(hallType: HallType, pageable: Pageable): Page<WeddingHall>

    @Query("SELECT h FROM WeddingHall h WHERE h.minGuarantee <= :guests AND h.maxCapacity >= :guests ORDER BY h.createdAt DESC")
    fun findByCapacityOrderByCreatedAtDesc(guests: Int, pageable: Pageable): Page<WeddingHall>

    @Query("SELECT h FROM WeddingHall h WHERE h.name LIKE %:keyword% OR h.address LIKE %:keyword% ORDER BY h.createdAt DESC")
    fun searchByKeywordOrderByCreatedAtDesc(keyword: String, pageable: Pageable): Page<WeddingHall>
}
