package com.msa.auth.oauth

/**
 * OAuth 클라이언트 인터페이스 (swyp11 스타일)
 */
interface OAuthClient {
    fun getProvider(): String  // "google", "kakao", "naver"
    fun getUserInfo(code: String, redirectUri: String): OAuthUserInfo
}

/**
 * OAuth 사용자 정보 공통 인터페이스
 */
interface OAuthUserInfo {
    val provider: String
    val providerId: String
    val email: String?
    val name: String?
    val profileImage: String?
}
