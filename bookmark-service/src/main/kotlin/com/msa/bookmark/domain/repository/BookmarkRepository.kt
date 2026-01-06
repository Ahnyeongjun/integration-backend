package com.msa.bookmark.domain.repository

import com.msa.bookmark.domain.entity.Bookmark
import com.msa.bookmark.domain.entity.TargetType
import com.msa.common.enums.ServiceType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface BookmarkRepository : JpaRepository<Bookmark, Long> {

    @Query("SELECT b FROM Bookmark b WHERE b.userId = :userId AND b.deletedAt IS NULL ORDER BY b.createdAt DESC")
    fun findAllByUserId(userId: Long): List<Bookmark>

    @Query("SELECT b FROM Bookmark b WHERE b.userId = :userId AND b.deletedAt IS NULL")
    fun findByUserId(userId: Long, pageable: Pageable): Page<Bookmark>

    @Query("SELECT b FROM Bookmark b WHERE b.userId = :userId AND b.serviceType = :serviceType AND b.deletedAt IS NULL")
    fun findByUserIdAndServiceType(userId: Long, serviceType: ServiceType, pageable: Pageable): Page<Bookmark>

    @Query("SELECT b FROM Bookmark b WHERE b.userId = :userId AND b.targetType = :targetType AND b.deletedAt IS NULL ORDER BY b.createdAt DESC")
    fun findByUserIdAndTargetType(userId: Long, targetType: TargetType): List<Bookmark>

    @Query("SELECT b FROM Bookmark b WHERE b.userId = :userId AND b.serviceType = :serviceType AND b.targetType = :targetType AND b.targetId = :targetId")
    fun findByUserAndTarget(userId: Long, serviceType: ServiceType, targetType: TargetType, targetId: Long): Bookmark?

    @Query("SELECT COUNT(b) FROM Bookmark b WHERE b.serviceType = :serviceType AND b.targetType = :targetType AND b.targetId = :targetId AND b.deletedAt IS NULL")
    fun countByTarget(serviceType: ServiceType, targetType: TargetType, targetId: Long): Long

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Bookmark b WHERE b.userId = :userId AND b.serviceType = :serviceType AND b.targetType = :targetType AND b.targetId = :targetId AND b.deletedAt IS NULL")
    fun existsByUserAndTarget(userId: Long, serviceType: ServiceType, targetType: TargetType, targetId: Long): Boolean

    // Batch queries for internal API
    @Query("SELECT b.targetId FROM Bookmark b WHERE b.userId = :userId AND b.serviceType = :serviceType AND b.targetType = :targetType AND b.targetId IN :targetIds AND b.deletedAt IS NULL")
    fun findBookmarkedTargetIds(userId: Long, serviceType: ServiceType, targetType: TargetType, targetIds: List<Long>): List<Long>

    @Query("SELECT b.targetId, COUNT(b) FROM Bookmark b WHERE b.serviceType = :serviceType AND b.targetType = :targetType AND b.targetId IN :targetIds AND b.deletedAt IS NULL GROUP BY b.targetId")
    fun countByTargetIds(serviceType: ServiceType, targetType: TargetType, targetIds: List<Long>): List<Array<Any>>
}
