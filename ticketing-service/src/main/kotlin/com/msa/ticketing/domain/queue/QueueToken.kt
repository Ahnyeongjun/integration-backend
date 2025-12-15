package com.msa.ticketing.domain.queue

import java.time.Instant
import java.util.*

data class QueueToken(
    val token: String = UUID.randomUUID().toString(),
    val userId: Long,
    val concertId: Long,
    val createdAt: Instant = Instant.now(),
    var status: QueueStatus = QueueStatus.WAITING,
    var position: Long? = null,
    var activatedAt: Instant? = null
)

enum class QueueStatus {
    WAITING, ACTIVE, EXPIRED, COMPLETED
}
