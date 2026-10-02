package de.readeckapp.io.rest

import de.readeckapp.io.prefs.SettingsDataStore
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class UrlInterceptorTest {
    private val settingsDataStore = mockk<SettingsDataStore>()

    private fun runInterceptor(userUrl: String?, originalUrl: String = "http://readeck.invalid/api/bookmarks"): String {
        val flow = MutableStateFlow(userUrl)
        every { settingsDataStore.urlFlow } returns flow

        val interceptor = UrlInterceptor(settingsDataStore)
        val request = Request.Builder().url(originalUrl).build()

        val captured = slot<Request>()
        val dummyResponse = mockk<Response>(relaxed = true)

        val chain = mockk<Interceptor.Chain> {
            every { request() } returns request
            every { proceed(capture(captured)) } returns dummyResponse
        }

        interceptor.intercept(chain)

        return captured.captured.url.toString()
    }

    @Test
    fun testTrailingSlashMustNotCreateDoubleSlash() {
        val url = runInterceptor("http://192.168.1.246:8000/")
        assertEquals("http://192.168.1.246:8000/api/bookmarks", url)
    }

    @Test
    fun testNoTrailingSlashWorksCorrectly() {
        val url = runInterceptor("http://192.168.1.246:8000")
        assertEquals("http://192.168.1.246:8000/api/bookmarks", url)
    }

    @Test
    fun testDoubleTrailingSlashIsHandled() {
        val url = runInterceptor("http://192.168.1.246:8000//")
        assertEquals("http://192.168.1.246:8000/api/bookmarks", url)
    }

    @Test
    fun testHttpsUrlWorks() {
        val url = runInterceptor("https://readeck.example.com/")
        assertEquals("https://readeck.example.com/api/bookmarks", url)
    }

    @Test
    fun testBaseUrlWithPortAndPath() {
        val url = runInterceptor("http://myserver.com:3000/custom/")
        assertEquals("http://myserver.com:3000/custom/api/bookmarks", url)
    }

    @Test(expected = IOException::class)
    fun testEmptyUrlThrows() {
        runInterceptor("")
    }

    @Test(expected = IOException::class)
    fun testNullUrlThrows() {
        runInterceptor(null)
    }
}