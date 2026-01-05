package com.msa.auth.application

import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val mailSender: JavaMailSender,
    @Value("\${mail.from}") private val fromEmail: String
) {

    fun sendVerificationCode(to: String, code: String) {
        val message = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")

        helper.setFrom(fromEmail)
        helper.setTo(to)
        helper.setSubject("[MSA Platform] 이메일 인증 코드")
        helper.setText(buildVerificationEmailHtml(code), true)

        mailSender.send(message)
    }

    fun sendPasswordResetCode(to: String, code: String) {
        val message = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")

        helper.setFrom(fromEmail)
        helper.setTo(to)
        helper.setSubject("[MSA Platform] 비밀번호 재설정 코드")
        helper.setText(buildPasswordResetEmailHtml(code), true)

        mailSender.send(message)
    }

    private fun buildVerificationEmailHtml(code: String): String = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                .container { max-width: 600px; margin: 0 auto; padding: 20px; font-family: Arial, sans-serif; }
                .code { font-size: 32px; font-weight: bold; color: #4F46E5; letter-spacing: 8px; text-align: center; padding: 20px; background: #F3F4F6; border-radius: 8px; margin: 20px 0; }
                .footer { color: #6B7280; font-size: 12px; margin-top: 30px; }
            </style>
        </head>
        <body>
            <div class="container">
                <h2>이메일 인증</h2>
                <p>아래 인증 코드를 입력해주세요.</p>
                <div class="code">$code</div>
                <p>이 코드는 5분 후 만료됩니다.</p>
                <div class="footer">
                    <p>본 메일은 발신 전용입니다.</p>
                </div>
            </div>
        </body>
        </html>
    """.trimIndent()

    private fun buildPasswordResetEmailHtml(code: String): String = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                .container { max-width: 600px; margin: 0 auto; padding: 20px; font-family: Arial, sans-serif; }
                .code { font-size: 32px; font-weight: bold; color: #DC2626; letter-spacing: 8px; text-align: center; padding: 20px; background: #FEF2F2; border-radius: 8px; margin: 20px 0; }
                .footer { color: #6B7280; font-size: 12px; margin-top: 30px; }
            </style>
        </head>
        <body>
            <div class="container">
                <h2>비밀번호 재설정</h2>
                <p>아래 인증 코드를 입력하여 비밀번호를 재설정하세요.</p>
                <div class="code">$code</div>
                <p>이 코드는 5분 후 만료됩니다.</p>
                <p>본인이 요청하지 않았다면 이 메일을 무시하세요.</p>
                <div class="footer">
                    <p>본 메일은 발신 전용입니다.</p>
                </div>
            </div>
        </body>
        </html>
    """.trimIndent()
}
