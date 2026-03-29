package ru.techgid.data.repository

import ru.techgid.data.remote.api.AuthApi
import ru.techgid.data.remote.dto.LoginRequestDto
import ru.techgid.data.remote.dto.PhoneOTPRequestDto
import ru.techgid.data.remote.dto.PhoneOTPVerifyDto
import ru.techgid.data.remote.dto.RefreshRequestDto
import ru.techgid.domain.repository.AuthRepository
import ru.techgid.security.TokenManager
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager,
) : AuthRepository {

    override suspend fun requestOtp(phone: String): String? {
        val response = authApi.requestOtp(PhoneOTPRequestDto(phone))
        // debug_code доступен только в dev-режиме
        return response["debug_code"]
    }

    override suspend fun register(
        phone: String,
        code: String,
        displayName: String,
        password: String,
    ): Boolean {
        val tokens = authApi.register(
            PhoneOTPVerifyDto(
                phone = phone,
                code = code,
                displayName = displayName,
                password = password,
            )
        )
        tokenManager.saveTokens(tokens.accessToken, tokens.refreshToken)
        return true
    }

    override suspend fun login(phone: String, password: String): Boolean {
        val tokens = authApi.login(LoginRequestDto(phone, password))
        tokenManager.saveTokens(tokens.accessToken, tokens.refreshToken)
        return true
    }

    override suspend fun refreshToken(): Boolean {
        val refreshToken = tokenManager.getRefreshToken() ?: return false
        val tokens = authApi.refreshToken(RefreshRequestDto(refreshToken))
        tokenManager.saveTokens(tokens.accessToken, tokens.refreshToken)
        return true
    }

    override suspend fun logout() {
        val refreshToken = tokenManager.getRefreshToken()
        if (refreshToken != null) {
            try {
                authApi.logout(RefreshRequestDto(refreshToken))
            } catch (_: Exception) {
                // Если сервер недоступен — всё равно очищаем локально
            }
        }
        tokenManager.clear()
    }

    override fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()
}
