package com.msa.auth.application

import com.msa.auth.domain.entity.OAuthProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

interface OAuthClient {
    fun getUserInfo(code: String): OAuthUserInfo
}

@Component
class OAuthClientFactory(
    private val kakaoOAuthClient: KakaoOAuthClient,
    private val googleOAuthClient: GoogleOAuthClient,
    private val naverOAuthClient: NaverOAuthClient
) {
    fun getClient(provider: OAuthProvider): OAuthClient {
        return when (provider) {
            OAuthProvider.KAKAO -> kakaoOAuthClient
            OAuthProvider.GOOGLE -> googleOAuthClient
            OAuthProvider.NAVER -> naverOAuthClient
        }
    }
}

@Component
class KakaoOAuthClient(
    @Value("\${oauth.kakao.client-id}") private val clientId: String,
    @Value("\${oauth.kakao.client-secret}") private val clientSecret: String,
    @Value("\${oauth.kakao.redirect-uri}") private val redirectUri: String
) : OAuthClient {

    private val webClient = WebClient.builder().build()

    override fun getUserInfo(code: String): OAuthUserInfo {
        val tokenResponse = getAccessToken(code)
        return fetchUserInfo(tokenResponse.accessToken)
    }

    private fun getAccessToken(code: String): KakaoTokenResponse {
        return webClient.post()
            .uri("https://kauth.kakao.com/oauth/token")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .bodyValue("grant_type=authorization_code&client_id=$clientId&client_secret=$clientSecret&redirect_uri=$redirectUri&code=$code")
            .retrieve()
            .bodyToMono(KakaoTokenResponse::class.java)
            .block() ?: throw RuntimeException("Failed to get Kakao access token")
    }

    private fun fetchUserInfo(accessToken: String): OAuthUserInfo {
        val response = webClient.get()
            .uri("https://kapi.kakao.com/v2/user/me")
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .bodyToMono(KakaoUserResponse::class.java)
            .block() ?: throw RuntimeException("Failed to get Kakao user info")

        return OAuthUserInfo(
            id = response.id.toString(),
            email = response.kakaoAccount?.email,
            nickname = response.kakaoAccount?.profile?.nickname,
            profileImage = response.kakaoAccount?.profile?.profileImageUrl
        )
    }
}

@Component
class GoogleOAuthClient(
    @Value("\${oauth.google.client-id}") private val clientId: String,
    @Value("\${oauth.google.client-secret}") private val clientSecret: String,
    @Value("\${oauth.google.redirect-uri}") private val redirectUri: String
) : OAuthClient {

    private val webClient = WebClient.builder().build()

    override fun getUserInfo(code: String): OAuthUserInfo {
        val tokenResponse = getAccessToken(code)
        return fetchUserInfo(tokenResponse.accessToken)
    }

    private fun getAccessToken(code: String): GoogleTokenResponse {
        return webClient.post()
            .uri("https://oauth2.googleapis.com/token")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .bodyValue("grant_type=authorization_code&client_id=$clientId&client_secret=$clientSecret&redirect_uri=$redirectUri&code=$code")
            .retrieve()
            .bodyToMono(GoogleTokenResponse::class.java)
            .block() ?: throw RuntimeException("Failed to get Google access token")
    }

    private fun fetchUserInfo(accessToken: String): OAuthUserInfo {
        val response = webClient.get()
            .uri("https://www.googleapis.com/oauth2/v2/userinfo")
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .bodyToMono(GoogleUserResponse::class.java)
            .block() ?: throw RuntimeException("Failed to get Google user info")

        return OAuthUserInfo(
            id = response.id,
            email = response.email,
            nickname = response.name,
            profileImage = response.picture
        )
    }
}

@Component
class NaverOAuthClient(
    @Value("\${oauth.naver.client-id}") private val clientId: String,
    @Value("\${oauth.naver.client-secret}") private val clientSecret: String,
    @Value("\${oauth.naver.redirect-uri}") private val redirectUri: String
) : OAuthClient {

    private val webClient = WebClient.builder().build()

    override fun getUserInfo(code: String): OAuthUserInfo {
        val tokenResponse = getAccessToken(code)
        return fetchUserInfo(tokenResponse.accessToken)
    }

    private fun getAccessToken(code: String): NaverTokenResponse {
        return webClient.post()
            .uri("https://nid.naver.com/oauth2.0/token?grant_type=authorization_code&client_id=$clientId&client_secret=$clientSecret&code=$code")
            .retrieve()
            .bodyToMono(NaverTokenResponse::class.java)
            .block() ?: throw RuntimeException("Failed to get Naver access token")
    }

    private fun fetchUserInfo(accessToken: String): OAuthUserInfo {
        val response = webClient.get()
            .uri("https://openapi.naver.com/v1/nid/me")
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .bodyToMono(NaverUserResponse::class.java)
            .block() ?: throw RuntimeException("Failed to get Naver user info")

        return OAuthUserInfo(
            id = response.response.id,
            email = response.response.email,
            nickname = response.response.nickname,
            profileImage = response.response.profileImage
        )
    }
}

// DTOs for OAuth responses
data class KakaoTokenResponse(val accessToken: String)
data class KakaoUserResponse(val id: Long, val kakaoAccount: KakaoAccount?)
data class KakaoAccount(val email: String?, val profile: KakaoProfile?)
data class KakaoProfile(val nickname: String?, val profileImageUrl: String?)

data class GoogleTokenResponse(val accessToken: String)
data class GoogleUserResponse(val id: String, val email: String?, val name: String?, val picture: String?)

data class NaverTokenResponse(val accessToken: String)
data class NaverUserResponse(val response: NaverUserInfo)
data class NaverUserInfo(val id: String, val email: String?, val nickname: String?, val profileImage: String?)
