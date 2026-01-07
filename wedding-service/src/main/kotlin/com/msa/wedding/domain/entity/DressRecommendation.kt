package com.msa.wedding.domain.entity

import com.msa.common.entity.BaseTimeEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "dress_recommendations",
    indexes = [
        Index(name = "idx_dress_rec_hash", columnList = "query_hash", unique = true),
        Index(name = "idx_dress_rec_params", columnList = "arm_length, leg_length, neck_length, face_shape")
    ]
)
class DressRecommendation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "query_hash", nullable = false, length = 64, unique = true)
    val queryHash: String,

    @Column(name = "arm_length", nullable = false, length = 20)
    val armLength: String,

    @Column(name = "leg_length", nullable = false, length = 20)
    val legLength: String,

    @Column(name = "neck_length", nullable = false, length = 20)
    val neckLength: String,

    @Column(name = "face_shape", nullable = false, length = 20)
    val faceShape: String,

    @Column(name = "body_type", length = 100)
    val bodyType: String? = null,

    @Column(name = "recommendation", columnDefinition = "JSON", nullable = false)
    val recommendation: String,

    @Column(name = "access_count")
    var accessCount: Int = 0,

    @Column(name = "last_accessed")
    var lastAccessed: LocalDateTime? = null
) : BaseTimeEntity() {

    fun incrementAccess() {
        accessCount++
        lastAccessed = LocalDateTime.now()
    }

    companion object {
        fun generateHash(armLength: String, legLength: String, neckLength: String, faceShape: String, bodyType: String?): String {
            val combined = "$armLength|$legLength|$neckLength|$faceShape|${bodyType ?: ""}"
            return combined.hashCode().toString(16).padStart(16, '0')
        }
    }
}

enum class ArmLength { SHORT, MEDIUM, LONG }
enum class LegLength { SHORT, MEDIUM, LONG }
enum class NeckLength { SHORT, MEDIUM, LONG }
enum class FaceShape { OVAL, ROUND, SQUARE, HEART, LONG, DIAMOND }
enum class BodyType { SLIM, STANDARD, CURVY, PLUS_SIZE }
