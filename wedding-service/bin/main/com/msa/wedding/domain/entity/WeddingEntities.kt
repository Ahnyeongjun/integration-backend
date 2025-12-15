package com.msa.wedding.domain.entity

import com.msa.common.entity.BaseTimeEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "wedding_halls")
class WeddingHall(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = false, length = 200)
    var name: String,
    @Column(nullable = false, length = 500)
    var address: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var hallType: HallType,
    @Column(columnDefinition = "TEXT")
    var description: String? = null,
    @Column(name = "min_guarantee")
    var minGuarantee: Int? = null,
    @Column(name = "max_capacity")
    var maxCapacity: Int? = null,
    @Column(name = "meal_price")
    var mealPrice: Int? = null,
    @Column(name = "hall_rental_price")
    var hallRentalPrice: Int? = null,
    @Column(name = "cover_image", length = 500)
    var coverImage: String? = null,
    @Column(name = "avg_rating")
    var avgRating: Double = 0.0,
    @Column(name = "review_count")
    var reviewCount: Int = 0
) : BaseTimeEntity()

enum class HallType { HOTEL, CONVENTION, HOUSE, CHURCH, OUTDOOR, RESTAURANT, OTHER }

@Entity
@Table(name = "dresses")
class Dress(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = false, length = 200)
    var shopName: String,
    @Column(nullable = false, length = 500)
    var address: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var dressType: DressType,
    @Column(name = "min_price")
    var minPrice: Int? = null,
    @Column(name = "max_price")
    var maxPrice: Int? = null,
    @Column(name = "cover_image", length = 500)
    var coverImage: String? = null,
    @Column(name = "avg_rating")
    var avgRating: Double = 0.0
) : BaseTimeEntity()

enum class DressType { WEDDING_DRESS, HANBOK, TUXEDO, SUIT, BOTH }

@Entity
@Table(name = "makeup_shops")
class MakeupShop(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = false, length = 200)
    var name: String,
    @Column(nullable = false, length = 500)
    var address: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var serviceType: MakeupServiceType,
    @Column(name = "base_price")
    var basePrice: Int? = null,
    @Column(name = "cover_image", length = 500)
    var coverImage: String? = null,
    @Column(name = "avg_rating")
    var avgRating: Double = 0.0,
    @Column(name = "on_site_available")
    var onSiteAvailable: Boolean = true
) : BaseTimeEntity()

enum class MakeupServiceType { MAKEUP_ONLY, HAIR_ONLY, MAKEUP_AND_HAIR, FULL_SERVICE }

@Entity
@Table(name = "wedding_plans")
class WeddingPlan(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "user_id", nullable = false)
    val userId: Long,
    @Column(nullable = false, length = 100)
    var title: String,
    @Column(name = "wedding_date")
    var weddingDate: LocalDate? = null,
    @Column
    var budget: Long? = null,
    @Column(name = "expected_guests")
    var expectedGuests: Int? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hall_id")
    var selectedHall: WeddingHall? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dress_id")
    var selectedDress: Dress? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "makeup_shop_id")
    var selectedMakeupShop: MakeupShop? = null
) : BaseTimeEntity() {
    fun calculateTotalEstimate(): Long {
        var total = 0L
        selectedHall?.let { total += (it.hallRentalPrice ?: 0) + ((it.mealPrice ?: 0) * (expectedGuests ?: 0)) }
        selectedDress?.let { total += it.minPrice ?: 0 }
        selectedMakeupShop?.let { total += it.basePrice ?: 0 }
        return total
    }
}
