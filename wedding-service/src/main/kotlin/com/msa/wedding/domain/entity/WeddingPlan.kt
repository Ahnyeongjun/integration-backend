package com.msa.wedding.domain.entity

import com.msa.common.entity.BaseTimeEntity
import jakarta.persistence.*
import java.time.LocalDate

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
    @JoinColumn(name = "dress_shop_id")
    var selectedDressShop: DressShop? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "makeup_shop_id")
    var selectedMakeupShop: MakeupShop? = null
) : BaseTimeEntity() {

    fun calculateTotalEstimate(): Long {
        var total = 0L
        selectedHall?.let {
            total += (it.hallRentalPrice ?: 0) + ((it.mealPrice ?: 0) * (expectedGuests ?: 0))
        }
        selectedMakeupShop?.let { total += it.basePrice ?: 0 }
        return total
    }
}
