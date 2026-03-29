package ru.techgid.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ── Авторизация ────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class PhoneOTPRequestDto(
    val phone: String,
)

@JsonClass(generateAdapter = true)
data class PhoneOTPVerifyDto(
    val phone: String,
    val code: String,
    @Json(name = "display_name") val displayName: String,
    val password: String,
)

@JsonClass(generateAdapter = true)
data class LoginRequestDto(
    val phone: String,
    val password: String,
)

@JsonClass(generateAdapter = true)
data class TokenPairDto(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "refresh_token") val refreshToken: String,
    @Json(name = "token_type") val tokenType: String = "bearer",
)

@JsonClass(generateAdapter = true)
data class RefreshRequestDto(
    @Json(name = "refresh_token") val refreshToken: String,
)

// ── Пользователь ───────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class UserDto(
    val id: Int,
    val phone: String,
    val email: String? = null,
    @Json(name = "email_verified") val emailVerified: Boolean = false,
    @Json(name = "display_name") val displayName: String,
    @Json(name = "avatar_url") val avatarUrl: String? = null,
    val role: String,
    @Json(name = "is_verified_author") val isVerifiedAuthor: Boolean = false,
    @Json(name = "created_at") val createdAt: String,
)

@JsonClass(generateAdapter = true)
data class UserUpdateDto(
    @Json(name = "display_name") val displayName: String? = null,
    @Json(name = "avatar_url") val avatarUrl: String? = null,
)

@JsonClass(generateAdapter = true)
data class UserCarDto(
    val id: Int,
    @Json(name = "configuration_id") val configurationId: Int,
    @Json(name = "display_name") val displayName: String? = null,
    @Json(name = "is_primary") val isPrimary: Boolean = false,
    @Json(name = "mileage_km") val mileageKm: Int? = null,
)

@JsonClass(generateAdapter = true)
data class UserCarCreateDto(
    @Json(name = "configuration_id") val configurationId: Int,
    @Json(name = "display_name") val displayName: String? = null,
    @Json(name = "is_primary") val isPrimary: Boolean = false,
    val vin: String? = null,
    @Json(name = "mileage_km") val mileageKm: Int? = null,
)
