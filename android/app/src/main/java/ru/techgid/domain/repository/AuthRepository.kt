package ru.techgid.domain.repository

interface AuthRepository {
    suspend fun requestOtp(phone: String): String?
    suspend fun register(phone: String, code: String, displayName: String, password: String): Boolean
    suspend fun login(phone: String, password: String): Boolean
    suspend fun refreshToken(): Boolean
    suspend fun logout()
    fun isLoggedIn(): Boolean
}
