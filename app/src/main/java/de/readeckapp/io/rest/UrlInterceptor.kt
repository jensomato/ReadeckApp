package de.readeckapp.io.rest

import de.readeckapp.coroutine.ApplicationScope
import de.readeckapp.io.prefs.SettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class UrlInterceptor @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    @ApplicationScope private val applicationScope: CoroutineScope
) : Interceptor {
    @Volatile
    private var baseUrl: String? = null
    init {
        applicationScope.launch {
            settingsDataStore.urlFlow.collectLatest {
                Timber.d("new url = $it")
                baseUrl = it
            }
        }
    }
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val originalUrl = request.url.toString()

        if (baseUrl.isNullOrEmpty()) {
            throw IOException("baseUrl is not set")
        } else {
            // Modify the request's URL to use the current baseUrl
            println("originalUrl: $originalUrl")
            val newUrl = originalUrl.replace(
                "http://readeck.invalid",
                baseUrl!!
            )
            println("newUrl: $newUrl baseUrl: $baseUrl") //debugging
            val newRequest: Request = request.newBuilder()
                .url(newUrl)
                .build()

            return chain.proceed(newRequest)

        }
    }
}