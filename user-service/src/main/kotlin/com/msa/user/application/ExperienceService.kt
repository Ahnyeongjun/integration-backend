package com.msa.user.application

import com.msa.user.domain.entity.UserExperience
import com.msa.user.domain.repository.UserExperienceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ExperienceService(
    private val userExperienceRepository: UserExperienceRepository
) {

    @Transactional
    fun saveExperience(userId: Long, rating: Int, feedback: String?): UserExperience {
        // 기존 경험이 있으면 업데이트, 없으면 새로 생성
        val existing = userExperienceRepository.findByUserId(userId)

        return if (existing != null) {
            existing.rating = rating
            existing.feedback = feedback
            userExperienceRepository.save(existing)
        } else {
            val experience = UserExperience(
                userId = userId,
                rating = rating,
                feedback = feedback
            )
            userExperienceRepository.save(experience)
        }
    }

    @Transactional(readOnly = true)
    fun getExperience(userId: Long): UserExperience? {
        return userExperienceRepository.findByUserId(userId)
    }
}
