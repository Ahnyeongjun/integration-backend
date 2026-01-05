package com.msa.auth.oauth

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

/**
 * Naver OAuth 클라이언트
 */
@Component
class NaverOAuthClient(
    @Value("\${oauth.naver.client-id}") private val clientId: String,
    @Value("\${oauth.naver.client-secret}") private val clientSecret: String,
    @Value("\${oauth.naver.token-uri:https://nid.naver.com/oauth2.0/token}") private val tokenUri: String,
    @Value("\${oauth.naver.userinfo-uri:https://openapi.naver.com/v1/nid/me}") private val userinfoUri: String
) : OAuthClient {

    private val restTemplate = RestTemplate()

    override fun getProvider(): String = "naver"

    override fun getUserInfo(code: String, redirectUri: String): OAuthUserInfo {
        // 1. code → access_token 교환
        val tokenUrl = "$tokenUri?grant_type=authorization_code&client_id=$clientId&client_secret=$clientSecret&code=$code"

        val tokenResponse = restTemplate.postForObject(tokenUrl, null, Map::class.java)
            ?: throw RuntimeException("Failed to get Naver access token")

        val accessToken = tokenResponse["access_token"] as String

        // 2. 사용자 정보 요청
        val headers = HttpHeaders().apply {
            setBearerAuth(accessToken)
        }
        val entity = HttpEntity<Void>(headers)

        val userInfo = restTemplate.exchange(userinfoUri, HttpMethod.GET, entity, Map::class.java).body
            ?: throw RuntimeException("Failed to get Naver user info")

        return NaverUserInfo(userInfo as Map<String, Any>)
    }
}

/**
 * Naver 사용자 정보
 */
data class NaverUserInfo(
    private val attributes: Map<String, Any>
) : OAuthUserInfo {

    override val provider: String = "naver"

    @Suppress("UNCHECKED_CAST")
    private val response: Map<String, Any>?
        get() = attributes["response"] as? Map<String, Any>

    override val providerId: String
        get() = response?.get("id")?.toString() ?: "unknown"

    override val email: String?
        get() = response?.get("email")?.toString()

    override val name: String?
        get() = response?.get("nickname")?.toString()
            ?: response?.get("name")?.toString()

    override val profileImage: String?
        get() = response?.get("profile_image")?.toString()
}
