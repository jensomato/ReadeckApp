package de.readeckapp.ui.list

import android.content.Context
import de.readeckapp.domain.BookmarkRepository
import de.readeckapp.domain.model.Bookmark
import de.readeckapp.io.prefs.SettingsDataStore
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookmarkListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var bookmarkRepository: BookmarkRepository
    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var context: Context
    private lateinit var viewModel: BookmarkListViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        bookmarkRepository = mockk()
        settingsDataStore = mockk()
        context = mockk()

        // Default Mocking Behavior
        coEvery { settingsDataStore.isInitialSyncPerformed() } returns true // Assume sync is done
        every { bookmarkRepository.observeBookmarks(any(), any(), any(), any()) } returns flowOf(
            emptyList()
        ) // No bookmarks initially
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState is Loading`() {
        coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
        viewModel = BookmarkListViewModel(bookmarkRepository, context, settingsDataStore)
        assertEquals(BookmarkListViewModel.UiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `loadBookmarks enqueues LoadBookmarksWorker`() = runTest {
        // TODO: Find a way to properly test WorkManager enqueuing
        //  This requires more setup with Robolectric and testing WorkManager
        coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
        viewModel = BookmarkListViewModel(bookmarkRepository, context, settingsDataStore)
        // Just verify that it doesn't throw an exception for now
        viewModel.loadBookmarks()
    }

    @Test
    fun `onClickAll clears filters`() = runTest {
        coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
        viewModel = BookmarkListViewModel(bookmarkRepository, context, settingsDataStore)
        viewModel.onClickAll()
        assertEquals(BookmarkListViewModel.FilterState(), viewModel.filterState.first())
    }

    @Test
    fun `onClickUnread sets unread filter`() = runTest {
        coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
        viewModel = BookmarkListViewModel(bookmarkRepository, context, settingsDataStore)
        viewModel.onClickUnread()
        assertEquals(
            BookmarkListViewModel.FilterState(unread = true),
            viewModel.filterState.first()
        )
    }

    @Test
    fun `onClickArchive sets archived filter`() = runTest {
        coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
        viewModel = BookmarkListViewModel(bookmarkRepository, context, settingsDataStore)
        viewModel.onClickArchive()
        assertEquals(
            BookmarkListViewModel.FilterState(archived = true),
            viewModel.filterState.first()
        )
    }

    @Test
    fun `onClickFavorite sets favorite filter`() = runTest {
        coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
        viewModel = BookmarkListViewModel(bookmarkRepository, context, settingsDataStore)
        viewModel.onClickFavorite()
        assertEquals(
            BookmarkListViewModel.FilterState(favorite = true),
            viewModel.filterState.first()
        )
    }

    @Test
    fun `onClickArticles sets type filter to Article`() = runTest {
        coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
        viewModel = BookmarkListViewModel(bookmarkRepository, context, settingsDataStore)
        viewModel.onClickArticles()
        assertEquals(
            BookmarkListViewModel.FilterState(type = Bookmark.Type.Article),
            viewModel.filterState.first()
        )
    }

    @Test
    fun `onClickPictures sets type filter to Picture`() = runTest {
        coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
        viewModel = BookmarkListViewModel(bookmarkRepository, context, settingsDataStore)
        viewModel.onClickPictures()
        assertEquals(
            BookmarkListViewModel.FilterState(type = Bookmark.Type.Picture),
            viewModel.filterState.first()
        )
    }

    @Test
    fun `onClickVideos sets type filter to Video`() = runTest {
        coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
        viewModel = BookmarkListViewModel(bookmarkRepository, context, settingsDataStore)
        viewModel.onClickVideos()
        assertEquals(
            BookmarkListViewModel.FilterState(type = Bookmark.Type.Video),
            viewModel.filterState.first()
        )
    }

    @Test
    fun `onClickSettings sets NavigateToSettings navigation event`() = runTest {
        coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
        viewModel = BookmarkListViewModel(bookmarkRepository, context, settingsDataStore)
        viewModel.onClickSettings()
        assertEquals(
            BookmarkListViewModel.NavigationEvent.NavigateToSettings,
            viewModel.navigationEvent.first()
        )
    }

    @Test
    fun `onClickBookmark sets NavigateToBookmarkDetail navigation event`() = runTest {
        coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
        viewModel = BookmarkListViewModel(bookmarkRepository, context, settingsDataStore)
        val bookmarkId = "someBookmarkId"
        viewModel.onClickBookmark(bookmarkId)
        assertEquals(
            BookmarkListViewModel.NavigationEvent.NavigateToBookmarkDetail(bookmarkId),
            viewModel.navigationEvent.first()
        )
    }

    @Test
    fun `onNavigationEventConsumed resets navigation event`() = runTest {
        coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
        viewModel = BookmarkListViewModel(bookmarkRepository, context, settingsDataStore)
        viewModel.onClickSettings() // Set a navigation event
        viewModel.onNavigationEventConsumed()
        assertEquals(null, viewModel.navigationEvent.first())
    }

    @Test
    fun `observeBookmarks collects bookmarks with correct filters`() = runTest {
        // Arrange
        val expectedBookmarks = listOf(
            Bookmark(
                id = "1",
                href = "https://example.com",
                created = kotlinx.datetime.LocalDateTime(2024, 1, 1, 0, 0),
                updated = kotlinx.datetime.LocalDateTime(2024, 1, 1, 0, 0),
                state = 0,
                loaded = true,
                url = "https://example.com",
                title = "Test Bookmark",
                siteName = "Example Site",
                site = "example.com",
                authors = listOf("Author 1", "Author 2"),
                lang = "en",
                textDirection = "ltr",
                documentTpe = "article",
                type = Bookmark.Type.Article,
                hasArticle = true,
                description = "Test Description",
                isDeleted = false,
                isMarked = false,
                isArchived = false,
                labels = emptyList(),
                readProgress = 0,
                wordCount = 0,
                readingTime = 0,
                article = Bookmark.Resource(""),
                articleContent = "Test Article Content",
                icon = Bookmark.ImageResource("", 0, 0),
                image = Bookmark.ImageResource("", 0, 0),
                log = Bookmark.Resource(""),
                props = Bookmark.Resource(""),
                thumbnail = Bookmark.ImageResource("", 0, 0)
            )
        )
        val bookmarkFlow = MutableStateFlow(expectedBookmarks)
        coEvery {
            bookmarkRepository.observeBookmarks(
                type = Bookmark.Type.Article,
                unread = true,
                archived = null,
                favorite = null
            )
        } returns bookmarkFlow

        coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
        viewModel = BookmarkListViewModel(bookmarkRepository, context, settingsDataStore)

        viewModel.onClickArticles()
        viewModel.onClickUnread()

        val uiStates = viewModel.uiState.take(2).toList()
        val loading = uiStates[0]
        val success = uiStates[1]
        // Assert initial state
        assert(loading is BookmarkListViewModel.UiState.Loading)
        // Assert success state
        assertEquals(
            BookmarkListViewModel.UiState.Success(expectedBookmarks),
            success
        )
    }
}
