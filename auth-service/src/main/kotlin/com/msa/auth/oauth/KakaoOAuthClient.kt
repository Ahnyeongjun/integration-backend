package com.msa.auth.oauth

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

/**
 * Kakao OAuth 클라이언트
 */
@Component
class KakaoOAuthClient(
    @Value("\${oauth.kakao.client-id}") private val clientId: String,
    @Value("\${oauth.kakao.client-secret:}") private val clientSecret: String,
    @Value("\${oauth.kakao.token-uri:https://kauth.kakao.com/oauth/token}") private val tokenUri: String,
    @Value("\${oauth.kakao.userinfo-uri:https://kapi.kakao.com/v2/user/me}") private val userinfoUri: String
) : OAuthClient {

    private val restTemplate = RestTemplate()

    override fun getProvider(): String = "kakao"

    override fun getUserInfo(code: String, redirectUri: String): OAuthUserInfo {
        // 1. code → access_token 교환
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_FORM_URLENCODED
        }

        val params = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "authorization_code")
            add("client_id", clientId)
            if (clientSecret.isNotBlank()) {
                add("client_secret", clientSecret)
            }
            add("redirect_uri", redirectUri)
            add("code", code)
        }

        val tokenRequest = HttpEntity(params, headers)
        val tokenResponse = restTemplate.postForObject(tokenUri, tokenRequest, Map::class.java)
            ?: throw RuntimeException("Failed to get Kakao access token")

        val accessToken = tokenResponse["access_token"] as String

        // 2. 사용자 정보 요청
        val userHeaders = HttpHeaders().apply {
            setBearerAuth(accessToken)
        }
        val userRequest = HttpEntity<Void>(userHeaders)

        val userInfo = restTemplate.exchange(userinfoUri, HttpMethod.GET, userRequest, Map::class.java).body
            ?: throw RuntimeException("Failed to get Kakao user info")

        return KakaoUserInfo(userInfo as Map<String, Any>)
    }
}

/**
 * Kakao 사용자 정보
 */
data class KakaoUserInfo(
    private val attributes: Map<String, Any>
) : OAuthUserInfo {

    override val provider: String = "kakao"

    override val providerId: String
        get() = attributes["id"]?.toString() ?: "unknown"

    @Suppress("UNCHECKED_CAST")
    private val kakaoAccount: Map<String, Any>?
        get() = attributes["kakao_account"] as? Map<String, Any>

    @Suppress("UNCHECKED_CAST")
    private val profile: Map<String, Any>?
        get() = kakaoAccount?.get("profile") as? Map<String, Any>

    override val email: String?
        get() = kakaoAccount?.get("email")?.toString()

    override val name: String?
        get() = profile?.get("nickname")?.toString()

    override val profileImage: String?
        get() = profile?.get("profile_image_url")?.toString()
}
