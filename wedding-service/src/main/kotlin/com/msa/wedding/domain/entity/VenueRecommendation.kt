package com.msa.wedding.domain.entity

import com.msa.common.entity.BaseTimeEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "venue_recommendations",
    indexes = [
        Index(name = "idx_venue_rec_hash", columnList = "query_hash", unique = true),
        Index(name = "idx_venue_rec_params", columnList = "guest_count, budget, region, style_preference, season")
    ]
)
class VenueRecommendation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "query_hash", nullable = false, length = 64, unique = true)
    val queryHash: String,

    @Column(name = "guest_count", nullable = false, length = 20)
    val guestCount: String,

    @Column(name = "budget", nullable = false, length = 20)
    val budget: String,

    @Column(name = "region", nullable = false, length = 20)
    val region: String,

    @Column(name = "style_preference", nullable = false, length = 20)
    val stylePreference: String,

    @Column(name = "season", nullable = false, length = 20)
    val season: String,

    @Column(name = "recommendation", columnDefinition = "JSON", nullable = false)
    val recommendation: String,

    @Column(name = "access_count")
    var accessCount: Int = 0,

    @Column(name = "last_accessed")
    var lastAccessed: LocalDateTime? = null
) : BaseTimeEntity() {

    fun incrementAccess() {
        accessCount++
        lastAccessed = LocalDateTime.now()
    }

    companion object {
        fun generateHash(guestCount: String, budget: String, region: String, stylePreference: String, season: String): String {
            val combined = "$guestCount|$budget|$region|$stylePreference|$season"
            return combined.hashCode().toString(16).padStart(16, '0')
        }
    }
}

enum class GuestCountRange {
    SMALL,      // ~50명
    MEDIUM,     // 50~100명
    LARGE,      // 100~200명
    EXTRA_LARGE // 200명~
}

enum class BudgetRange {
    ECONOMY,    // ~3000만원
    STANDARD,   // 3000~5000만원
    PREMIUM,    // 5000~8000만원
    LUXURY      // 8000만원~
}

enum class Region {
    SEOUL, GYEONGGI, INCHEON, BUSAN, DAEGU, DAEJEON, GWANGJU, JEJU, OTHER
}

enum class StylePreference {
    MODERN, CLASSIC, ROMANTIC, NATURAL, LUXURY, MINIMAL
}

enum class WeddingSeason {
    SPRING, SUMMER, FALL, WINTER
}
