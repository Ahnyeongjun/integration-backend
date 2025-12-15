package com.msa.ticketing.infrastructure

import com.fasterxml.jackson.databind.ObjectMapper
import com.msa.ticketing.domain.queue.QueueStatus
import com.msa.ticketing.domain.queue.QueueToken
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

@Component
class RedisQueueStore(
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper
) {
    companion object {
        private const val WAITING_QUEUE_KEY = "queue:waiting:"
        private const val ACTIVE_QUEUE_KEY = "queue:active:"
        private const val TOKEN_KEY = "queue:token:"
    }

    fun enterQueue(userId: Long, concertId: Long): QueueToken {
        val token = QueueToken(userId = userId, concertId = concertId)

        // Add to waiting queue (sorted set with timestamp as score)
        redisTemplate.opsForZSet().add(
            "$WAITING_QUEUE_KEY$concertId",
            token.token,
            System.currentTimeMillis().toDouble()
        )

        // Store token data
        redisTemplate.opsForValue().set(
            "$TOKEN_KEY${token.token}",
            objectMapper.writeValueAsString(token),
            Duration.ofMinutes(30)
        )

        // Calculate position
        val position = redisTemplate.opsForZSet().rank("$WAITING_QUEUE_KEY$concertId", token.token)
        token.position = (position ?: 0) + 1

        return token
    }

    fun getQueueStatus(token: String): QueueToken? {
        val tokenData = redisTemplate.opsForValue().get("$TOKEN_KEY$token") ?: return null
        val queueToken = objectMapper.readValue(tokenData, QueueToken::class.java)

        // Check if in active queue
        val isActive = redisTemplate.opsForSet().isMember("$ACTIVE_QUEUE_KEY${queueToken.concertId}", token)
        if (isActive == true) {
            queueToken.status = QueueStatus.ACTIVE
            return queueToken
        }

        // Get position in waiting queue
        val position = redisTemplate.opsForZSet().rank("$WAITING_QUEUE_KEY${queueToken.concertId}", token)
        if (position != null) {
            queueToken.position = position + 1
            queueToken.status = QueueStatus.WAITING
        } else {
            queueToken.status = QueueStatus.EXPIRED
        }

        return queueToken
    }

    fun activateNextBatch(concertId: Long, batchSize: Int): List<QueueToken> {
        val waitingKey = "$WAITING_QUEUE_KEY$concertId"
        val activeKey = "$ACTIVE_QUEUE_KEY$concertId"

        // Get next batch from waiting queue
        val tokens = redisTemplate.opsForZSet().range(waitingKey, 0, batchSize.toLong() - 1) ?: return emptyList()

        val activatedTokens = mutableListOf<QueueToken>()
        tokens.forEach { tokenStr ->
            // Move from waiting to active
            redisTemplate.opsForZSet().remove(waitingKey, tokenStr)
            redisTemplate.opsForSet().add(activeKey, tokenStr)

            // Update token data
            val tokenData = redisTemplate.opsForValue().get("$TOKEN_KEY$tokenStr")
            if (tokenData != null) {
                val token = objectMapper.readValue(tokenData, QueueToken::class.java)
                token.status = QueueStatus.ACTIVE
                token.activatedAt = Instant.now()
                redisTemplate.opsForValue().set(
                    "$TOKEN_KEY$tokenStr",
                    objectMapper.writeValueAsString(token),
                    Duration.ofMinutes(10)  // Active tokens expire in 10 minutes
                )
                activatedTokens.add(token)
            }
        }

        return activatedTokens
    }

    fun completeToken(token: String, concertId: Long) {
        redisTemplate.opsForSet().remove("$ACTIVE_QUEUE_KEY$concertId", token)
        redisTemplate.delete("$TOKEN_KEY$token")
    }

    fun getWaitingCount(concertId: Long): Long {
        return redisTemplate.opsForZSet().size("$WAITING_QUEUE_KEY$concertId") ?: 0
    }

    fun getActiveCount(concertId: Long): Long {
        return redisTemplate.opsForSet().size("$ACTIVE_QUEUE_KEY$concertId") ?: 0
    }
}
