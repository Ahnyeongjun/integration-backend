package com.msa.bookmark.domain.repository

import com.msa.bookmark.domain.entity.Bookmark
import com.msa.bookmark.domain.entity.TargetType
import com.msa.common.enums.ServiceType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface BookmarkRepository : JpaRepository<Bookmark, Long> {

    @Query("SELECT b FROM Bookmark b WHERE b.userId = :userId AND b.deletedAt IS NULL")
    fun findByUserId(userId: Long, pageable: Pageable): Page<Bookmark>

    @Query("SELECT b FROM Bookmark b WHERE b.userId = :userId AND b.serviceType = :serviceType AND b.deletedAt IS NULL")
    fun findByUserIdAndServiceType(userId: Long, serviceType: ServiceType, pageable: Pageable): Page<Bookmark>

    @Query("SELECT b FROM Bookmark b WHERE b.userId = :userId AND b.serviceType = :serviceType AND b.targetType = :targetType AND b.targetId = :targetId")
    fun findByUserAndTarget(userId: Long, serviceType: ServiceType, targetType: TargetType, targetId: Long): Bookmark?

    @Query("SELECT COUNT(b) FROM Bookmark b WHERE b.serviceType = :serviceType AND b.targetType = :targetType AND b.targetId = :targetId AND b.deletedAt IS NULL")
    fun countByTarget(serviceType: ServiceType, targetType: TargetType, targetId: Long): Long

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Bookmark b WHERE b.userId = :userId AND b.serviceType = :serviceType AND b.targetType = :targetType AND b.targetId = :targetId AND b.deletedAt IS NULL")
    fun existsByUserAndTarget(userId: Long, serviceType: ServiceType, targetType: TargetType, targetId: Long): Boolean
}
