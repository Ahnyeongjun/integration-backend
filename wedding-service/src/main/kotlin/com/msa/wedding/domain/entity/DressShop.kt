package com.msa.wedding.domain.entity

import com.msa.common.entity.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "dress_shops")
class DressShop(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "shop_name", nullable = false, length = 200)
    var shopName: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(length = 500)
    var address: String? = null,

    @Column(length = 50)
    var phone: String? = null,

    @Column(name = "sns_url", length = 500)
    var snsUrl: String? = null,

    @Column(name = "image_url", length = 500)
    var imageUrl: String? = null,

    @Column(length = 200)
    var specialty: String? = null,

    @Column(columnDefinition = "TEXT")
    var features: String? = null,

    @Column(name = "avg_rating")
    var avgRating: Double = 0.0,

    @OneToMany(mappedBy = "dressShop", cascade = [CascadeType.ALL], orphanRemoval = true)
    val dresses: MutableList<Dress> = mutableListOf()
) : BaseTimeEntity()
