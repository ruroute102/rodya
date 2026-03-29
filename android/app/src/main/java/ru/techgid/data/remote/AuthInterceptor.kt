package ru.techgid.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import ru.techgid.security.TokenManager
import javax.inject.Inject

/**
 * OkHttp-интерцептор: подставляет access token в заголовок Authorization.
 */
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val accessToken = tokenManager.getAccessToken()
        if (accessToken.isNullOrBlank()) {
            return chain.proceed(original)
        }

        val request = original.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()

        return chain.proceed(request)
    }
}
