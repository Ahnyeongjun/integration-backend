package com.msa.travel.domain.entity

import com.msa.common.entity.BaseTimeEntity
import jakarta.persistence.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Entity
@Table(name = "itineraries")
class Itinerary(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "user_id", nullable = false)
    val userId: Long,
    @Column(nullable = false, length = 100)
    var title: String,
    @Column(name = "start_date", nullable = false)
    var startDate: LocalDate,
    @Column(name = "end_date", nullable = false)
    var endDate: LocalDate,
    @Column(name = "is_public")
    var isPublic: Boolean = false,
    @Column(columnDefinition = "TEXT")
    var description: String? = null,
    @Column(name = "cover_image", length = 500)
    var coverImage: String? = null,
    @OneToMany(mappedBy = "itinerary", cascade = [CascadeType.ALL], orphanRemoval = true)
    val dailySchedules: MutableList<DailySchedule> = mutableListOf()
) : BaseTimeEntity() {
    fun getTotalDays(): Int = ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1
}

@Entity
@Table(name = "daily_schedules")
class DailySchedule(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_id", nullable = false)
    val itinerary: Itinerary,
    @Column(nullable = false)
    val day: Int,
    @Column(nullable = false)
    val date: LocalDate,
    @OneToMany(mappedBy = "dailySchedule", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    val items: MutableList<ScheduleItem> = mutableListOf()
) : BaseTimeEntity()

@Entity
@Table(name = "schedule_items")
class ScheduleItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_schedule_id", nullable = false)
    val dailySchedule: DailySchedule,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attraction_id")
    var attraction: Attraction? = null,
    @Column(name = "order_index", nullable = false)
    var orderIndex: Int,
    @Column(name = "stay_minutes")
    var stayMinutes: Int = 60,
    @Column(length = 500)
    var memo: String? = null
) : BaseTimeEntity()

@Entity
@Table(name = "attractions")
class Attraction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = false, length = 200)
    var name: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var type: AttractionType,
    @Column(nullable = false, length = 500)
    var address: String,
    @Column(columnDefinition = "TEXT")
    var description: String? = null,
    @Column
    var latitude: Double? = null,
    @Column
    var longitude: Double? = null,
    @Column
    var rating: Double? = null
) : BaseTimeEntity()

enum class AttractionType {
    TOURIST_SPOT, RESTAURANT, CAFE, ACCOMMODATION, SHOPPING, ACTIVITY
}
