package com.msa.travel.mock

import com.msa.travel.api.dto.*

/**
 * Mock 여행 데이터
 * MCP 기능 미지원으로 인한 하드코딩 데이터
 */
object MockTravelData {

    /**
     * 추천 여행지 목록
     */
    val destinations = listOf(
        RecommendResponse(
            name = "제주도",
            theme = "자연과 힐링의 섬",
            address = "제주특별자치도 제주시",
            imageUrl = "https://images.unsplash.com/photo-1579169326371-b8148794e7e4?w=800",
            latitude = 33.4996,
            longitude = 126.5312
        ),
        RecommendResponse(
            name = "부산",
            theme = "바다와 도시의 조화",
            address = "부산광역시 해운대구",
            imageUrl = "https://images.unsplash.com/photo-1538485399081-7191377e8241?w=800",
            latitude = 35.1587,
            longitude = 129.1604
        ),
        RecommendResponse(
            name = "강릉",
            theme = "동해의 낭만",
            address = "강원특별자치도 강릉시",
            imageUrl = "https://images.unsplash.com/photo-1596433809252-c0bccce2f244?w=800",
            latitude = 37.7519,
            longitude = 128.8761
        ),
        RecommendResponse(
            name = "경주",
            theme = "천년 역사의 도시",
            address = "경상북도 경주시",
            imageUrl = "https://images.unsplash.com/photo-1592882595561-c4a94a1e3986?w=800",
            latitude = 35.8562,
            longitude = 129.2247
        ),
        RecommendResponse(
            name = "전주",
            theme = "한옥과 맛의 고장",
            address = "전북특별자치도 전주시",
            imageUrl = "https://images.unsplash.com/photo-1534274867514-d5b47ef89ed7?w=800",
            latitude = 35.8242,
            longitude = 127.1480
        ),
        RecommendResponse(
            name = "여수",
            theme = "밤바다의 보석",
            address = "전라남도 여수시",
            imageUrl = "https://images.unsplash.com/photo-1596178065887-1198b6148b2b?w=800",
            latitude = 34.7604,
            longitude = 127.6622
        ),
        RecommendResponse(
            name = "속초",
            theme = "설악산과 바다",
            address = "강원특별자치도 속초시",
            imageUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800",
            latitude = 38.2070,
            longitude = 128.5918
        ),
        RecommendResponse(
            name = "통영",
            theme = "한국의 나폴리",
            address = "경상남도 통영시",
            imageUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800",
            latitude = 34.8544,
            longitude = 128.4330
        )
    )

