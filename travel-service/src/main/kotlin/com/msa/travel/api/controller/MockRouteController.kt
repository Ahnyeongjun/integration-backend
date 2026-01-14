package com.msa.travel.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import kotlin.math.*

/**
 * Mock Route Controller
 * 지도 API 미연동으로 인한 Mock 소요시간 계산
 */
@Tag(name = "Mock Route", description = "경로 소요시간 Mock API")
@RestController
@RequestMapping("/route")
class MockRouteController {

    @Operation(summary = "두 지점 간 소요시간 조회", description = "도보/차량 소요시간 Mock 계산")
    @GetMapping("/time")
    fun getRouteTime(
        @RequestParam startLatitude: Double,
        @RequestParam startLongitude: Double,
        @RequestParam endLatitude: Double,
        @RequestParam endLongitude: Double
    ): ResponseEntity<RouteTimeResponse> {
        // Haversine 공식으로 거리 계산 (km)
        val distance = calculateDistance(startLatitude, startLongitude, endLatitude, endLongitude)

        // 도보 속도: 약 4km/h → 분 단위 계산
        val walkingDuration = ((distance / 4.0) * 60).roundToInt()

        // 차량 속도: 약 40km/h (도심 평균) → 분 단위 계산
        val drivingDuration = ((distance / 40.0) * 60).roundToInt()

        // 거리 포맷
        val distanceStr = if (distance < 1) {
            "${(distance * 1000).roundToInt()}m"
        } else {
            String.format("%.1fkm", distance)
        }

        return ResponseEntity.ok(
            RouteTimeResponse(
                walkingDuration = maxOf(1, walkingDuration),
                drivingDuration = maxOf(1, drivingDuration),
                distance = distanceStr
            )
        )
    }

    /**
     * Haversine 공식을 사용한 두 좌표 간 거리 계산 (km)
     */
    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadius = 6371.0 // km

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }
}

data class RouteTimeResponse(
    val walkingDuration: Int,  // 도보 소요시간 (분)
    val drivingDuration: Int,  // 차량 소요시간 (분)
    val distance: String       // 거리 (예: "1.5km", "800m")
)
