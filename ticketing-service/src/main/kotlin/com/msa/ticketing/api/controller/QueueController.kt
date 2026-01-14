package com.msa.ticketing.api.controller

import com.msa.common.response.ApiResponse
import com.msa.ticketing.application.QueueService
import com.msa.ticketing.application.QueueTokenResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "Queue", description = "대기열 API")
@RestController
@RequestMapping("/api/v1/queue")
class QueueController(
    private val queueService: QueueService
) {

    @Operation(summary = "대기열 진입")
    @PostMapping("/enter")
    fun enterQueue(
        @AuthenticationPrincipal(expression = "userId") userId: Long,
        @RequestParam concertId: Long
    ): ApiResponse<QueueTokenResponse> {
        return ApiResponse.success(queueService.enterQueue(userId, concertId))
    }

    @Operation(summary = "대기열 상태 조회")
    @GetMapping("/status")
    fun getQueueStatus(
        @RequestParam token: String
    ): ApiResponse<QueueTokenResponse> {
        return ApiResponse.success(queueService.getQueueStatus(token))
    }
}
