package com.msa.wedding.domain.entity

import com.msa.common.entity.BaseTimeEntity
import jakarta.persistence.*

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

    @Column(name = "phone", length = 50)
    var phone: String? = null,

    @Column(name = "email", length = 100)
    var email: String? = null,

    @Column(name = "parking")
    var parking: Int? = null,

    @Column(name = "avg_rating")
    var avgRating: Double = 0.0,

    @Column(name = "review_count")
    var reviewCount: Int = 0
) : BaseTimeEntity()

enum class HallType {
    HOTEL,
    CONVENTION,
    HOUSE,
    CHURCH,
    OUTDOOR,
    RESTAURANT,
    OTHER
}

enum class SortType {
    RECENT,
    FAVORITE
}
