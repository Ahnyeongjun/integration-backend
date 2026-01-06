package com.msa.wedding.domain.entity

import com.msa.common.entity.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "halls")
class Hall(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wedding_hall_id", nullable = false)
    val weddingHall: WeddingHall,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(name = "floor")
    var floor: Int? = null,

    @Column(name = "min_capacity")
    var minCapacity: Int? = null,

    @Column(name = "max_capacity")
    var maxCapacity: Int? = null,

    @Column(name = "rental_price")
    var rentalPrice: Int? = null,

    @Column(name = "meal_price")
    var mealPrice: Int? = null,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "image_url", length = 500)
    var imageUrl: String? = null,

    @Column(name = "is_available")
    var isAvailable: Boolean = true
) : BaseTimeEntity()
