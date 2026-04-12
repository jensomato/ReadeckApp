package de.readeckapp.domain.usecase

import de.readeckapp.domain.BookmarkRepository
import de.readeckapp.io.prefs.SettingsDataStore
import de.readeckapp.io.rest.ReadeckApi
import de.readeckapp.io.rest.model.BookmarkDto
import de.readeckapp.io.rest.model.ImageResource
import de.readeckapp.io.rest.model.Resource
import de.readeckapp.io.rest.model.Resources
import de.readeckapp.io.rest.model.SyncInfoDto
import de.readeckapp.io.rest.model.SyncInfoType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import kotlin.time.Duration.Companion.days

class SyncBookmarksUseCaseTest {

    private lateinit var bookmarkRepository: BookmarkRepository
    private lateinit var readeckApi: ReadeckApi
    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var syncBookmarksUseCase: SyncBookmarksUseCase

    @Before
    fun setUp() {
        bookmarkRepository = mockk(relaxed = true)
        readeckApi = mockk()
        settingsDataStore = mockk(relaxed = true)
        syncBookmarksUseCase = SyncBookmarksUseCase(
            bookmarkRepository,
            readeckApi,
            mockk(relaxed = true),
            settingsDataStore
        )
    }

    @Test
    fun `execute sync with updates and deletes`() = runBlocking {
        val lastSync = Clock.System.now()
        coEvery { settingsDataStore.getLastSyncTimestamp() } returns lastSync
        
        val syncList = listOf(
            SyncInfoDto("1", Clock.System.now(), SyncInfoType.update),
            SyncInfoDto("2", Clock.System.now(), SyncInfoType.delete)
        )
        coEvery { readeckApi.getBookmarkSyncList(lastSync) } returns Response.success(syncList)
        coEvery { readeckApi.getBookmarks(any(), any(), any(), any(), listOf("1")) } returns Response.success(listOf(bookmark1))

        val result = syncBookmarksUseCase.execute()

        assertTrue(result is UseCaseResult.Success)
        coVerify { bookmarkRepository.deleteBookmarkLocal("2") }
        coVerify { bookmarkRepository.insertBookmarks(match { param -> param[0].id == "1" } ) }
    }

    val bookmark1 = BookmarkDto(
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
