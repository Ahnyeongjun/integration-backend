package com.msa.auth.application

import com.msa.auth.domain.repository.LocalAccountRepository
import com.msa.common.exception.BadRequestException
import com.msa.common.exception.NotFoundException
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.util.concurrent.TimeUnit

@Service
class EmailVerificationService(
    private val redisTemplate: StringRedisTemplate,
    private val emailService: EmailService,
    private val localAccountRepository: LocalAccountRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${mail.verification.expiry-minutes:5}") private val expiryMinutes: Long,
    @Value("\${mail.verification.code-length:6}") private val codeLength: Int
) {

    companion object {
        private const val SIGNUP_CODE_PREFIX = "email:signup:"
        private const val RESET_CODE_PREFIX = "email:reset:"
        private const val VERIFIED_PREFIX = "email:verified:"
        private const val ATTEMPT_PREFIX = "email:attempt:"
        private const val MAX_ATTEMPTS = 5
        private const val BLOCK_MINUTES = 30L
    }

    /**
     * 회원가입용 이메일 인증 코드 발송
     */
    fun sendSignupVerificationCode(email: String) {
        // 이미 가입된 이메일인지 확인
        if (localAccountRepository.existsByEmail(email)) {
            throw BadRequestException("이미 가입된 이메일입니다")
        }

        checkRateLimit(email)
        val code = generateCode()

        // Redis에 코드 저장 (TTL 적용)
        redisTemplate.opsForValue().set(
            "$SIGNUP_CODE_PREFIX$email",
            code,
            expiryMinutes,
            TimeUnit.MINUTES
        )

        emailService.sendVerificationCode(email, code)
    }

    /**
     * 비밀번호 재설정용 이메일 인증 코드 발송
     */
    fun sendPasswordResetCode(email: String) {
        // 가입된 이메일인지 확인
        if (!localAccountRepository.existsByEmail(email)) {
            throw NotFoundException("User", "email: $email")
        }

        checkRateLimit(email)
        val code = generateCode()

        redisTemplate.opsForValue().set(
            "$RESET_CODE_PREFIX$email",
            code,
            expiryMinutes,
            TimeUnit.MINUTES
        )

        emailService.sendPasswordResetCode(email, code)
    }

    /**
     * 회원가입 이메일 인증 코드 검증
     * @return 검증 성공시 임시 토큰 반환 (회원가입 완료에 사용)
     */
    fun verifySignupCode(email: String, code: String): String {
        val key = "$SIGNUP_CODE_PREFIX$email"
        verifyCode(key, email, code)

        // 검증 성공 - 임시 verified 상태 저장 (10분)
        val verificationToken = generateVerificationToken()
        redisTemplate.opsForValue().set(
            "$VERIFIED_PREFIX$verificationToken",
            email,
            10,
            TimeUnit.MINUTES
        )

        // 사용된 코드 삭제
        redisTemplate.delete(key)

        return verificationToken
    }

    /**
     * 비밀번호 재설정 코드 검증
     * @return 검증 성공시 리셋 토큰 반환 (비밀번호 변경에 사용)
     */
    fun verifyPasswordResetCode(email: String, code: String): String {
        val key = "$RESET_CODE_PREFIX$email"
        verifyCode(key, email, code)

        // 검증 성공 - 리셋 토큰 생성 (10분)
        val resetToken = generateVerificationToken()
        redisTemplate.opsForValue().set(
            "$VERIFIED_PREFIX$resetToken",
            email,
            10,
            TimeUnit.MINUTES
        )

        // 사용된 코드 삭제
        redisTemplate.delete(key)

        return resetToken
    }

    /**
     * 비밀번호 재설정
     */
    fun resetPassword(token: String, newPassword: String) {
        val key = "$VERIFIED_PREFIX$token"
        val email = redisTemplate.opsForValue().get(key)
            ?: throw BadRequestException("유효하지 않거나 만료된 토큰입니다")

        val account = localAccountRepository.findByEmail(email)
            ?: throw NotFoundException("User", "email: $email")

        account.password = passwordEncoder.encode(newPassword)
        localAccountRepository.save(account)

        // 사용된 토큰 삭제
        redisTemplate.delete(key)
    }

    /**
     * 이메일 인증 상태 확인 (회원가입 시 사용)
     */
    fun isEmailVerified(token: String): String? {
        return redisTemplate.opsForValue().get("$VERIFIED_PREFIX$token")
    }

    /**
     * 검증 토큰 소비 (회원가입 완료 후)
     */
    fun consumeVerificationToken(token: String) {
        redisTemplate.delete("$VERIFIED_PREFIX$token")
    }

    private fun verifyCode(key: String, email: String, code: String) {
        val attemptKey = "$ATTEMPT_PREFIX$email"
        val attempts = redisTemplate.opsForValue().get(attemptKey)?.toIntOrNull() ?: 0

        if (attempts >= MAX_ATTEMPTS) {
            throw BadRequestException("너무 많은 시도. ${BLOCK_MINUTES}분 후 다시 시도해주세요.")
        }

        val storedCode = redisTemplate.opsForValue().get(key)
            ?: throw BadRequestException("인증 코드가 만료되었거나 존재하지 않습니다")

        if (storedCode != code) {
            // 실패 횟수 증가
            redisTemplate.opsForValue().set(
                attemptKey,
                (attempts + 1).toString(),
                BLOCK_MINUTES,
                TimeUnit.MINUTES
            )
            throw BadRequestException("인증 코드가 일치하지 않습니다")
        }

        // 성공시 시도 횟수 초기화
        redisTemplate.delete(attemptKey)
    }

    private fun checkRateLimit(email: String) {
        val attemptKey = "$ATTEMPT_PREFIX$email"
        val attempts = redisTemplate.opsForValue().get(attemptKey)?.toIntOrNull() ?: 0
        if (attempts >= MAX_ATTEMPTS) {
            throw BadRequestException("너무 많은 요청. ${BLOCK_MINUTES}분 후 다시 시도해주세요.")
        }
    }

    private fun generateCode(): String {
        val random = SecureRandom()
        val code = StringBuilder()
        repeat(codeLength) {
            code.append(random.nextInt(10))
        }
        return code.toString()
    }

    private fun generateVerificationToken(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
