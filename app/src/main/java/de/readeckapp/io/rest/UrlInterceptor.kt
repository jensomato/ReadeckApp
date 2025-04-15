package de.readeckapp.io.rest

import de.readeckapp.coroutine.ApplicationScope
import de.readeckapp.io.prefs.SettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class UrlInterceptor @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val originalUrl = request.url.toString()

        // Retrieve the baseUrl synchronously
        val baseUrl = runBlocking { settingsDataStore.urlFlow.first() }

        if (baseUrl.isNullOrEmpty()) {
            throw IOException("baseUrl is not set")
        } else {
            // Modify the request's URL to use the current baseUrl
            val newUrl = originalUrl.replace(
                "http://readeck.invalid",
                baseUrl
            )
            val newRequest: Request = request.newBuilder()
                .url(newUrl)
                .build()

            return chain.proceed(newRequest)
        }
    }
}
