package com.msa.schedule.domain.repository

import com.msa.common.enums.ServiceType
import com.msa.schedule.domain.entity.Schedule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface ScheduleRepository : JpaRepository<Schedule, Long> {

    @Query("""
        SELECT s FROM Schedule s
        WHERE s.userId = :userId
        AND ((s.startDate <= :endDate AND s.endDate >= :startDate)
             OR (s.startDate >= :startDate AND s.startDate <= :endDate))
        ORDER BY s.startDate, s.startTime
    """)
    fun findByUserIdAndDateRange(userId: Long, startDate: LocalDate, endDate: LocalDate): List<Schedule>

    @Query("""
        SELECT s FROM Schedule s
        WHERE s.userId = :userId
        AND s.serviceType = :serviceType
        AND ((s.startDate <= :endDate AND s.endDate >= :startDate)
             OR (s.startDate >= :startDate AND s.startDate <= :endDate))
        ORDER BY s.startDate, s.startTime
    """)
    fun findByUserIdAndServiceTypeAndDateRange(
        userId: Long,
        serviceType: ServiceType,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Schedule>

    fun findByUserIdAndStartDate(userId: Long, startDate: LocalDate): List<Schedule>

    fun findByUserIdAndServiceTypeAndStartDate(
        userId: Long,
        serviceType: ServiceType,
        startDate: LocalDate
    ): List<Schedule>

    fun findByServiceTypeAndRefId(serviceType: ServiceType, refId: Long): Schedule?

    fun deleteByServiceTypeAndRefId(serviceType: ServiceType, refId: Long)
}
