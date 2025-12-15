package com.msa.wedding.domain.repository

import com.msa.wedding.domain.entity.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface WeddingHallRepository : JpaRepository<WeddingHall, Long> {
    fun findByHallType(hallType: HallType, pageable: Pageable): Page<WeddingHall>
    @Query("SELECT h FROM WeddingHall h WHERE h.name LIKE %:keyword% OR h.address LIKE %:keyword%")
    fun searchByKeyword(keyword: String, pageable: Pageable): Page<WeddingHall>
    @Query("SELECT h FROM WeddingHall h WHERE h.minGuarantee <= :guests AND h.maxCapacity >= :guests ORDER BY h.avgRating DESC")
    fun findByCapacity(guests: Int, pageable: Pageable): Page<WeddingHall>
}

interface DressRepository : JpaRepository<Dress, Long> {
    fun findByDressType(dressType: DressType, pageable: Pageable): Page<Dress>
    @Query("SELECT d FROM Dress d WHERE d.shopName LIKE %:keyword% OR d.address LIKE %:keyword%")
    fun searchByKeyword(keyword: String, pageable: Pageable): Page<Dress>
}

interface MakeupShopRepository : JpaRepository<MakeupShop, Long> {
    fun findByServiceType(serviceType: MakeupServiceType, pageable: Pageable): Page<MakeupShop>
    @Query("SELECT m FROM MakeupShop m WHERE m.onSiteAvailable = true")
    fun findOnSiteAvailable(pageable: Pageable): Page<MakeupShop>
}

interface WeddingPlanRepository : JpaRepository<WeddingPlan, Long> {
    fun findByUserId(userId: Long, pageable: Pageable): Page<WeddingPlan>
    fun findByUserIdAndId(userId: Long, id: Long): WeddingPlan?
}
