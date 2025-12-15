package com.msa.ticketing.application

import com.msa.ticketing.domain.queue.QueueStatus
import com.msa.ticketing.domain.queue.QueueToken
import com.msa.ticketing.infrastructure.RedisQueueStore
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class QueueService(
    private val redisQueueStore: RedisQueueStore,
    @Value("\${queue.active-size:100}") private val activeSize: Int
) {

    fun enterQueue(userId: Long, concertId: Long): QueueTokenResponse {
        val token = redisQueueStore.enterQueue(userId, concertId)
        val waitingCount = redisQueueStore.getWaitingCount(concertId)

        return QueueTokenResponse(
            token = token.token,
            status = token.status,
            position = token.position,
            estimatedWaitMinutes = calculateEstimatedWait(token.position ?: 0)
        )
    }

    fun getQueueStatus(token: String): QueueTokenResponse {
        val queueToken = redisQueueStore.getQueueStatus(token)
            ?: return QueueTokenResponse(
                token = token,
                status = QueueStatus.EXPIRED,
                position = null,
                estimatedWaitMinutes = null
            )

        return QueueTokenResponse(
            token = queueToken.token,
            status = queueToken.status,
            position = queueToken.position,
            estimatedWaitMinutes = if (queueToken.status == QueueStatus.WAITING)
                calculateEstimatedWait(queueToken.position ?: 0) else null
        )
    }

    @Scheduled(fixedRate = 5000)  // Every 5 seconds
    fun processQueue() {
        // This would need to iterate over all active concerts
        // Simplified: would get list of concerts with open tickets
        // For each concert, activate next batch if active count < activeSize
    }

    fun activateNextBatch(concertId: Long) {
        val currentActive = redisQueueStore.getActiveCount(concertId)
        if (currentActive < activeSize) {
            val batchSize = (activeSize - currentActive).toInt()
            redisQueueStore.activateNextBatch(concertId, batchSize)
        }
    }

    private fun calculateEstimatedWait(position: Long): Long {
        // Estimate: 100 users processed every 5 seconds = 1200 per minute
        return (position / 1200) + 1
    }
}

data class QueueTokenResponse(
    val token: String,
    val status: QueueStatus,
    val position: Long?,
    val estimatedWaitMinutes: Long?
)
