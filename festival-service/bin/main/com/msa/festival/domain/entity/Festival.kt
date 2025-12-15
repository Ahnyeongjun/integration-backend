package com.msa.festival.domain.entity

import com.msa.common.entity.BaseTimeEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "festivals")
class Festival(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "content_id", unique = true)
    val contentId: String,
    @Column(nullable = false, length = 200)
    var title: String,
    @Column(columnDefinition = "TEXT")
    var description: String? = null,
    @Column(length = 500)
    var address: String? = null,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var category: FestivalCategory,
    @Column(name = "start_date")
    var startDate: LocalDate? = null,
    @Column(name = "end_date")
    var endDate: LocalDate? = null,
    @Column(name = "cover_image", length = 500)
    var coverImage: String? = null,
    @Column
    var latitude: Double? = null,
    @Column
    var longitude: Double? = null,
    @Column(name = "view_count")
    var viewCount: Long = 0,
    @Column(name = "avg_rating")
    var avgRating: Double = 0.0,
    @Column(name = "review_count")
    var reviewCount: Int = 0
) : BaseTimeEntity() {
    fun isOngoing(): Boolean {
        val today = LocalDate.now()
        return startDate?.let { start ->
            endDate?.let { end -> !today.isBefore(start) && !today.isAfter(end) } ?: !today.isBefore(start)
        } ?: true
    }
}

enum class FestivalCategory {
    FESTIVAL, PERFORMANCE, EXHIBITION, CULTURE, NATURE, LEISURE, SHOPPING, FOOD, ACCOMMODATION
}
