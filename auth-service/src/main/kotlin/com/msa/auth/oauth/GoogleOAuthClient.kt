package com.msa.auth.oauth

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

/**
 * Google OAuth 클라이언트
 */
@Component
class GoogleOAuthClient(
    @Value("\${oauth.google.client-id}") private val clientId: String,
    @Value("\${oauth.google.client-secret}") private val clientSecret: String,
    @Value("\${oauth.google.token-uri:https://oauth2.googleapis.com/token}") private val tokenUri: String,
    @Value("\${oauth.google.userinfo-uri:https://www.googleapis.com/oauth2/v3/userinfo}") private val userinfoUri: String
) : OAuthClient {

    private val restTemplate = RestTemplate()

    override fun getProvider(): String = "google"

    override fun getUserInfo(code: String, redirectUri: String): OAuthUserInfo {
        // 1. code → access_token 교환
        val params = LinkedMultiValueMap<String, String>().apply {
            add("code", code)
            add("client_id", clientId)
            add("client_secret", clientSecret)
            add("redirect_uri", redirectUri)
            add("grant_type", "authorization_code")
        }

        val tokenResponse = restTemplate.postForObject(tokenUri, params, Map::class.java)
            ?: throw RuntimeException("Failed to get Google access token")

        val accessToken = tokenResponse["access_token"] as String

        // 2. 사용자 정보 요청
        val headers = HttpHeaders().apply {
            setBearerAuth(accessToken)
        }
        val entity = HttpEntity<Void>(headers)

        val userInfo = restTemplate.exchange(userinfoUri, HttpMethod.GET, entity, Map::class.java).body
            ?: throw RuntimeException("Failed to get Google user info")

        return GoogleUserInfo(userInfo as Map<String, Any>)
    }
}

/**
 * Google 사용자 정보
 */
data class GoogleUserInfo(
    private val attributes: Map<String, Any>
) : OAuthUserInfo {

    override val provider: String = "google"

    // 구글은 "sub" 또는 "id" 둘 중 하나로 옴
    override val providerId: String
        get() = attributes["sub"]?.toString()
            ?: attributes["id"]?.toString()
            ?: "unknown"

    override val email: String?
        get() = attributes["email"]?.toString()

    override val name: String?
        get() = attributes["name"]?.toString()

    override val profileImage: String?
        get() = attributes["picture"]?.toString()
}
