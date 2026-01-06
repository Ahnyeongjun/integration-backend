package com.msa.wedding.domain.repository

import com.msa.wedding.domain.entity.MakeupServiceType
import com.msa.wedding.domain.entity.MakeupShop
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface MakeupShopRepository : JpaRepository<MakeupShop, Long> {

    fun findByServiceType(serviceType: MakeupServiceType, pageable: Pageable): Page<MakeupShop>

    @Query("SELECT m FROM MakeupShop m WHERE m.onSiteAvailable = true")
    fun findOnSiteAvailable(pageable: Pageable): Page<MakeupShop>

    @Query("""
        SELECT m FROM MakeupShop m
        WHERE (:name IS NULL OR m.name LIKE %:name%)
        AND (:address IS NULL OR m.address LIKE %:address%)
        AND (:specialty IS NULL OR m.specialty LIKE %:specialty%)
        ORDER BY m.createdAt DESC
    """)
    fun searchShops(
        name: String?,
        address: String?,
        specialty: String?,
        pageable: Pageable
    ): Page<MakeupShop>

    @Query("SELECT m FROM MakeupShop m ORDER BY m.createdAt DESC")
    fun findAllOrderByCreatedAtDesc(pageable: Pageable): Page<MakeupShop>
}
