package com.msa.wedding.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.msa.common.exception.NotFoundException
import com.msa.wedding.domain.entity.VenueRecommendation
import com.msa.wedding.domain.repository.VenueRecommendationRepository
import com.msa.wedding.domain.repository.WeddingHallRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class VenueRecommendationService(
    private val venueRecommendationRepository: VenueRecommendationRepository,
    private val weddingHallRepository: WeddingHallRepository,
    private val objectMapper: ObjectMapper
) {
    /**
     * 조건 기반 웨딩홀 추천 조회
     */
    @Transactional
    fun getRecommendation(request: VenueRecommendationRequest): VenueRecommendationResponse {
        val queryHash = VenueRecommendation.generateHash(
            request.guestCount,
            request.budget,
            request.region,
            request.stylePreference,
            request.season
        )

        // 기존 추천이 있는지 확인
        val existing = venueRecommendationRepository.findByQueryHash(queryHash)
        if (existing.isPresent) {
            val recommendation = existing.get()
            recommendation.incrementAccess()
            venueRecommendationRepository.save(recommendation)
            return toResponse(recommendation)
        }

        // 새 추천 생성
        val recommendation = generateRecommendation(request, queryHash)
        val saved = venueRecommendationRepository.save(recommendation)
        return toResponse(saved)
    }

    /**
     * ID로 추천 조회
     */
    fun getRecommendationById(id: Long): VenueRecommendationResponse {
        val recommendation = venueRecommendationRepository.findById(id)
            .orElseThrow { NotFoundException("VenueRecommendation", id) }
        return toResponse(recommendation)
    }

    /**
     * 인기 추천 목록 조회
     */
    fun getPopularRecommendations(limit: Int = 10): List<VenueRecommendationResponse> {
        return venueRecommendationRepository.findTopByAccessCount()
            .take(limit)
            .map { toResponse(it) }
    }

    /**
     * 조건 기반 웨딩홀 추천 로직
     */
    private fun generateRecommendation(request: VenueRecommendationRequest, queryHash: String): VenueRecommendation {
        val recommendedVenues = mutableListOf<RecommendedVenueInfo>()

        val allVenues = weddingHallRepository.findAll()
        for (venue in allVenues) {
            var score = 0.0
            val reasons = mutableListOf<String>()

            // 지역 매칭
            if (matchesRegion(venue.address, request.region)) {
                score += 30.0
                reasons.add("희망 지역과 일치")
            }

            // 홀 타입 매칭 (스타일 선호도)
            if (matchesStyle(venue.hallType.name, request.stylePreference)) {
                score += 25.0
                reasons.add("선호하는 웨딩 스타일과 일치")
            }

            // 수용 인원 매칭
            if (matchesCapacity(venue.maxCapacity, request.guestCount)) {
                score += 20.0
                reasons.add("하객 수 수용 가능")
            }

            // 시즌 매칭 (야외/실내 추천)
            if (matchesSeason(venue.hallType.name, request.season)) {
                score += 15.0
                reasons.add("선택 시즌에 적합한 웨딩홀")
            }

            if (score > 0) {
                recommendedVenues.add(
                    RecommendedVenueInfo(
                        venueId = venue.id,
                        name = venue.name,
                        address = venue.address,
                        hallType = venue.hallType.name,
                        coverImage = venue.coverImage,
                        matchScore = score,
                        reasons = reasons,
                        parking = venue.parking,
                        avgRating = venue.avgRating
                    )
                )
            }
        }

        // 점수순 정렬 후 상위 10개
        val topRecommendations = recommendedVenues
            .sortedByDescending { it.matchScore }
            .take(10)

        val recommendationJson = objectMapper.writeValueAsString(
            VenueRecommendationResult(
                criteria = VenueCriteria(
                    guestCount = request.guestCount,
                    budget = request.budget,
                    region = request.region,
                    stylePreference = request.stylePreference,
                    season = request.season
                ),
                recommendations = topRecommendations,
                planningTips = getPlanningTips(request)
            )
        )

        return VenueRecommendation(
            queryHash = queryHash,
            guestCount = request.guestCount,
            budget = request.budget,
            region = request.region,
            stylePreference = request.stylePreference,
            season = request.season,
            recommendation = recommendationJson
        )
    }

    private fun matchesRegion(address: String, region: String): Boolean {
        val regionKeywords = when (region.uppercase()) {
            "SEOUL" -> listOf("서울")
            "GYEONGGI" -> listOf("경기", "성남", "수원", "고양", "용인", "분당")
            "INCHEON" -> listOf("인천")
            "BUSAN" -> listOf("부산")
            "DAEGU" -> listOf("대구")
            "DAEJEON" -> listOf("대전")
            "GWANGJU" -> listOf("광주")
            "JEJU" -> listOf("제주")
            else -> listOf()
        }
        return regionKeywords.any { address.contains(it) }
    }

    private fun matchesStyle(hallType: String, stylePreference: String): Boolean {
        val styleMap = mapOf(
            "LUXURY" to listOf("HOTEL"),
            "CLASSIC" to listOf("HOTEL", "CONVENTION"),
            "MODERN" to listOf("CONVENTION", "HOUSE"),
            "ROMANTIC" to listOf("GARDEN", "OUTDOOR", "HOUSE"),
            "NATURAL" to listOf("OUTDOOR", "GARDEN"),
            "MINIMAL" to listOf("HOUSE", "RESTAURANT")
        )
        return styleMap[stylePreference.uppercase()]?.contains(hallType) == true
    }

    private fun matchesCapacity(maxCapacity: Int?, guestCount: String): Boolean {
        if (maxCapacity == null) return false
        return when (guestCount.uppercase()) {
            "SMALL" -> maxCapacity >= 50
            "MEDIUM" -> maxCapacity >= 100
            "LARGE" -> maxCapacity >= 200
            "EXTRA_LARGE" -> maxCapacity >= 300
            else -> true
        }
    }

    private fun matchesSeason(hallType: String, season: String): Boolean {
        return when (season.uppercase()) {
            "SPRING", "FALL" -> true  // 모든 타입 적합
            "SUMMER" -> hallType !in listOf("OUTDOOR")  // 여름에는 실내 추천
            "WINTER" -> hallType in listOf("HOTEL", "CONVENTION", "HOUSE")  // 겨울에는 실내만
            else -> true
        }
    }

    private fun getPlanningTips(request: VenueRecommendationRequest): List<String> {
        val tips = mutableListOf<String>()

        when (request.season.uppercase()) {
            "SPRING" -> {
                tips.add("봄 웨딩 시즌은 인기가 많으니 최소 6개월 전 예약을 권장합니다")
                tips.add("야외 웨딩 시 꽃가루 알레르기 대비를 고려하세요")
            }
            "SUMMER" -> {
                tips.add("여름에는 냉방이 잘 되는 실내 홀을 추천합니다")
                tips.add("우천 시 대비 플랜B를 준비하세요")
            }
            "FALL" -> {
                tips.add("가을은 야외 웨딩의 최적 시즌입니다")
                tips.add("단풍 시즌에 맞춘 포토존 활용을 추천합니다")
            }
            "WINTER" -> {
                tips.add("겨울 웨딩은 실내 홀과 따뜻한 조명이 포인트입니다")
                tips.add("하객들의 이동 동선을 최소화할 수 있는 장소를 선택하세요")
            }
        }

        when (request.guestCount.uppercase()) {
            "SMALL" -> tips.add("소규모 웨딩은 하우스웨딩이나 레스토랑웨딩이 아늑합니다")
            "LARGE", "EXTRA_LARGE" -> tips.add("대규모 하객을 위한 충분한 주차 공간을 확인하세요")
        }

        return tips
    }

    private fun toResponse(recommendation: VenueRecommendation): VenueRecommendationResponse {
        val result = objectMapper.readValue(recommendation.recommendation, VenueRecommendationResult::class.java)
        return VenueRecommendationResponse(
            id = recommendation.id,
            queryHash = recommendation.queryHash,
            criteria = result.criteria,
            recommendations = result.recommendations,
            planningTips = result.planningTips,
            accessCount = recommendation.accessCount,
            lastAccessed = recommendation.lastAccessed?.toString()
        )
    }
}

// Request/Response DTOs
data class VenueRecommendationRequest(
    val guestCount: String,
    val budget: String,
    val region: String,
    val stylePreference: String,
    val season: String
)

data class VenueRecommendationResponse(
    val id: Long,
    val queryHash: String,
    val criteria: VenueCriteria,
    val recommendations: List<RecommendedVenueInfo>,
    val planningTips: List<String>,
    val accessCount: Int,
    val lastAccessed: String?
)

data class VenueCriteria(
    val guestCount: String,
    val budget: String,
    val region: String,
    val stylePreference: String,
    val season: String
)

data class RecommendedVenueInfo(
    val venueId: Long,
    val name: String,
    val address: String,
    val hallType: String,
    val coverImage: String?,
    val matchScore: Double,
    val reasons: List<String>,
    val parking: Int?,
    val avgRating: Double
)

data class VenueRecommendationResult(
    val criteria: VenueCriteria,
    val recommendations: List<RecommendedVenueInfo>,
    val planningTips: List<String>
)
