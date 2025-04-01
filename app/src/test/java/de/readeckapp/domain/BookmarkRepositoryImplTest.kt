package de.readeckapp.domain

import de.readeckapp.io.db.dao.BookmarkDao
import de.readeckapp.io.rest.ReadeckApi
import de.readeckapp.io.rest.model.EditBookmarkDto
import de.readeckapp.io.rest.model.EditBookmarkErrorDto
import de.readeckapp.io.rest.model.EditBookmarkResponseDto
import de.readeckapp.io.rest.model.StatusMessageDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException

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
        val result = bookmarkRepositoryImpl.updateBookmark(bookmarkId, isFavorite)

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
        val result = bookmarkRepositoryImpl.updateBookmark(bookmarkId, isFavorite)

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
        val result = bookmarkRepositoryImpl.updateBookmark(bookmarkId, isFavorite)

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
        val result = bookmarkRepositoryImpl.updateBookmark(bookmarkId, isFavorite)

        // Assert
        assertTrue(result is BookmarkRepository.UpdateResult.Error)
        assertEquals("Network error: Network error", (result as BookmarkRepository.UpdateResult.Error).errorMessage)
        assertTrue(result.ex is IOException)
    }

    @Test
    fun `updateBookmark unexpected error`() = runTest {
        // Arrange
        val bookmarkId = "123"
        val isFavorite = true
        coEvery { readeckApi.editBookmark(bookmarkId, EditBookmarkDto(isMarked = isFavorite)) } throws RuntimeException("Unexpected error")

        // Act
        val result = bookmarkRepositoryImpl.updateBookmark(bookmarkId, isFavorite)

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
        val result = bookmarkRepositoryImpl.updateBookmark(bookmarkId, isFavorite)

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
        val result = bookmarkRepositoryImpl.updateBookmark(bookmarkId, isFavorite)

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
        val result = bookmarkRepositoryImpl.updateBookmark(bookmarkId, isFavorite)

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
        val result = bookmarkRepositoryImpl.updateBookmark(bookmarkId, isFavorite)

        // Assert
        assertTrue(result is BookmarkRepository.UpdateResult.Error)
        assertEquals("Empty error body", (result as BookmarkRepository.UpdateResult.Error).errorMessage)
        assertEquals(400, result.code)
    }
}
