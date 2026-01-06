package com.msa.bookmark.domain.entity

import com.msa.common.entity.BaseTimeEntity
import com.msa.common.enums.ServiceType
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "bookmarks",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "service_type", "target_type", "target_id"])
    ],
    indexes = [
        Index(name = "idx_bookmark_user", columnList = "user_id"),
        Index(name = "idx_bookmark_target", columnList = "service_type, target_type, target_id")
    ]
)
class Bookmark(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 30)
    val serviceType: ServiceType,

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 30)
    val targetType: TargetType,

    @Column(name = "target_id", nullable = false)
    val targetId: Long,

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null

) : BaseTimeEntity() {

    fun isDeleted(): Boolean = deletedAt != null

    fun delete() {
        deletedAt = LocalDateTime.now()
    }

    fun restore() {
        deletedAt = null
    }
}

enum class TargetType {
    // Book Service
    BOOK, AUTHOR,
    // Ticketing Service
    CONCERT, ARTIST,
    // Travel Service
    ITINERARY, ATTRACTION,
    // Festival Service
    FESTIVAL,
    // Wedding Service
    HALL, WEDDING_HALL, DRESS, DRESS_SHOP, MAKEUP_SHOP;

    companion object {
        fun fromCategory(category: String): TargetType {
            return when (category.lowercase()) {
                "hall" -> HALL
                "wedding_hall" -> WEDDING_HALL
                "dress" -> DRESS
                "dress_shop" -> DRESS_SHOP
                "makeup_shop" -> MAKEUP_SHOP
                else -> valueOf(category.uppercase())
            }
        }
    }
}
