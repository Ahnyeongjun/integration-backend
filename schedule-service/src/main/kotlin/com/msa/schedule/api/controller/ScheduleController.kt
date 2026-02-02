package com.msa.schedule.api.controller

import com.msa.common.enums.ServiceType
import com.msa.common.response.ApiResponse
import com.msa.schedule.application.*
import com.msa.schedule.domain.entity.Schedule
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import com.msa.common.security.UserPrincipal
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
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: ScheduleCreateRequest
    ): ApiResponse<ScheduleResponse> {
        val schedule = scheduleService.createSchedule(principal.userId, request)
        return ApiResponse.success(ScheduleResponse.from(schedule))
    }

    @Operation(summary = "월간 일정 조회")
    @GetMapping("/month")
    fun getMonthlySchedules(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestParam year: Int,
        @RequestParam month: Int,
        @RequestParam(required = false) serviceType: ServiceType?
    ): ApiResponse<List<ScheduleResponse>> {
        require(month in 1..12) { "month는 1~12 사이여야 합니다" }
        require(year in 1900..2100) { "year는 1900~2100 사이여야 합니다" }
        val schedules = scheduleService.getMonthlySchedules(principal.userId, year, month, serviceType)
        return ApiResponse.success(schedules.map { ScheduleResponse.from(it) })
    }

    @Operation(summary = "주간 일정 조회")
    @GetMapping("/week")
    fun getWeeklySchedules(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam(required = false) serviceType: ServiceType?
    ): ApiResponse<List<ScheduleResponse>> {
        val schedules = scheduleService.getWeeklySchedules(principal.userId, startDate, serviceType)
        return ApiResponse.success(schedules.map { ScheduleResponse.from(it) })
    }

    @Operation(summary = "일간 일정 조회")
    @GetMapping("/day")
    fun getDailySchedules(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
        @RequestParam(required = false) serviceType: ServiceType?
    ): ApiResponse<List<ScheduleResponse>> {
        val schedules = scheduleService.getDailySchedules(principal.userId, date, serviceType)
        return ApiResponse.success(schedules.map { ScheduleResponse.from(it) })
    }

    @Operation(summary = "일정 상세")
    @GetMapping("/{id}")
    fun getSchedule(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: Long
    ): ApiResponse<ScheduleResponse> {
        require(id > 0) { "id는 양수여야 합니다" }
        val schedule = scheduleService.getSchedule(principal.userId, id)
        return ApiResponse.success(ScheduleResponse.from(schedule))
    }

    @Operation(summary = "다가오는 일정 알림")
    @GetMapping("/notifications")
    fun getNotifications(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ApiResponse<List<ScheduleResponse>> {
        val schedules = scheduleService.getUpcomingSchedules(principal.userId)
        return ApiResponse.success(schedules.map { ScheduleResponse.from(it) })
    }

    @Operation(summary = "일정 수정")
    @PatchMapping("/{id}")
    fun updateSchedule(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: Long,
        @RequestBody request: ScheduleUpdateRequest
    ): ApiResponse<ScheduleResponse> {
        require(id > 0) { "id는 양수여야 합니다" }
        val schedule = scheduleService.updateSchedule(principal.userId, id, request)
        return ApiResponse.success(ScheduleResponse.from(schedule))
    }

    @Operation(summary = "일정 삭제")
    @DeleteMapping("/{id}")
    fun deleteSchedule(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: Long
    ): ApiResponse<Unit> {
        require(id > 0) { "id는 양수여야 합니다" }
        scheduleService.deleteSchedule(principal.userId, id)
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