    /**
     * 지역별 관광지 데이터
     */
    val attractionsByRegion: Map<String, List<AttractionDto>> = mapOf(
        "제주" to listOf(
            AttractionDto(
                id = 1001, type = "place", name = "성산일출봉",
                address = "제주특별자치도 서귀포시 성산읍",
                description = "유네스코 세계자연유산으로 지정된 제주의 상징적인 화산",
                coverImage = "https://images.unsplash.com/photo-1579169326371-b8148794e7e4?w=800",
                businessTime = "매일 07:00 - 20:00", rating = 4.7,
                latitude = 33.4589, longitude = 126.9426
            ),
            AttractionDto(
                id = 1002, type = "place", name = "우도",
                address = "제주특별자치도 제주시 우도면",
                description = "에메랄드빛 바다와 하얀 모래사장이 아름다운 섬",
                coverImage = "https://images.unsplash.com/photo-1570077188670-e3a8d3c6c6e0?w=800",
                businessTime = "페리 운항시간 확인", rating = 4.6,
                latitude = 33.5000, longitude = 126.9519
            ),
            AttractionDto(
                id = 1003, type = "meal", name = "제주 흑돼지 거리",
                address = "제주특별자치도 제주시 연동",
                description = "제주 특산물 흑돼지를 맛볼 수 있는 맛집 거리",
                coverImage = "https://images.unsplash.com/photo-1544025162-d76694265947?w=800",
                businessTime = "매일 11:00 - 22:00", rating = 4.5,
                latitude = 33.4890, longitude = 126.4983
            ),
            AttractionDto(
                id = 1004, type = "meal", name = "협재해수욕장 맛집거리",
                address = "제주특별자치도 제주시 한림읍",
                description = "신선한 해산물 요리를 즐길 수 있는 곳",
                coverImage = "https://images.unsplash.com/photo-1579584425555-c3ce17fd4351?w=800",
                businessTime = "매일 10:00 - 21:00", rating = 4.4,
                latitude = 33.3940, longitude = 126.2396
            ),
            AttractionDto(
                id = 1005, type = "place", name = "한라산 국립공원",
                address = "제주특별자치도 제주시",
                description = "대한민국 최고봉, 사계절 아름다운 등산 명소",
                coverImage = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800",
                businessTime = "코스별 상이", rating = 4.8,
                latitude = 33.3617, longitude = 126.5292
            )
        ),
        "부산" to listOf(
            AttractionDto(
                id = 2001, type = "place", name = "해운대 해수욕장",
                address = "부산광역시 해운대구 해운대해변로",
                description = "대한민국 대표 해수욕장, 여름 휴가의 성지",
                coverImage = "https://images.unsplash.com/photo-1538485399081-7191377e8241?w=800",
                businessTime = "24시간", rating = 4.5,
                latitude = 35.1587, longitude = 129.1604
            ),
            AttractionDto(
                id = 2002, type = "place", name = "감천문화마을",
                address = "부산광역시 사하구 감내2로",
                description = "알록달록한 집들이 모여있는 부산의 산토리니",
                coverImage = "https://images.unsplash.com/photo-1517154421773-0529f29ea451?w=800",
                businessTime = "매일 09:00 - 18:00", rating = 4.4,
                latitude = 35.0975, longitude = 129.0108
            ),
            AttractionDto(
                id = 2003, type = "meal", name = "자갈치시장",
                address = "부산광역시 중구 자갈치해안로",
                description = "싱싱한 해산물을 맛볼 수 있는 전통시장",
                coverImage = "https://images.unsplash.com/photo-1579584425555-c3ce17fd4351?w=800",
                businessTime = "매일 05:00 - 22:00", rating = 4.3,
                latitude = 35.0968, longitude = 129.0306
            ),
            AttractionDto(
                id = 2004, type = "meal", name = "서면 먹자골목",
                address = "부산광역시 부산진구 서면로",
                description = "다양한 음식점이 밀집한 부산의 핫플레이스",
                coverImage = "https://images.unsplash.com/photo-1544025162-d76694265947?w=800",
                businessTime = "매일 11:00 - 24:00", rating = 4.2,
                latitude = 35.1579, longitude = 129.0599
            ),
            AttractionDto(
                id = 2005, type = "place", name = "광안리 해수욕장",
                address = "부산광역시 수영구 광안해변로",
                description = "광안대교 야경이 아름다운 해변",
                coverImage = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800",
                businessTime = "24시간", rating = 4.6,
                latitude = 35.1532, longitude = 129.1189
            )
        ),
        "강릉" to listOf(
            AttractionDto(
                id = 3001, type = "place", name = "정동진",
                address = "강원특별자치도 강릉시 강동면",
                description = "해돋이 명소로 유명한 동해안 해변",
                coverImage = "https://images.unsplash.com/photo-1596433809252-c0bccce2f244?w=800",
                businessTime = "24시간", rating = 4.5,
                latitude = 37.6899, longitude = 129.0344
            ),
            AttractionDto(
                id = 3002, type = "place", name = "경포대",
                address = "강원특별자치도 강릉시 경포로",
                description = "달맞이 명소, 경포호수와 바다를 함께 즐기는 곳",
                coverImage = "https://images.unsplash.com/photo-1500382017468-9049fed747ef?w=800",
                businessTime = "24시간", rating = 4.4,
                latitude = 37.7948, longitude = 128.8969
            ),
            AttractionDto(
                id = 3003, type = "meal", name = "강릉 중앙시장",
                address = "강원특별자치도 강릉시 금성로",
                description = "닭강정, 짬뽕순두부 등 강릉 먹거리 집합소",
                coverImage = "https://images.unsplash.com/photo-1567521464027-f127ff144326?w=800",
                businessTime = "매일 09:00 - 20:00", rating = 4.3,
                latitude = 37.7536, longitude = 128.8985
            ),
            AttractionDto(
                id = 3004, type = "meal", name = "안목 커피거리",
                address = "강원특별자치도 강릉시 창해로",
                description = "바다를 보며 커피를 즐기는 카페 거리",
                coverImage = "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=800",
                businessTime = "매일 10:00 - 22:00", rating = 4.5,
                latitude = 37.7733, longitude = 128.9519
            ),
            AttractionDto(
                id = 3005, type = "place", name = "오죽헌",
                address = "강원특별자치도 강릉시 율곡로",
                description = "율곡 이이 선생의 생가, 역사 문화 명소",
                coverImage = "https://images.unsplash.com/photo-1534274867514-d5b47ef89ed7?w=800",
                businessTime = "매일 09:00 - 18:00", rating = 4.2,
                latitude = 37.7789, longitude = 128.8790
            )
        ),
        "경주" to listOf(
            AttractionDto(
                id = 4001, type = "place", name = "불국사",
                address = "경상북도 경주시 불국로",
                description = "유네스코 세계문화유산, 신라 불교 예술의 정수",
                coverImage = "https://images.unsplash.com/photo-1592882595561-c4a94a1e3986?w=800",
                businessTime = "매일 07:00 - 18:00", rating = 4.7,
                latitude = 35.7903, longitude = 129.3316
            ),
            AttractionDto(
                id = 4002, type = "place", name = "첨성대",
                address = "경상북도 경주시 인왕동",
                description = "동양 최고의 천문대, 신라 과학의 상징",
                coverImage = "https://images.unsplash.com/photo-1548115184-bc6544d06a58?w=800",
                businessTime = "24시간 (외부 관람)", rating = 4.4,
                latitude = 35.8346, longitude = 129.2190
            ),
            AttractionDto(
                id = 4003, type = "meal", name = "경주 황리단길",
                address = "경상북도 경주시 포석로",
                description = "한옥과 현대가 어우러진 맛집 & 카페 거리",
                coverImage = "https://images.unsplash.com/photo-1567521464027-f127ff144326?w=800",
                businessTime = "매일 10:00 - 22:00", rating = 4.5,
                latitude = 35.8389, longitude = 129.2125
            ),
            AttractionDto(
                id = 4004, type = "meal", name = "경주 교동 쌈밥거리",
                address = "경상북도 경주시 교동",
                description = "정갈한 한정식과 쌈밥을 맛볼 수 있는 곳",
                coverImage = "https://images.unsplash.com/photo-1498654896293-37aacf113fd9?w=800",
                businessTime = "매일 11:00 - 21:00", rating = 4.3,
                latitude = 35.8320, longitude = 129.2150
            ),
            AttractionDto(
                id = 4005, type = "place", name = "동궁과 월지",
                address = "경상북도 경주시 원화로",
                description = "신라 왕궁의 별궁, 야경이 아름다운 역사 명소",
                coverImage = "https://images.unsplash.com/photo-1548115184-bc6544d06a58?w=800",
                businessTime = "매일 09:00 - 22:00", rating = 4.6,
                latitude = 35.8308, longitude = 129.2269
            )
        ),
        "전주" to listOf(
            AttractionDto(
                id = 5001, type = "place", name = "전주 한옥마을",
                address = "전북특별자치도 전주시 완산구",
                description = "700여 채의 한옥이 밀집한 전통 문화 마을",
                coverImage = "https://images.unsplash.com/photo-1534274867514-d5b47ef89ed7?w=800",
                businessTime = "24시간", rating = 4.6,
                latitude = 35.8150, longitude = 127.1530
            ),
            AttractionDto(
                id = 5002, type = "place", name = "경기전",
                address = "전북특별자치도 전주시 완산구 태조로",
                description = "태조 이성계의 어진을 봉안한 곳",
                coverImage = "https://images.unsplash.com/photo-1548115184-bc6544d06a58?w=800",
                businessTime = "매일 09:00 - 19:00", rating = 4.4,
                latitude = 35.8139, longitude = 127.1500
            ),
            AttractionDto(
                id = 5003, type = "meal", name = "전주 비빔밥 골목",
                address = "전북특별자치도 전주시 완산구",
                description = "전주의 대표 음식 비빔밥 맛집이 모여있는 곳",
                coverImage = "https://images.unsplash.com/photo-1498654896293-37aacf113fd9?w=800",
                businessTime = "매일 10:00 - 21:00", rating = 4.5,
                latitude = 35.8165, longitude = 127.1488
            ),
            AttractionDto(
                id = 5004, type = "meal", name = "남부시장",
                address = "전북특별자치도 전주시 완산구 전주객사3길",
                description = "야시장으로 유명한 전통시장, 다양한 먹거리",
                coverImage = "https://images.unsplash.com/photo-1567521464027-f127ff144326?w=800",
                businessTime = "매일 09:00 - 23:00", rating = 4.3,
                latitude = 35.8088, longitude = 127.1455
            ),
            AttractionDto(
                id = 5005, type = "place", name = "전주 향교",
                address = "전북특별자치도 전주시 완산구 향교길",
                description = "조선시대 유교 교육기관, 은행나무 길이 아름다운 곳",
                coverImage = "https://images.unsplash.com/photo-1534274867514-d5b47ef89ed7?w=800",
                businessTime = "매일 09:00 - 18:00", rating = 4.2,
                latitude = 35.8122, longitude = 127.1604
            )
        )
    )

