package com.msa.wedding.infrastructure

import com.msa.common.enums.ServiceType
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class BookmarkClient(
    @Value("\${services.bookmark-service.url:http://bookmark-service:8080}")
    private val bookmarkServiceUrl: String
) {
    private val webClient = WebClient.builder()
        .baseUrl(bookmarkServiceUrl)
        .build()

    fun isBookmarked(userId: Long, serviceType: ServiceType, targetType: String, targetId: Long): Boolean {
        return try {
            webClient.get()
                .uri { builder ->
                    builder.path("/api/v1/bookmarks/internal/check")
                        .queryParam("userId", userId)
                        .queryParam("serviceType", serviceType)
                        .queryParam("targetType", targetType)
                        .queryParam("targetId", targetId)
                        .build()
                }
                .retrieve()
                .bodyToMono<Boolean>()
                .block() ?: false
        } catch (e: Exception) {
            false
        }
    }

    fun getBookmarkCount(serviceType: ServiceType, targetType: String, targetId: Long): Long {
        return try {
            webClient.get()
                .uri { builder ->
                    builder.path("/api/v1/bookmarks/internal/count")
                        .queryParam("serviceType", serviceType)
                        .queryParam("targetType", targetType)
                        .queryParam("targetId", targetId)
                        .build()
                }
                .retrieve()
                .bodyToMono<Long>()
                .block() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    fun getBookmarkCountBatch(serviceType: ServiceType, targetType: String, targetIds: List<Long>): Map<Long, Long> {
        return try {
            webClient.post()
                .uri("/api/v1/bookmarks/internal/count-batch")
                .bodyValue(BatchCountRequest(serviceType, targetType, targetIds))
                .retrieve()
                .bodyToMono<Map<Long, Long>>()
                .block() ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun getBookmarkedIds(userId: Long, serviceType: ServiceType, targetType: String, targetIds: List<Long>): Set<Long> {
        return try {
            webClient.post()
                .uri("/api/v1/bookmarks/internal/check-batch")
                .bodyValue(BatchCheckRequest(userId, serviceType, targetType, targetIds))
                .retrieve()
                .bodyToMono<Set<Long>>()
                .block() ?: emptySet()
        } catch (e: Exception) {
            emptySet()
        }
    }
}

data class BatchCountRequest(
    val serviceType: ServiceType,
    val targetType: String,
    val targetIds: List<Long>
)

data class BatchCheckRequest(
    val userId: Long,
    val serviceType: ServiceType,
    val targetType: String,
    val targetIds: List<Long>
)
