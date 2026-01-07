package com.msa.wedding.domain.repository

import com.msa.wedding.domain.entity.VenueRecommendation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface VenueRecommendationRepository : JpaRepository<VenueRecommendation, Long> {

    fun findByQueryHash(queryHash: String): Optional<VenueRecommendation>

    @Query("""
        SELECT vr FROM VenueRecommendation vr
        WHERE vr.guestCount = :guestCount
        AND vr.budget = :budget
        AND vr.region = :region
        AND vr.stylePreference = :stylePreference
        AND vr.season = :season
    """)
    fun findByVenueParams(
        @Param("guestCount") guestCount: String,
        @Param("budget") budget: String,
        @Param("region") region: String,
        @Param("stylePreference") stylePreference: String,
        @Param("season") season: String
    ): Optional<VenueRecommendation>

    @Query("SELECT vr FROM VenueRecommendation vr ORDER BY vr.accessCount DESC")
    fun findTopByAccessCount(): List<VenueRecommendation>

    fun existsByQueryHash(queryHash: String): Boolean
}
