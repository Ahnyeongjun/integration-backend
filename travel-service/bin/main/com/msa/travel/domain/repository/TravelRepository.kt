package com.msa.travel.domain.repository

import com.msa.travel.domain.entity.Attraction
import com.msa.travel.domain.entity.AttractionType
import com.msa.travel.domain.entity.Itinerary
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ItineraryRepository : JpaRepository<Itinerary, Long> {
    fun findByUserId(userId: Long, pageable: Pageable): Page<Itinerary>
    @Query("SELECT i FROM Itinerary i WHERE i.isPublic = true ORDER BY i.createdAt DESC")
    fun findPublicItineraries(pageable: Pageable): Page<Itinerary>
}

interface AttractionRepository : JpaRepository<Attraction, Long> {
    fun findByType(type: AttractionType, pageable: Pageable): Page<Attraction>
    @Query("SELECT a FROM Attraction a WHERE a.name LIKE %:keyword% OR a.address LIKE %:keyword%")
    fun searchByKeyword(keyword: String, pageable: Pageable): Page<Attraction>
}
