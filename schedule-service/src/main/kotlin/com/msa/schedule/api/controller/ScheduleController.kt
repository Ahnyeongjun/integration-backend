package com.msa.schedule.api.controller

import com.msa.common.enums.ServiceType
import com.msa.common.response.ApiResponse
import com.msa.schedule.application.*
import com.msa.schedule.domain.entity.Schedule
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@Tag(name = "Schedule", description = "일정 API")
@RestController
@RequestMapping("/api/v1/schedules")
class ScheduleController(
    private val scheduleService: ScheduleService
) {

    @Operation(summary = "일정 생성")
    @PostMapping
    fun createSchedule(
        @AuthenticationPrincipal(expression = "userId") userId: Long,
        @RequestBody request: ScheduleCreateRequest
    ): ApiResponse<ScheduleResponse> {
        val schedule = scheduleService.createSchedule(userId, request)
        return ApiResponse.success(ScheduleResponse.from(schedule))
    }

    @Operation(summary = "월간 일정 조회")
    @GetMapping("/month")
    fun getMonthlySchedules(
        @AuthenticationPrincipal(expression = "userId") userId: Long,
        @RequestParam year: Int,
        @RequestParam month: Int,
        @RequestParam(required = false) serviceType: ServiceType?
    ): ApiResponse<List<ScheduleResponse>> {
        val schedules = scheduleService.getMonthlySchedules(userId, year, month, serviceType)
        return ApiResponse.success(schedules.map { ScheduleResponse.from(it) })
    }

    @Operation(summary = "주간 일정 조회")
    @GetMapping("/week")
    fun getWeeklySchedules(
        @AuthenticationPrincipal(expression = "userId") userId: Long,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam(required = false) serviceType: ServiceType?
    ): ApiResponse<List<ScheduleResponse>> {
        val schedules = scheduleService.getWeeklySchedules(userId, startDate, serviceType)
        return ApiResponse.success(schedules.map { ScheduleResponse.from(it) })
    }

    @Operation(summary = "일간 일정 조회")
    @GetMapping("/day")
    fun getDailySchedules(
        @AuthenticationPrincipal(expression = "userId") userId: Long,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
        @RequestParam(required = false) serviceType: ServiceType?
    ): ApiResponse<List<ScheduleResponse>> {
        val schedules = scheduleService.getDailySchedules(userId, date, serviceType)
        return ApiResponse.success(schedules.map { ScheduleResponse.from(it) })
    }

    @Operation(summary = "일정 상세")
    @GetMapping("/{id}")
    fun getSchedule(
        @AuthenticationPrincipal(expression = "userId") userId: Long,
        @PathVariable id: Long
    ): ApiResponse<ScheduleResponse> {
        val schedule = scheduleService.getSchedule(userId, id)
        return ApiResponse.success(ScheduleResponse.from(schedule))
    }

    @Operation(summary = "다가오는 일정 알림")
    @GetMapping("/notifications")
    fun getNotifications(
        @AuthenticationPrincipal(expression = "userId") userId: Long
    ): ApiResponse<List<ScheduleResponse>> {
        val schedules = scheduleService.getUpcomingSchedules(userId)
        return ApiResponse.success(schedules.map { ScheduleResponse.from(it) })
    }

    @Operation(summary = "일정 수정 (PUT)")
    @PutMapping("/{id}")
    fun updateSchedulePut(
        @AuthenticationPrincipal(expression = "userId") userId: Long,
        @PathVariable id: Long,
        @RequestBody request: ScheduleUpdateRequest
    ): ApiResponse<ScheduleResponse> {
        val schedule = scheduleService.updateSchedule(userId, id, request)
        return ApiResponse.success(ScheduleResponse.from(schedule))
    }

    @Operation(summary = "일정 수정 (PATCH)")
    @PatchMapping("/{id}")
    fun updateSchedulePatch(
        @AuthenticationPrincipal(expression = "userId") userId: Long,
        @PathVariable id: Long,
        @RequestBody request: ScheduleUpdateRequest
    ): ApiResponse<ScheduleResponse> {
        val schedule = scheduleService.updateSchedule(userId, id, request)
        return ApiResponse.success(ScheduleResponse.from(schedule))
    }

    @Operation(summary = "일정 삭제")
    @DeleteMapping("/{id}")
    fun deleteSchedule(
        @AuthenticationPrincipal(expression = "userId") userId: Long,
        @PathVariable id: Long
    ): ApiResponse<Unit> {
        scheduleService.deleteSchedule(userId, id)
        return ApiResponse.success()
    }

    // Internal API
    @PostMapping("/internal")
    fun createFromService(@RequestBody request: ServiceScheduleRequest): ApiResponse<ScheduleResponse> {
        val schedule = scheduleService.createFromService(request)
        return ApiResponse.success(ScheduleResponse.from(schedule))
    }
}

data class ScheduleResponse(
    val id: Long,
    val serviceType: com.msa.common.enums.ServiceType,
    val refId: Long?,
    val title: String,
    val description: String?,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val startTime: java.time.LocalTime?,
    val endTime: java.time.LocalTime?,
    val allDay: Boolean,
    val color: String?
) {
    companion object {
        fun from(schedule: Schedule) = ScheduleResponse(
            id = schedule.id,
            serviceType = schedule.serviceType,
            refId = schedule.refId,
            title = schedule.title,
            description = schedule.description,
            startDate = schedule.startDate,
            endDate = schedule.endDate,
            startTime = schedule.startTime,
            endTime = schedule.endTime,
            allDay = schedule.allDay,
            color = schedule.color
        )
    }
}
