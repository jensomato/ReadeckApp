package de.readeckapp.io.rest

import de.readeckapp.io.rest.auth.AuthInterceptor
import de.readeckapp.io.rest.auth.TokenManager
import de.readeckapp.io.rest.model.AuthenticationRequestDto
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.create
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection

class ReadeckApiTest {
    private lateinit var mockWebServer: MockWebServer
    val tokenManager = mockk<TokenManager>()
    val authInterceptor = AuthInterceptor(tokenManager)
    val loggingInterceptor =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()
    lateinit var retrofit: Retrofit
    lateinit var readeckApi: ReadeckApi

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val mediaType = "application/json; charset=UTF8".toMediaType()
        retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(Json.asConverterFactory(mediaType))
            .build()

        readeckApi = retrofit.create()

        every { tokenManager.getToken() } returns "mockToken"
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun testSuccessfulAuthentication() = runTest {
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .addHeader("Content-Type", "application/json")
            .setBody(loadJsonFromClasspath("api/auth.json"))
        )
        val response = readeckApi.authenticate(AuthenticationRequestDto("test", "test", "test"))
        assertEquals(true, response.isSuccessful)
        assertEquals(200, response.code())
        assertEquals("theId", response.body()?.id)
        assertEquals("theToken", response.body()?.token)
    }

    @Test
    fun testFailedAuthentication() = runTest {
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_FORBIDDEN)
            .addHeader("Content-Type", "application/json")
            .setBody(loadJsonFromClasspath("api/auth-failure.json"))
        )
        val response = readeckApi.authenticate(AuthenticationRequestDto("test", "test", "test"))
        assertEquals(false, response.isSuccessful)
        assertEquals(403, response.code())
    }

    private fun loadJsonFromClasspath(resourcePath: String): String {
        val inputStream = this.javaClass.classLoader?.getResourceAsStream(resourcePath)
            ?: throw IllegalArgumentException("Resource not found: $resourcePath")

        val reader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            stringBuilder.append(line)
        }
        inputStream.close()
        return stringBuilder.toString()
    }
}