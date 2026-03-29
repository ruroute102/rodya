package ru.techgid.data.remote.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import ru.techgid.data.remote.dto.UserCarCreateDto
import ru.techgid.data.remote.dto.UserCarDto
import ru.techgid.data.remote.dto.UserDto
import ru.techgid.data.remote.dto.UserUpdateDto

interface UserApi {

    @GET("users/me")
    suspend fun getMe(): UserDto

    @PATCH("users/me")
    suspend fun updateMe(@Body body: UserUpdateDto): UserDto

    @GET("users/me/cars")
    suspend fun getMyCars(): List<UserCarDto>

    @POST("users/me/cars")
    suspend fun addCar(@Body body: UserCarCreateDto): UserCarDto

    @DELETE("users/me/cars/{carId}")
    suspend fun removeCar(@Path("carId") carId: Int): Map<String, String>
}
