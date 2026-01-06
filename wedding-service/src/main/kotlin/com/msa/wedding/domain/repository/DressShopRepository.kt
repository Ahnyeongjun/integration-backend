package com.msa.wedding.domain.repository

import com.msa.wedding.domain.entity.DressShop
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface DressShopRepository : JpaRepository<DressShop, Long> {

    @Query("""
        SELECT s FROM DressShop s
        WHERE (:shopName IS NULL OR s.shopName LIKE %:shopName%)
        AND (:address IS NULL OR s.address LIKE %:address%)
        AND (:specialty IS NULL OR s.specialty LIKE %:specialty%)
        ORDER BY s.createdAt DESC
    """)
    fun searchShops(
        shopName: String?,
        address: String?,
        specialty: String?,
        pageable: Pageable
    ): Page<DressShop>

    @Query("SELECT s FROM DressShop s ORDER BY s.createdAt DESC")
    fun findAllOrderByCreatedAtDesc(pageable: Pageable): Page<DressShop>
}
