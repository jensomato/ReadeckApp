package de.readeckapp.io.rest

import de.readeckapp.io.rest.auth.AuthInterceptor
import de.readeckapp.io.rest.auth.TokenManager
import de.readeckapp.io.rest.model.AuthenticationRequestDto
import de.readeckapp.io.rest.model.EditBookmarkDto
import de.readeckapp.io.rest.model.EditBookmarkErrorDto
import de.readeckapp.io.rest.model.StatusMessageDto
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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

    @Test
    fun testSuccessfulEditBookmark() = runTest {
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .addHeader("Content-Type", "application/json")
            .setBody(loadJsonFromClasspath("api/update-success.json"))
        )

        val editBookmarkDto = EditBookmarkDto(
            addLabels = listOf("label1", "label2"),
            isArchived = true,
            isDeleted = false,
            isMarked = true,
            labels = listOf("label3"),
            readAnchor = "anchor1",
            readProgress = 50,
            removeLabels = listOf("label4"),
            title = "New Title"
        )

        val response = readeckApi.editBookmark("bookmarkId", editBookmarkDto)

        assertEquals(true, response.isSuccessful)
        assertEquals(200, response.code())
        assertTrue(response.body()?.isMarked!!)
    }

    @Test
    fun testFailedEditBookmark() = runTest {
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(422)
            .addHeader("Content-Type", "application/json")
            .setBody(loadJsonFromClasspath("api/update-invalid-data.json"))
        )

        val editBookmarkDto = EditBookmarkDto(
            addLabels = listOf("label1", "label2"),
            isArchived = true,
            isDeleted = false,
            isMarked = true,
            labels = listOf("label3"),
            readAnchor = "anchor1",
            readProgress = 50,
            removeLabels = listOf("label4"),
            title = "New Title"
        )

        val response = readeckApi.editBookmark("bookmarkId", editBookmarkDto)

        assertEquals(false, response.isSuccessful)
        assertEquals(422, response.code())

        val errorBody = response.errorBody()?.string()
        val editBookmarkErrorDto = Json.decodeFromString<EditBookmarkErrorDto>(errorBody!!)

        assertEquals(false, editBookmarkErrorDto.isValid)
        assertEquals("invalid type", editBookmarkErrorDto.fields?.get("is_marked")?.errors?.get(0))
    }

    @Test
    fun testGetBookmarks() = runTest {
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .addHeader("Content-Type", "application/json")
            .setBody(loadJsonFromClasspath("api/bookmarks.json"))
        )

        val response = readeckApi.getBookmarks(10, 0, null, ReadeckApi.SortOrder(ReadeckApi.Sort.Created))

        assertTrue(response.isSuccessful)
        assertEquals(200, response.code())
        assertEquals(2, response.body()?.size)
    }

    @Test
    fun testSuccessfulDeleteBookmark() = runTest {
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_NO_CONTENT)
        )

        val response = readeckApi.deleteBookmark("bookmarkId")

        assertTrue(response.isSuccessful)
        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, response.code())
        // No body to assert, just checking the status code
    }

    @Test
    fun testFailedDeleteBookmark() = runTest {
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_FORBIDDEN)
            .addHeader("Content-Type", "application/json")
            .setBody(loadJsonFromClasspath("api/auth-failure.json"))
        )

        val response = readeckApi.deleteBookmark("bookmarkId")

        assertFalse(response.isSuccessful)
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, response.code())

        val errorBody = response.errorBody()?.string()
        val statusMessage = Json.decodeFromString<StatusMessageDto>(errorBody!!)

        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, statusMessage.status)
        assertEquals("Invalid user and/or password", statusMessage.message)
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
