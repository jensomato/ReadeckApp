package de.readeckapp.domain.usecase

import de.readeckapp.domain.BookmarkRepository
import de.readeckapp.io.prefs.SettingsDataStore
import de.readeckapp.io.rest.ReadeckApi
import de.readeckapp.io.rest.model.BookmarkDto
import de.readeckapp.io.rest.model.ImageResource
import de.readeckapp.io.rest.model.Resource
import de.readeckapp.io.rest.model.Resources
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import okhttp3.Headers
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import timber.log.Timber

class LoadBookmarksUseCaseTest {

    private lateinit var bookmarkRepository: BookmarkRepository
    private lateinit var readeckApi: ReadeckApi
    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var loadBookmarksUseCase: LoadBookmarksUseCase

    @Before
    fun setUp() {
        bookmarkRepository = mockk(relaxed = true)
        readeckApi = mockk()
        settingsDataStore = mockk(relaxed = true)
        loadBookmarksUseCase = LoadBookmarksUseCase(
            bookmarkRepository,
            readeckApi,
            mockk(relaxed = true),
            settingsDataStore
        )
    }

    @Test
    fun `execute successful load`() = runBlocking {
        // Mock API response
        val bookmarkDtoList = listOf(sampleBookmarkDto)
        val response: Response<List<BookmarkDto>> = Response.success(
            bookmarkDtoList,
            Headers.headersOf(
                ReadeckApi.Header.TOTAL_COUNT,
                "1",
                ReadeckApi.Header.TOTAL_PAGES,
                "1",
                ReadeckApi.Header.CURRENT_PAGE,
                "1"
            )
        )
        coEvery { readeckApi.getBookmarks(any(), any(), any(), any()) } returns response

        // Mock SettingsDataStore
        coEvery { settingsDataStore.getLastBookmarkTimestamp() } returns null

        // Execute the use case
        val result = loadBookmarksUseCase.execute(10, 0)

        println("result=$result")
        // Verify the result
        assertTrue(result is LoadBookmarksUseCase.UseCaseResult.Success<*>)
        // Add more specific assertions based on your logic
    }

    @Test
    fun `execute api error`() = runBlocking {
        // Mock API response to return an error
        val response: Response<List<BookmarkDto>> = Response.error(500, mockk(relaxed = true))
        coEvery { readeckApi.getBookmarks(any(), any(), any(), any()) } returns response

        // Execute the use case
        val result = loadBookmarksUseCase.execute(10, 0)

        // Verify the result
        assertTrue(result is LoadBookmarksUseCase.UseCaseResult.Error)
        // Add more specific assertions based on your logic
    }

    @Test
    fun `execute exception thrown`() = runBlocking {
        // Mock API to throw an exception
        coEvery {
            readeckApi.getBookmarks(
                any(),
                any(),
                any(),
                any(),
            )
        } throws RuntimeException("Test Exception")

        // Execute the use case
        val result = loadBookmarksUseCase.execute(10, 0)

        // Verify the result
        assertTrue(result is LoadBookmarksUseCase.UseCaseResult.Error)
        // Add more specific assertions based on your logic
    }

    // Add more tests to cover different scenarios:
    // - Pagination
    // - Saving last loaded timestamp
    // - Handling empty API response
    // - etc.

    val sampleBookmarkDto = BookmarkDto(
        id = "123",
        href = "https://example.com",
        created = Clock.System.now(),
        updated = Clock.System.now(),
        state = 1,
        loaded = true,
        url = "https://example.com/article",
        title = "Sample Article",
        siteName = "Example Site",
        site = "example.com",
        authors = listOf("John Doe", "Jane Smith"),
        lang = "en",
        textDirection = "ltr",
        documentTpe = "article",
        type = "article",
        hasArticle = true,
        description = "This is a sample article description.",
        isDeleted = false,
        isMarked = false,
        isArchived = false,
        labels = listOf("sample", "article"),
        readProgress = 0,
        resources = Resources(
            article = Resource(src = "https://example.com/article.pdf"),
            icon = ImageResource(src = "https://example.com/icon.png", width = 32, height = 32),
            image = ImageResource(src = "https://example.com/image.jpg", width = 600, height = 400),
            log = Resource(src = "https://example.com/log.txt"),
            props = Resource(src = "https://example.com/props.json"),
            thumbnail = ImageResource(
                src = "https://example.com/thumbnail.jpg",
                width = 200,
                height = 150
            )
        ),
        wordCount = 1000,
        readingTime = 5
    )
}