    /**
     * 추천 텍스트 문구
     */
    val suggestions = listOf(
        RecommendTextResponse(
            feeling = "요즘 너무 지쳐서 힐링이 필요해요",
            atmosphere = "조용하고 평화로운 곳이면 좋겠어요",
            activities = "천천히 산책하면서 쉬고 싶어요"
        ),
        RecommendTextResponse(
            feeling = "새로운 경험에 설레고 있어요",
            atmosphere = "활기차고 사람들이 많은 곳으로 가고싶어요",
            activities = "현지 맛집을 찾아다니고 싶어요"
        ),
        RecommendTextResponse(
            feeling = "여유로운 마음으로 떠나고 싶어요",
            atmosphere = "푸른 자연에 둘러싸인 곳이면 좋겠어요",
            activities = "캠핑하며 별구경하고 싶어요"
        ),
        RecommendTextResponse(
            feeling = "모험적인 기분이 들어요",
            atmosphere = "이색적이고 독특한 분위기를 느끼고싶어요",
            activities = "스릴 넘치는 액티비티를 해보고싶어요"
        ),
        RecommendTextResponse(
            feeling = "일상에서 벗어나 재충전하고 싶어요",
            atmosphere = "바다가 보이는 탁 트인 곳이면 좋겠어요",
            activities = "맛있는 해산물을 먹고 싶어요"
        ),
        RecommendTextResponse(
            feeling = "역사와 문화가 궁금해요",
            atmosphere = "고즈넉하고 전통적인 분위기면 좋겠어요",
            activities = "유적지 탐방을 하고 싶어요"
        )
    )

