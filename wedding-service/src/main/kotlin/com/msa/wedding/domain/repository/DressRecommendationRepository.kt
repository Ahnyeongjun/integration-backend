package com.msa.wedding.domain.repository

import com.msa.wedding.domain.entity.DressRecommendation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface DressRecommendationRepository : JpaRepository<DressRecommendation, Long> {

    fun findByQueryHash(queryHash: String): Optional<DressRecommendation>

    @Query("""
        SELECT dr FROM DressRecommendation dr
        WHERE dr.armLength = :armLength
        AND dr.legLength = :legLength
        AND dr.neckLength = :neckLength
        AND dr.faceShape = :faceShape
    """)
    fun findByBodyParams(
        @Param("armLength") armLength: String,
        @Param("legLength") legLength: String,
        @Param("neckLength") neckLength: String,
        @Param("faceShape") faceShape: String
    ): Optional<DressRecommendation>

    @Query("""
        SELECT dr FROM DressRecommendation dr
        WHERE dr.armLength = :armLength
        AND dr.legLength = :legLength
        AND dr.neckLength = :neckLength
        AND dr.faceShape = :faceShape
        AND (:bodyType IS NULL OR dr.bodyType = :bodyType)
    """)
    fun findByBodyParamsWithType(
        @Param("armLength") armLength: String,
        @Param("legLength") legLength: String,
        @Param("neckLength") neckLength: String,
        @Param("faceShape") faceShape: String,
        @Param("bodyType") bodyType: String?
    ): Optional<DressRecommendation>

    @Query("SELECT dr FROM DressRecommendation dr ORDER BY dr.accessCount DESC")
    fun findTopByAccessCount(): List<DressRecommendation>

    fun existsByQueryHash(queryHash: String): Boolean
}
