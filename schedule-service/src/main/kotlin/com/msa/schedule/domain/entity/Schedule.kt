package com.msa.schedule.domain.entity

import com.msa.common.entity.BaseTimeEntity
import com.msa.common.enums.ServiceType
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalTime

@Entity
@Table(
    name = "schedules",
    indexes = [
        Index(name = "idx_schedule_user_date", columnList = "user_id, start_date")
    ]
)
class Schedule(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 30)
    val serviceType: ServiceType,

    @Column(name = "ref_id")
    val refId: Long? = null,  // 원본 서비스의 ID (여행 일정 ID, 콘서트 ID 등)

    @Column(nullable = false, length = 200)
    var title: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "start_date", nullable = false)
    var startDate: LocalDate,

    @Column(name = "end_date")
    var endDate: LocalDate? = null,

    @Column(name = "start_time")
    var startTime: LocalTime? = null,

    @Column(name = "end_time")
    var endTime: LocalTime? = null,

    @Column(name = "all_day")
    var allDay: Boolean = false,

    @Column(length = 20)
    var color: String? = null

) : BaseTimeEntity()
