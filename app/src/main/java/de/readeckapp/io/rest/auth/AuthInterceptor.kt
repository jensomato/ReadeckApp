package de.readeckapp.io.rest.auth

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
    private val notificationHelper: NotificationHelper
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()

        tokenManager.getToken()?.let { token ->
            request.addHeader("Authorization", "Bearer $token")
        }

        val response = chain.proceed(request.build())

        if (response.code == 401) {
            Timber.d("Received 401, showing notification")
            notificationHelper.showUnauthorizedNotification()
        }

        return response
    }
}
