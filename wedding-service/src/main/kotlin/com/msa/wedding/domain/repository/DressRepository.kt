package com.msa.wedding.domain.repository

import com.msa.wedding.domain.entity.Dress
import com.msa.wedding.domain.entity.DressType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface DressRepository : JpaRepository<Dress, Long> {

    fun findByDressType(dressType: DressType, pageable: Pageable): Page<Dress>

    fun findByDressShopId(dressShopId: Long, pageable: Pageable): Page<Dress>

    @Query("SELECT d FROM Dress d WHERE d.dressShop.shopName LIKE %:keyword% OR d.name LIKE %:keyword%")
    fun searchByKeyword(keyword: String, pageable: Pageable): Page<Dress>

    @Query("SELECT d FROM Dress d ORDER BY d.createdAt DESC")
    fun findAllOrderByCreatedAtDesc(pageable: Pageable): Page<Dress>

    @Query("SELECT d FROM Dress d WHERE d.dressType = :dressType ORDER BY d.createdAt DESC")
    fun findByDressTypeOrderByCreatedAtDesc(dressType: DressType, pageable: Pageable): Page<Dress>
}
