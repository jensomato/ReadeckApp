package de.readeckapp.domain

import de.readeckapp.io.db.dao.BookmarkDao
import de.readeckapp.io.rest.ReadeckApi
import de.readeckapp.io.rest.model.BookmarkDto
import de.readeckapp.io.rest.model.EditBookmarkDto
import de.readeckapp.io.rest.model.EditBookmarkErrorDto
import de.readeckapp.io.rest.model.EditBookmarkResponseDto
import de.readeckapp.io.rest.model.ImageResource
import de.readeckapp.io.rest.model.Resource
import de.readeckapp.io.rest.model.Resources
import de.readeckapp.io.rest.model.StatusMessageDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import okhttp3.Headers
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException
import kotlin.time.Duration.Companion.days

@OptIn(ExperimentalCoroutinesApi::class)
class BookmarkRepositoryImplTest {

    private lateinit var bookmarkDao: BookmarkDao
    private lateinit var readeckApi: ReadeckApi
    private lateinit var json: Json
    private lateinit var bookmarkRepositoryImpl: BookmarkRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher) // Use Unconfined for immediate execution
        bookmarkDao = mockk(relaxed = true)
        readeckApi = mockk()
        json = Json { ignoreUnknownKeys = false }
        bookmarkRepositoryImpl = BookmarkRepositoryImpl(bookmarkDao, readeckApi, json, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `updateBookmark successful`() = runTest {
        // Arrange
        val bookmarkId = "123"
        val isFavorite = true
        val editBookmarkResponseDto = EditBookmarkResponseDto(
            href = "http://example.com",
            id = "123",
            isArchived = true,
            isDeleted = true,
            isMarked = true,
            labels = "label1,label2",
            readAnchor = "anchor1",
            readProgress = 50,
            title = "New Title",
            updated = Clock.System.now()
        )
        val response: Response<EditBookmarkResponseDto> = Response.success(editBookmarkResponseDto)
        coEvery { readeckApi.editBookmark(bookmarkId, EditBookmarkDto(isMarked = isFavorite)) } returns response

        // Act
        val result = bookmarkRepositoryImpl.updateBookmark(
            bookmarkId = bookmarkId,
            isFavorite = isFavorite,
            isArchived = null,
            isRead = null)

        // Assert
        assertTrue(result is BookmarkRepository.UpdateResult.Success)
    }

    @Test
    fun `updateBookmark error 422`() = runTest {
        // Arrange
        val bookmarkId = "123"
        val isFavorite = true
        val errorDto = EditBookmarkErrorDto(errors = listOf("Invalid input"))
        val errorResponse = Response.error<EditBookmarkResponseDto>(
            422,
            json.encodeToString(EditBookmarkErrorDto.serializer(), errorDto).toResponseBody()
        )
        coEvery { readeckApi.editBookmark(bookmarkId, EditBookmarkDto(isMarked = isFavorite)) } returns errorResponse

        // Act
        val result = bookmarkRepositoryImpl.updateBookmark(bookmarkId, isFavorite, isArchived = null, isRead = null)

        // Assert
        assertTrue(result is BookmarkRepository.UpdateResult.Error)
        assertEquals("[Invalid input]", (result as BookmarkRepository.UpdateResult.Error).errorMessage)
        assertEquals(422, result.code)
    }

    @Test
    fun `updateBookmark error other than 422`() = runTest {
        // Arrange
        val bookmarkId = "123"
        val isFavorite = true
        val statusMessageDto = StatusMessageDto(400, "Bad Request")
        val errorResponse = Response.error<EditBookmarkResponseDto>(
            400,
            json.encodeToString(StatusMessageDto.serializer(), statusMessageDto).toResponseBody()
        )
        coEvery { readeckApi.editBookmark(bookmarkId, EditBookmarkDto(isMarked = isFavorite)) } returns errorResponse

        // Act
        val result = bookmarkRepositoryImpl.updateBookmark(bookmarkId, isFavorite, isArchived = null, isRead = null)

        // Assert
        assertTrue(result is BookmarkRepository.UpdateResult.Error)
        assertEquals("Bad Request", (result as BookmarkRepository.UpdateResult.Error).errorMessage)
        assertEquals(400, result.code)
    }

    @Test
    fun `updateBookmark network error`() = runTest {
        // Arrange
        val bookmarkId = "123"
        val isFavorite = true
        coEvery { readeckApi.editBookmark(bookmarkId, EditBookmarkDto(isMarked = isFavorite)) } throws IOException("Network error")

        // Act
        val result = bookmarkRepositoryImpl.updateBookmark(bookmarkId, isFavorite, isArchived = null, isRead = null)

        // Assert
        assertTrue(result is BookmarkRepository.UpdateResult.NetworkError)
        assertEquals("Network error: Network error", (result as BookmarkRepository.UpdateResult.NetworkError).errorMessage)
        assertTrue(result.ex is IOException)
    }

    @Test
    fun `updateBookmark unexpected error`() = runTest {
        // Arrange
        val bookmarkId = "123"
        val isFavorite = true
        coEvery { readeckApi.editBookmark(bookmarkId, EditBookmarkDto(isMarked = isFavorite)) } throws RuntimeException("Unexpected error")

        // Act
        val result = bookmarkRepositoryImpl.updateBookmark(bookmarkId, isFavorite, isArchived = null, isRead = null)

        // Assert
        assertTrue(result is BookmarkRepository.UpdateResult.Error)
        assertEquals("An unexpected error occurred: Unexpected error", (result as BookmarkRepository.UpdateResult.Error).errorMessage)
        assertTrue(result.ex is RuntimeException)
    }

    @Test
    fun `updateBookmark error 422 serialization exception`() = runTest {
        // Arrange
        val bookmarkId = "123"
        val isFavorite = true
        val errorResponse = Response.error<EditBookmarkResponseDto>(
            422,
            "{\"invalid_json\": true}".toResponseBody()
        )
        coEvery { readeckApi.editBookmark(bookmarkId, EditBookmarkDto(isMarked = isFavorite)) } returns errorResponse

        // Act
        val result = bookmarkRepositoryImpl.updateBookmark(bookmarkId, isFavorite, isArchived = null, isRead = null)

        // Assert
        assertTrue(result is BookmarkRepository.UpdateResult.Error)
        assertTrue((result as BookmarkRepository.UpdateResult.Error).errorMessage.contains("Failed to parse error"))
        assertEquals(422, result.code)
    }

    @Test
    fun `updateBookmark other error serialization exception`() = runTest {
        // Arrange
        val bookmarkId = "123"
        val isFavorite = true
        val errorResponse = Response.error<EditBookmarkResponseDto>(
            400,
            "{\"invalid_json\": true}".toResponseBody()
        )
        coEvery { readeckApi.editBookmark(bookmarkId, EditBookmarkDto(isMarked = isFavorite)) } returns errorResponse

        // Act
        val result = bookmarkRepositoryImpl.updateBookmark(bookmarkId, isFavorite, isArchived = null, isRead = null)

        // Assert
        assertTrue(result is BookmarkRepository.UpdateResult.Error)
        assertTrue((result as BookmarkRepository.UpdateResult.Error).errorMessage!!.contains("Failed to parse error"))
        assertEquals(400, result.code)
    }

    @Test
    fun `updateBookmark error 422 empty body`() = runTest {
        // Arrange
        val bookmarkId = "123"
        val isFavorite = true
        val errorResponse = Response.error<EditBookmarkResponseDto>(
            422,
            "".toResponseBody()
        )
        coEvery { readeckApi.editBookmark(bookmarkId, EditBookmarkDto(isMarked = isFavorite)) } returns errorResponse

        // Act
        val result = bookmarkRepositoryImpl.updateBookmark(bookmarkId, isFavorite, isArchived = null, isRead = null)

        // Assert
        assertTrue(result is BookmarkRepository.UpdateResult.Error)
        assertEquals("Empty error body", (result as BookmarkRepository.UpdateResult.Error).errorMessage)
        assertEquals(422, result.code)
    }

    @Test
    fun `updateBookmark other error empty body`() = runTest {
        // Arrange
        val bookmarkId = "123"
        val isFavorite = true
        val errorResponse = Response.error<EditBookmarkResponseDto>(
            400,
            "".toResponseBody()
        )
        coEvery { readeckApi.editBookmark(bookmarkId, EditBookmarkDto(isMarked = isFavorite)) } returns errorResponse

        // Act
        val result = bookmarkRepositoryImpl.updateBookmark(bookmarkId, isFavorite, isArchived = null, isRead = null)

        // Assert
        assertTrue(result is BookmarkRepository.UpdateResult.Error)
        assertEquals("Empty error body", (result as BookmarkRepository.UpdateResult.Error).errorMessage)
        assertEquals(400, result.code)
    }

    @Test
    fun `updateBookmark isFavorite sets correct field in EditBookmarkDto`() = runTest {
        // Arrange
        val bookmarkId = "123"
        val isFavorite = true
        val response: Response<EditBookmarkResponseDto> = Response.success(editBookmarkResponseDto)
        coEvery { readeckApi.editBookmark(bookmarkId, EditBookmarkDto(isMarked = isFavorite)) } returns response

        // Act
        val result = bookmarkRepositoryImpl.updateBookmark(
            bookmarkId = bookmarkId,
            isFavorite = isFavorite,
            isArchived = null,
            isRead = null)

        // Assert
        assertTrue(result is BookmarkRepository.UpdateResult.Success)
        coVerify { readeckApi.editBookmark(bookmarkId, EditBookmarkDto(isMarked = true)) }
    }

    @Test
    fun `updateBookmark isArchived sets correct field in EditBookmarkDto`() = runTest {
        // Arrange
        val bookmarkId = "123"
        val response: Response<EditBookmarkResponseDto> = Response.success(editBookmarkResponseDto)
        coEvery { readeckApi.editBookmark(bookmarkId, EditBookmarkDto(isArchived = true)) } returns response

        // Act
        val result = bookmarkRepositoryImpl.updateBookmark(
            bookmarkId = bookmarkId,
            isFavorite = null,
            isArchived = true,
            isRead = null)

        // Assert
        assertTrue(result is BookmarkRepository.UpdateResult.Success)
        coVerify { readeckApi.editBookmark(bookmarkId, EditBookmarkDto(isArchived = true)) }
    }

    @Test
    fun `updateBookmark isRead sets correct field in EditBookmarkDto`() = runTest {
        // Arrange
        val bookmarkId = "123"
        val response: Response<EditBookmarkResponseDto> = Response.success(editBookmarkResponseDto)
        coEvery { readeckApi.editBookmark(bookmarkId, EditBookmarkDto(readProgress = 100)) } returns response

        // Act
        val result = bookmarkRepositoryImpl.updateBookmark(
            bookmarkId = bookmarkId,
            isFavorite = null,
            isArchived = null,
            isRead = true)

        // Assert
        assertTrue(result is BookmarkRepository.UpdateResult.Success)
        coVerify { readeckApi.editBookmark(bookmarkId, EditBookmarkDto(readProgress = 100)) }
    }

    @Test
    fun `deleteBookmark successful`() = runTest {
        // Arrange
        val bookmarkId = "123"
        val response: Response<Unit> = Response.success(Unit)
        coEvery { readeckApi.deleteBookmark(bookmarkId) } returns response

        // Act
        val result = bookmarkRepositoryImpl.deleteBookmark(id = bookmarkId)

        // Assert
        assertTrue(result is BookmarkRepository.UpdateResult.Success)
        coVerify { readeckApi.deleteBookmark(bookmarkId) }
    }

    @Test
    fun `deleteBookmark failure 404`() = runTest {
        // Arrange
        val bookmarkId = "123"
        val statusMessageDto = StatusMessageDto(404, "Not Found")
        val errorResponse = Response.error<Unit>(
            404,
            json.encodeToString(StatusMessageDto.serializer(), statusMessageDto).toResponseBody()
        )
        coEvery { readeckApi.deleteBookmark(bookmarkId) } returns errorResponse

        // Act
        val result = bookmarkRepositoryImpl.deleteBookmark(id = bookmarkId)

        // Assert
        assertTrue(result is BookmarkRepository.UpdateResult.Error)
        assertEquals("Not Found", (result as BookmarkRepository.UpdateResult.Error).errorMessage)
        assertEquals(404, result.code)
        coVerify { readeckApi.deleteBookmark(bookmarkId) }
    }

    @Test
    fun `performFullSync successful sync with multiple pages`() = runTest {
        // Arrange
        val pageSize = 50
        val totalCount = 120
        val totalPages = 3
        val bookmarkList1 = List(pageSize) { bookmarkDto.copy(id = "bookmark_$it") }
        val bookmarkList2 = List(pageSize) { bookmarkDto.copy(id = "bookmark_${it + pageSize}") }
        val bookmarkList3 = List(20) { bookmarkDto.copy(id = "bookmark_${it + 2 * pageSize}") }

        coEvery {
            readeckApi.getBookmarks(limit = pageSize, offset = 0, updatedSince = null, ReadeckApi.SortOrder(ReadeckApi.Sort.Created))
        } returns Response.success(bookmarkList1, Headers.headersOf(
            ReadeckApi.Header.TOTAL_COUNT, totalCount.toString(),
            ReadeckApi.Header.TOTAL_PAGES, totalPages.toString(),
            ReadeckApi.Header.CURRENT_PAGE, "1"
        ))

        coEvery {
            readeckApi.getBookmarks(limit = pageSize, offset = pageSize, updatedSince = null, ReadeckApi.SortOrder(ReadeckApi.Sort.Created))
        } returns Response.success(bookmarkList2, Headers.headersOf(
            ReadeckApi.Header.TOTAL_COUNT, totalCount.toString(),
            ReadeckApi.Header.TOTAL_PAGES, totalPages.toString(),
            ReadeckApi.Header.CURRENT_PAGE, "2"
        ))

        coEvery {
            readeckApi.getBookmarks(limit = pageSize, offset = 2 * pageSize, updatedSince = null, ReadeckApi.SortOrder(ReadeckApi.Sort.Created))
        } returns Response.success(bookmarkList3, Headers.headersOf(
            ReadeckApi.Header.TOTAL_COUNT, totalCount.toString(),
            ReadeckApi.Header.TOTAL_PAGES, totalPages.toString(),
            ReadeckApi.Header.CURRENT_PAGE, "3"
        ))

        coEvery { bookmarkDao.removeDeletedBookmars() } returns 10
        coEvery { bookmarkDao.insertRemoteBookmarkIds(any()) } returns Unit

        // Act
        val result = bookmarkRepositoryImpl.performFullSync()

        // Assert
        assertTrue(result is BookmarkRepository.SyncResult.Success)
        assertEquals(10, (result as BookmarkRepository.SyncResult.Success).countDeleted)

        coVerify { readeckApi.getBookmarks(limit = pageSize, offset = 0, updatedSince = null, ReadeckApi.SortOrder(ReadeckApi.Sort.Created)) }
        coVerify { readeckApi.getBookmarks(limit = pageSize, offset = pageSize, updatedSince = null, ReadeckApi.SortOrder(ReadeckApi.Sort.Created)) }
        coVerify { readeckApi.getBookmarks(limit = pageSize, offset = 2 * pageSize, updatedSince = null, ReadeckApi.SortOrder(ReadeckApi.Sort.Created)) }
        coVerify { bookmarkDao.insertRemoteBookmarkIds(any()) }
        coVerify { bookmarkDao.removeDeletedBookmars() }
        coVerify { bookmarkDao.clearRemoteBookmarkIds() }
    }

    @Test
    fun `performFullSync API error`() = runTest {
        // Arrange
        coEvery { readeckApi.getBookmarks(limit = any(), offset = any(), updatedSince = any(), ReadeckApi.SortOrder(ReadeckApi.Sort.Created)) } returns Response.error(500, "Error".toResponseBody())

        // Act
        val result = bookmarkRepositoryImpl.performFullSync()

        // Assert
        assertTrue(result is BookmarkRepository.SyncResult.Error)
        assertEquals("Full sync failed", (result as BookmarkRepository.SyncResult.Error).errorMessage)
        assertEquals(500, result.code)
    }

    @Test
    fun `performFullSync missing headers`() = runTest {
        // Arrange
        coEvery { readeckApi.getBookmarks(limit = any(), offset = any(), updatedSince = any(), ReadeckApi.SortOrder(ReadeckApi.Sort.Created)) } returns Response.success(emptyList())

        // Act
        val result = bookmarkRepositoryImpl.performFullSync()

        // Assert
        assertTrue(result is BookmarkRepository.SyncResult.Error)
        assertEquals("Missing headers in API response", (result as BookmarkRepository.SyncResult.Error).errorMessage)
    }

    @Test
    fun `performFullSync network error`() = runTest {
        // Arrange
        coEvery { readeckApi.getBookmarks(limit = any(), offset = any(), updatedSince = any(), ReadeckApi.SortOrder(ReadeckApi.Sort.Created)) } throws IOException("Network error")

        // Act
        val result = bookmarkRepositoryImpl.performFullSync()

        // Assert
        assertTrue(result is BookmarkRepository.SyncResult.NetworkError)
        assertEquals("Network error during full sync", (result as BookmarkRepository.SyncResult.NetworkError).errorMessage)
        assertTrue((result as BookmarkRepository.SyncResult.NetworkError).ex is IOException)
    }

    private val editBookmarkResponseDto = EditBookmarkResponseDto(
        href = "http://example.com",
        id = "123",
        isArchived = true,
        isDeleted = true,
        isMarked = true,
        labels = "label1,label2",
        readAnchor = "anchor1",
        readProgress = 50,
        title = "New Title",
        updated = Clock.System.now()
    )

    val bookmarkDto = BookmarkDto(
        id = "1",
        href = "https://example.com",
        created = Clock.System.now().minus(1.days),
        updated = Clock.System.now().minus(1.days),
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
