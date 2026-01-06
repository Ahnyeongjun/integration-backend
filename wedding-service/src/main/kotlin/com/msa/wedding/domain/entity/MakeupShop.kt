package com.msa.wedding.domain.entity

import com.msa.common.entity.BaseTimeEntity
import jakarta.persistence.*

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

    @Column(length = 50)
    var phone: String? = null,

    @Column(name = "sns_url", length = 500)
    var snsUrl: String? = null,

    @Column(length = 200)
    var specialty: String? = null,

    @Column(name = "avg_rating")
    var avgRating: Double = 0.0,

    @Column(name = "on_site_available")
    var onSiteAvailable: Boolean = true
) : BaseTimeEntity()

enum class MakeupServiceType {
    MAKEUP_ONLY,
    HAIR_ONLY,
    MAKEUP_AND_HAIR,
    FULL_SERVICE
}
