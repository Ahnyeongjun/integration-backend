package com.msa.wedding.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.msa.common.exception.NotFoundException
import com.msa.wedding.domain.entity.DressRecommendation
import com.msa.wedding.domain.repository.DressRecommendationRepository
import com.msa.wedding.domain.repository.DressRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DressRecommendationService(
    private val dressRecommendationRepository: DressRecommendationRepository,
    private val dressRepository: DressRepository,
    private val objectMapper: ObjectMapper
) {
    /**
     * 체형 기반 드레스 추천 조회
     * 캐시된 추천이 있으면 반환, 없으면 새로 생성
     */
    @Transactional
    fun getRecommendation(request: DressRecommendationRequest): DressRecommendationResponse {
        val queryHash = DressRecommendation.generateHash(
            request.armLength,
            request.legLength,
            request.neckLength,
            request.faceShape,
            request.bodyType
        )

        // 기존 추천이 있는지 확인
        val existing = dressRecommendationRepository.findByQueryHash(queryHash)
        if (existing.isPresent) {
            val recommendation = existing.get()
            recommendation.incrementAccess()
            dressRecommendationRepository.save(recommendation)
            return toResponse(recommendation)
        }

        // 새 추천 생성
        val recommendation = generateRecommendation(request, queryHash)
        val saved = dressRecommendationRepository.save(recommendation)
        return toResponse(saved)
    }

    /**
     * ID로 추천 조회
     */
    fun getRecommendationById(id: Long): DressRecommendationResponse {
        val recommendation = dressRecommendationRepository.findById(id)
            .orElseThrow { NotFoundException("DressRecommendation", id) }
        return toResponse(recommendation)
    }

    /**
     * 인기 추천 목록 조회
     */
    fun getPopularRecommendations(limit: Int = 10): List<DressRecommendationResponse> {
        return dressRecommendationRepository.findTopByAccessCount()
            .take(limit)
            .map { toResponse(it) }
    }

    /**
     * 체형 기반 드레스 추천 로직
     */
    private fun generateRecommendation(request: DressRecommendationRequest, queryHash: String): DressRecommendation {
        val recommendedDresses = mutableListOf<RecommendedDressInfo>()

        // 체형에 따른 추천 드레스 타입과 실루엣 결정
        val recommendedStyles = getRecommendedStyles(request)

        // 실제 드레스 검색 (간단한 로직, 실제로는 더 복잡한 추천 알고리즘 사용)
        val allDresses = dressRepository.findAll()
        for (dress in allDresses) {
            var score = 0.0

            // 실루엣 매칭
            if (dress.shape in recommendedStyles.shapes) score += 30.0

            // 네크라인 매칭
            if (dress.neckLine?.name in recommendedStyles.necklines) score += 25.0

            // 무드 매칭
            if (dress.mood?.name in recommendedStyles.moods) score += 20.0

            // 길이 매칭
            if (dress.length?.name in recommendedStyles.lengths) score += 15.0

            if (score > 0) {
                recommendedDresses.add(
                    RecommendedDressInfo(
                        dressId = dress.id,
                        name = dress.name ?: "Unknown",
                        shopName = dress.shopName,
                        imageUrl = dress.imageUrl,
                        matchScore = score,
                        reasons = generateReasons(dress, recommendedStyles)
                    )
                )
            }
        }

        // 점수순 정렬 후 상위 10개
        val topRecommendations = recommendedDresses
            .sortedByDescending { it.matchScore }
            .take(10)

        val recommendationJson = objectMapper.writeValueAsString(
            RecommendationResult(
                bodyAnalysis = BodyAnalysis(
                    armLength = request.armLength,
                    legLength = request.legLength,
                    neckLength = request.neckLength,
                    faceShape = request.faceShape,
                    bodyType = request.bodyType
                ),
                recommendations = topRecommendations,
                stylingTips = getStylingTips(request)
            )
        )

        return DressRecommendation(
            queryHash = queryHash,
            armLength = request.armLength,
            legLength = request.legLength,
            neckLength = request.neckLength,
            faceShape = request.faceShape,
            bodyType = request.bodyType,
            recommendation = recommendationJson
        )
    }

    private fun getRecommendedStyles(request: DressRecommendationRequest): RecommendedStyles {
        val shapes = mutableListOf<String>()
        val necklines = mutableListOf<String>()
        val moods = mutableListOf<String>()
        val lengths = mutableListOf<String>("LONG", "TRAIN")

        // 팔 길이에 따른 추천
        when (request.armLength.uppercase()) {
            "SHORT" -> {
                necklines.addAll(listOf("OFF_SHOULDER", "HALTER"))
            }
            "LONG" -> {
                necklines.addAll(listOf("V_NECK", "SWEETHEART"))
            }
            else -> {
                necklines.addAll(listOf("V_NECK", "OFF_SHOULDER", "ROUND"))
            }
        }

        // 다리 길이에 따른 추천
        when (request.legLength.uppercase()) {
            "SHORT" -> {
                shapes.addAll(listOf("A_LINE", "EMPIRE"))
            }
            "LONG" -> {
                shapes.addAll(listOf("MERMAID", "SHEATH"))
            }
            else -> {
                shapes.addAll(listOf("A_LINE", "BALL_GOWN"))
            }
        }

        // 얼굴형에 따른 추천
        when (request.faceShape.uppercase()) {
            "ROUND" -> {
                necklines.add("V_NECK")
                moods.addAll(listOf("ELEGANT", "MODERN"))
            }
            "OVAL" -> {
                moods.addAll(listOf("ROMANTIC", "CLASSIC", "ELEGANT"))
            }
            "SQUARE" -> {
                necklines.add("SWEETHEART")
                moods.addAll(listOf("ROMANTIC", "BOHEMIAN"))
            }
            "HEART" -> {
                necklines.add("HALTER")
                moods.addAll(listOf("ROMANTIC", "VINTAGE"))
            }
            else -> {
                moods.addAll(listOf("ROMANTIC", "ELEGANT"))
            }
        }

        // 체형에 따른 추가 추천
        when (request.bodyType?.uppercase()) {
            "SLIM" -> {
                shapes.add("MERMAID")
                shapes.add("SHEATH")
            }
            "CURVY", "PLUS_SIZE" -> {
                shapes.add("A_LINE")
                shapes.add("BALL_GOWN")
            }
        }

        return RecommendedStyles(
            shapes = shapes.distinct(),
            necklines = necklines.distinct(),
            moods = moods.distinct(),
            lengths = lengths
        )
    }

    private fun generateReasons(dress: com.msa.wedding.domain.entity.Dress, styles: RecommendedStyles): List<String> {
        val reasons = mutableListOf<String>()

        if (dress.shape in styles.shapes) {
            reasons.add("체형에 어울리는 실루엣")
        }
        if (dress.neckLine?.name in styles.necklines) {
            reasons.add("목선을 강조하는 네크라인")
        }
        if (dress.mood?.name in styles.moods) {
            reasons.add("추천 무드와 일치")
        }

        return reasons
    }

    private fun getStylingTips(request: DressRecommendationRequest): List<String> {
        val tips = mutableListOf<String>()

        when (request.armLength.uppercase()) {
            "SHORT" -> tips.add("오프숄더나 홀터넥 스타일로 시선을 어깨로 유도하세요")
            "LONG" -> tips.add("긴소매나 캡소매로 팔의 길이를 활용하세요")
        }

        when (request.legLength.uppercase()) {
            "SHORT" -> tips.add("하이웨이스트 라인으로 다리가 길어보이는 효과를 줄 수 있어요")
            "LONG" -> tips.add("머메이드 라인으로 긴 다리를 강조해보세요")
        }

        when (request.faceShape.uppercase()) {
            "ROUND" -> tips.add("V넥 라인으로 얼굴이 갸름해 보이는 효과를 줄 수 있어요")
            "SQUARE" -> tips.add("스위트하트 네크라인으로 각진 턱선을 부드럽게 연출하세요")
        }

        return tips
    }

    private fun toResponse(recommendation: DressRecommendation): DressRecommendationResponse {
        val result = objectMapper.readValue(recommendation.recommendation, RecommendationResult::class.java)
        return DressRecommendationResponse(
            id = recommendation.id,
            queryHash = recommendation.queryHash,
            bodyAnalysis = result.bodyAnalysis,
            recommendations = result.recommendations,
            stylingTips = result.stylingTips,
            accessCount = recommendation.accessCount,
            lastAccessed = recommendation.lastAccessed?.toString()
        )
    }
}

// Request/Response DTOs
data class DressRecommendationRequest(
    val armLength: String,
    val legLength: String,
    val neckLength: String,
    val faceShape: String,
    val bodyType: String? = null
)

data class DressRecommendationResponse(
    val id: Long,
    val queryHash: String,
    val bodyAnalysis: BodyAnalysis,
    val recommendations: List<RecommendedDressInfo>,
    val stylingTips: List<String>,
    val accessCount: Int,
    val lastAccessed: String?
)

data class BodyAnalysis(
    val armLength: String,
    val legLength: String,
    val neckLength: String,
    val faceShape: String,
    val bodyType: String?
)

data class RecommendedDressInfo(
    val dressId: Long,
    val name: String,
    val shopName: String,
    val imageUrl: String?,
    val matchScore: Double,
    val reasons: List<String>
)

data class RecommendationResult(
    val bodyAnalysis: BodyAnalysis,
    val recommendations: List<RecommendedDressInfo>,
    val stylingTips: List<String>
)

data class RecommendedStyles(
    val shapes: List<String>,
    val necklines: List<String>,
    val moods: List<String>,
    val lengths: List<String>
)
