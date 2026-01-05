package com.msa.schedule.application

import com.msa.common.enums.ServiceType
import com.msa.common.exception.ForbiddenException
import com.msa.common.exception.NotFoundException
import com.msa.schedule.domain.entity.Schedule
import com.msa.schedule.domain.repository.ScheduleRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.YearMonth

@Service
@Transactional
class ScheduleService(
    private val scheduleRepository: ScheduleRepository
) {

    fun createSchedule(userId: Long, request: ScheduleCreateRequest): Schedule {
        val schedule = Schedule(
            userId = userId,
            serviceType = request.serviceType,
            refId = request.refId,
            title = request.title,
            description = request.description,
            startDate = request.startDate,
            endDate = request.endDate,
            startTime = request.startTime,
            endTime = request.endTime,
            allDay = request.allDay,
            color = request.color
        )
        return scheduleRepository.save(schedule)
    }

    @Transactional(readOnly = true)
    fun getMonthlySchedules(userId: Long, year: Int, month: Int): List<Schedule> {
        val yearMonth = YearMonth.of(year, month)
        val startDate = yearMonth.atDay(1)
        val endDate = yearMonth.atEndOfMonth()
        return scheduleRepository.findByUserIdAndDateRange(userId, startDate, endDate)
    }

    @Transactional(readOnly = true)
    fun getWeeklySchedules(userId: Long, startDate: LocalDate): List<Schedule> {
        val endDate = startDate.plusDays(6)
        return scheduleRepository.findByUserIdAndDateRange(userId, startDate, endDate)
    }

    @Transactional(readOnly = true)
    fun getDailySchedules(userId: Long, date: LocalDate): List<Schedule> {
        return scheduleRepository.findByUserIdAndStartDate(userId, date)
    }

    @Transactional(readOnly = true)
    fun getSchedule(userId: Long, scheduleId: Long): Schedule {
        val schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow { NotFoundException("Schedule", scheduleId) }

        if (schedule.userId != userId) {
            throw ForbiddenException("Cannot access this schedule")
        }
        return schedule
    }

    @Transactional(readOnly = true)
    fun getUpcomingSchedules(userId: Long): List<Schedule> {
        val today = LocalDate.now()
        val nextWeek = today.plusDays(7)
        return scheduleRepository.findByUserIdAndDateRange(userId, today, nextWeek)
    }

    fun updateSchedule(userId: Long, scheduleId: Long, request: ScheduleUpdateRequest): Schedule {
        val schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow { NotFoundException("Schedule", scheduleId) }

        if (schedule.userId != userId) {
            throw ForbiddenException("Cannot update this schedule")
        }

        request.title?.let { schedule.title = it }
        request.description?.let { schedule.description = it }
        request.startDate?.let { schedule.startDate = it }
        request.endDate?.let { schedule.endDate = it }
        request.startTime?.let { schedule.startTime = it }
        request.endTime?.let { schedule.endTime = it }
        request.allDay?.let { schedule.allDay = it }
        request.color?.let { schedule.color = it }

        return scheduleRepository.save(schedule)
    }

    fun deleteSchedule(userId: Long, scheduleId: Long) {
        val schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow { NotFoundException("Schedule", scheduleId) }

        if (schedule.userId != userId) {
            throw ForbiddenException("Cannot delete this schedule")
        }

        scheduleRepository.delete(schedule)
    }

    // Internal API for other services
    fun createFromService(request: ServiceScheduleRequest): Schedule {
        return scheduleRepository.save(
            Schedule(
                userId = request.userId,
                serviceType = request.serviceType,
                refId = request.refId,
                title = request.title,
                startDate = request.startDate,
                endDate = request.endDate,
                allDay = request.allDay
            )
        )
    }

    fun deleteByServiceRef(serviceType: ServiceType, refId: Long) {
        scheduleRepository.deleteByServiceTypeAndRefId(serviceType, refId)
    }
}

data class ScheduleCreateRequest(
    val serviceType: ServiceType,
    val refId: Long? = null,
    val title: String,
    val description: String? = null,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val startTime: java.time.LocalTime? = null,
    val endTime: java.time.LocalTime? = null,
    val allDay: Boolean = false,
    val color: String? = null
)

data class ScheduleUpdateRequest(
    val title: String? = null,
    val description: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val startTime: java.time.LocalTime? = null,
    val endTime: java.time.LocalTime? = null,
    val allDay: Boolean? = null,
    val color: String? = null
)

data class ServiceScheduleRequest(
    val userId: Long,
    val serviceType: ServiceType,
    val refId: Long,
    val title: String,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val allDay: Boolean = false
)
