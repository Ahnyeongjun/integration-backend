package com.msa.wedding.domain.entity

import com.msa.common.entity.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "dresses")
class Dress(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(length = 100)
    var name: String? = null,

    @Column(length = 50)
    var color: String? = null,

    @Column(length = 50)
    var shape: String? = null,

    @Column(name = "price_range", length = 50)
    var priceRange: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    var length: DressLength? = null,

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    var season: DressSeason? = null,

    @Column(length = 50)
    var designer: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    var dressType: DressType? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "neck_line", length = 30)
    var neckLine: DressNeckline? = null,

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    var mood: DressMood? = null,

    @Column(length = 200)
    var fabric: String? = null,

    @Column(name = "image_url", length = 500)
    var imageUrl: String? = null,

    @Column(columnDefinition = "TEXT")
    var features: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dress_shop_id", nullable = false)
    var dressShop: DressShop
) : BaseTimeEntity() {
    val shopName: String
        get() = dressShop.shopName
}

enum class DressType { WEDDING_DRESS, HANBOK, TUXEDO, SUIT, BOTH }
enum class DressLength { MINI, MIDI, LONG, TRAIN }
enum class DressSeason { SPRING, SUMMER, FALL, WINTER, ALL }
enum class DressNeckline { V_NECK, ROUND, OFF_SHOULDER, HALTER, SWEETHEART, OTHER }
enum class DressMood { ROMANTIC, ELEGANT, MODERN, CLASSIC, VINTAGE, BOHEMIAN }
