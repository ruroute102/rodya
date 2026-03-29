package ru.techgid.data.remote.api

import retrofit2.http.Body
import retrofit2.http.POST
import ru.techgid.data.remote.dto.LoginRequestDto
import ru.techgid.data.remote.dto.PhoneOTPRequestDto
import ru.techgid.data.remote.dto.PhoneOTPVerifyDto
import ru.techgid.data.remote.dto.RefreshRequestDto
import ru.techgid.data.remote.dto.TokenPairDto

interface AuthApi {

    @POST("auth/otp/request")
    suspend fun requestOtp(@Body body: PhoneOTPRequestDto): Map<String, String>

    @POST("auth/register")
    suspend fun register(@Body body: PhoneOTPVerifyDto): TokenPairDto

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequestDto): TokenPairDto

    @POST("auth/refresh")
    suspend fun refreshToken(@Body body: RefreshRequestDto): TokenPairDto

    @POST("auth/logout")
    suspend fun logout(@Body body: RefreshRequestDto): Map<String, String>
}
