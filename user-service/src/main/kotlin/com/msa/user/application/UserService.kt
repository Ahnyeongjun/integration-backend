package com.msa.user.application

import com.msa.common.exception.NotFoundException
import com.msa.user.domain.entity.User
import com.msa.user.domain.entity.UserStatus
import com.msa.user.domain.repository.UserRepository
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository
) {

    @Transactional(readOnly = true)
    fun getUser(userId: Long): User {
        return userRepository.findByIdAndStatus(userId, UserStatus.ACTIVE)
            ?: throw NotFoundException("User", userId)
    }

    fun updateProfile(userId: Long, request: ProfileUpdateRequest): User {
        val user = getUser(userId)
        user.updateProfile(request.nickname, request.profileImage, request.bio)
        return userRepository.save(user)
    }

    fun deleteUser(userId: Long) {
        val user = getUser(userId)
        user.delete()
        userRepository.save(user)
    }

    @KafkaListener(topics = ["user-events"], groupId = "user-service")
    fun handleUserCreatedEvent(event: Map<String, Any>) {
        val userId = (event["userId"] as Number).toLong()
        if (userRepository.existsById(userId)) return

        val user = User(
            id = userId,
            email = event["email"] as? String,
            nickname = event["nickname"] as? String,
            profileImage = event["profileImage"] as? String
        )
        userRepository.save(user)
    }
}

data class ProfileUpdateRequest(
    val nickname: String?,
    val profileImage: String?,
    val bio: String?
)