    /**
     * 랜덤 여행지 3개 반환
     */
    fun getRandomDestinations(count: Int = 3): List<RecommendResponse> {
        return destinations.shuffled().take(count)
    }

    /**
     * 랜덤 추천 텍스트 반환
     */
    fun getRandomSuggestion(): RecommendTextResponse {
        return suggestions.random()
    }

    /**
     * 좌표 기반으로 가장 가까운 지역 찾기
     */
    fun findClosestRegion(latitude: Double, longitude: Double): String {
        var closestRegion = "제주"
        var minDistance = Double.MAX_VALUE

        destinations.forEach { dest ->
            val distance = Math.sqrt(
                Math.pow(dest.latitude - latitude, 2.0) +
                Math.pow(dest.longitude - longitude, 2.0)
            )
            if (distance < minDistance) {
                minDistance = distance
                closestRegion = dest.name
            }
        }

        return closestRegion
    }

    /**
     * 지역명으로 관광지 조회
     */
    fun getAttractionsByRegion(regionName: String): List<AttractionDto> {
        val keywords = listOf("제주", "부산", "강릉", "경주", "전주")
        val matchedKey = keywords.find { regionName.contains(it) }

        return if (matchedKey != null && attractionsByRegion.containsKey(matchedKey)) {
            attractionsByRegion[matchedKey]!!
        } else {
            // 매칭되는 지역이 없으면 랜덤 지역 반환
            attractionsByRegion.values.random()
        }
    }

    /**
     * 랜덤 명소 반환 (현재 명소 제외)
     */
    fun getRandomAttraction(excludeId: Long? = null): AttractionDto {
        val allAttractions = attractionsByRegion.values.flatten()
        val filtered = if (excludeId != null) {
            allAttractions.filter { it.id != excludeId }
        } else {
            allAttractions
        }
        return filtered.random()
    }

    /**
     * Mock 일정 생성
     */
    fun generateMockItinerary(duration: Int, regionName: String): List<DailyScheduleDto> {
        val attractions = getAttractionsByRegion(regionName)
        val places = attractions.filter { it.type == "place" }
        val meals = attractions.filter { it.type == "meal" }

        return (1..duration).map { day ->
            val dayAttractions = mutableListOf<AttractionDto>()

            // 아침 식사
            if (meals.isNotEmpty()) {
                dayAttractions.add(meals[day % meals.size].copy(id = (day * 100 + 1).toLong()))
            }

            // 관광지 1
            if (places.isNotEmpty()) {
                dayAttractions.add(places[(day * 2) % places.size].copy(id = (day * 100 + 2).toLong()))
            }

            // 점심 식사
            if (meals.isNotEmpty()) {
                dayAttractions.add(meals[(day + 1) % meals.size].copy(id = (day * 100 + 3).toLong()))
            }

            // 관광지 2
            if (places.size > 1) {
                dayAttractions.add(places[(day * 2 + 1) % places.size].copy(id = (day * 100 + 4).toLong()))
            }

            // 저녁 식사
            if (meals.isNotEmpty()) {
                dayAttractions.add(meals[(day + 2) % meals.size].copy(id = (day * 100 + 5).toLong()))
            }

            DailyScheduleDto(
                dayDate = day,
                attractions = dayAttractions
            )
        }
    }
}
