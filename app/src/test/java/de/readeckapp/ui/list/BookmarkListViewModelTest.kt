package de.readeckapp.ui.list

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.work.WorkManager
import de.readeckapp.R
import de.readeckapp.domain.BookmarkRepository
import de.readeckapp.domain.model.Bookmark
import de.readeckapp.domain.model.BookmarkListItem
import de.readeckapp.domain.usecase.UpdateBookmarkUseCase
import de.readeckapp.io.prefs.SettingsDataStore
import io.mockk.coEvery
import io.mockk.coVerify
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
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookmarkListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var bookmarkRepository: BookmarkRepository
    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var context: Context
    private lateinit var viewModel: BookmarkListViewModel
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var updateBookmarkUseCase: UpdateBookmarkUseCase
    private lateinit var workManager : WorkManager

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        bookmarkRepository = mockk()
        settingsDataStore = mockk()
        context = mockk()
        savedStateHandle = mockk()
        updateBookmarkUseCase = mockk()
        workManager = mockk()

        // Default Mocking Behavior
        coEvery { settingsDataStore.isInitialSyncPerformed() } returns true // Assume sync is done
        every { bookmarkRepository.observeBookmarkListItems(any(), any(), any(), any(), any()) } returns flowOf(
            emptyList()
        ) // No bookmarks initially
        every { savedStateHandle.get<String>(any()) } returns null // no sharedUrl initially
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState is Empty`() {
        coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
        viewModel = BookmarkListViewModel(
            updateBookmarkUseCase,
            workManager,
            bookmarkRepository,
            context,
            settingsDataStore,
            savedStateHandle,
        )
        assertEquals(BookmarkListViewModel.UiState.Empty(R.string.list_view_empty_not_loaded_yet), viewModel.uiState.value)
    }

    @Test
    fun `loadBookmarks enqueues LoadBookmarksWorker`() = runTest {
        // TODO: Find a way to properly test WorkManager enqueuing
        //  This requires more setup with Robolectric and testing WorkManager
        coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
        viewModel = BookmarkListViewModel(
            updateBookmarkUseCase,
            workManager,
            bookmarkRepository,
            context,
            settingsDataStore,
            savedStateHandle
        )
        // Just verify that it doesn't throw an exception for now
        viewModel.onClickLoadBookmarks()
    }

    @Test
    fun `onClickAll clears filters`() = runTest {
        coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
        viewModel = BookmarkListViewModel(
            updateBookmarkUseCase,
            workManager,
            bookmarkRepository,
            context,
            settingsDataStore,
            savedStateHandle
        )
        viewModel.onClickAll()
        assertEquals(BookmarkListViewModel.FilterState(), viewModel.filterState.first())
    }

    @Test
    fun `onClickUnread sets unread filter`() = runTest {
        coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
        viewModel = BookmarkListViewModel(
            updateBookmarkUseCase,
            workManager,
            bookmarkRepository,
            context,
            settingsDataStore,
            savedStateHandle
        )
        viewModel.onClickUnread()
        assertEquals(
            BookmarkListViewModel.FilterState(unread = true),
            viewModel.filterState.first()
        )
    }

    @Test
    fun `onClickArchive sets archived filter`() = runTest {
        coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
        viewModel = BookmarkListViewModel(
            updateBookmarkUseCase,
            workManager,
            bookmarkRepository,
            context,
            settingsDataStore,
            savedStateHandle
        )
        viewModel.onClickArchive()
        assertEquals(
            BookmarkListViewModel.FilterState(archived = true),
            viewModel.filterState.first()
        )
    }

    @Test
    fun `onClickFavorite sets favorite filter`() = runTest {
        coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
        viewModel = BookmarkListViewModel(
            updateBookmarkUseCase,
            workManager,
            bookmarkRepository,
            context,
            settingsDataStore,
            savedStateHandle
        )
        viewModel.onClickFavorite()
        assertEquals(
            BookmarkListViewModel.FilterState(favorite = true),
            viewModel.filterState.first()
        )
    }

    @Test
    fun `onClickArticles sets type filter to Article`() = runTest {
        coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
        viewModel = BookmarkListViewModel(
            updateBookmarkUseCase,
            workManager,
            bookmarkRepository,
            context,
            settingsDataStore,
            savedStateHandle
        )
        viewModel.onClickArticles()
        assertEquals(
            BookmarkListViewModel.FilterState(type = Bookmark.Type.Article),
            viewModel.filterState.first()
        )
    }

    @Test
    fun `onClickPictures sets type filter to Picture`() = runTest {
        coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
        viewModel = BookmarkListViewModel(
            updateBookmarkUseCase,
            workManager,
            bookmarkRepository,
            context,
            settingsDataStore,
            savedStateHandle
        )
        viewModel.onClickPictures()
        assertEquals(
            BookmarkListViewModel.FilterState(type = Bookmark.Type.Picture),
            viewModel.filterState.first()
        )
    }

    @Test
    fun `onClickVideos sets type filter to Video`() = runTest {
        coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
        viewModel = BookmarkListViewModel(
            updateBookmarkUseCase,
            workManager,
            bookmarkRepository,
            context,
            settingsDataStore,
            savedStateHandle
        )
        viewModel.onClickVideos()
        assertEquals(
            BookmarkListViewModel.FilterState(type = Bookmark.Type.Video),
            viewModel.filterState.first()
        )
    }

    @Test
    fun `onClickSettings sets NavigateToSettings navigation event`() = runTest {
        coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
        viewModel = BookmarkListViewModel(
            updateBookmarkUseCase,
            workManager,
            bookmarkRepository,
            context,
            settingsDataStore,
            savedStateHandle
        )
        viewModel.onClickSettings()
        assertEquals(
            BookmarkListViewModel.NavigationEvent.NavigateToSettings,
            viewModel.navigationEvent.first()
        )
    }

    @Test
    fun `onClickBookmark sets NavigateToBookmarkDetail navigation event`() = runTest {
        coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
        viewModel = BookmarkListViewModel(
            updateBookmarkUseCase,
            workManager,
            bookmarkRepository,
            context,
            settingsDataStore,
            savedStateHandle
        )
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
        viewModel = BookmarkListViewModel(
            updateBookmarkUseCase,
            workManager,
            bookmarkRepository,
            context,
            settingsDataStore,
            savedStateHandle
        )
        viewModel.onClickSettings() // Set a navigation event
        viewModel.onNavigationEventConsumed()
        assertEquals(null, viewModel.navigationEvent.first())
    }

    @Test
    fun `observeBookmarks collects bookmarks with correct filters`() = runTest {
        // Arrange
        val expectedBookmarks = listOf(
            BookmarkListItem(
                id = "1",
                url = "https://example.com",
                title = "Test Bookmark",
                siteName = "Example Site",
                type = Bookmark.Type.Article,
                isMarked = false,
                isArchived = false,
                labels = emptyList(),
                isRead = false,
                thumbnailSrc = "",
                iconSrc = "",
                imageSrc = ""
            )
        )
        val bookmarkFlow = MutableStateFlow(expectedBookmarks)
        coEvery {
            bookmarkRepository.observeBookmarkListItems(
                type = Bookmark.Type.Article,
                unread = true,
                archived = null,
                favorite = null,
                state = Bookmark.State.LOADED
            )
        } returns bookmarkFlow

        coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
        viewModel = BookmarkListViewModel(
            updateBookmarkUseCase,
            workManager,
            bookmarkRepository,
            context,
            settingsDataStore,
            savedStateHandle
        )

        viewModel.onClickArticles()
        viewModel.onClickUnread()

        val uiStates = viewModel.uiState.take(2).toList()
        val empty = uiStates[0]
        val success = uiStates[1]
        // Assert initial state
        assert(empty is BookmarkListViewModel.UiState.Empty)
        // Assert success state
        assertEquals(
            BookmarkListViewModel.UiState.Success(expectedBookmarks, null),
            success
        )
    }

    @Test
    fun `openCreateBookmarkDialog sets CreateBookmarkUiState to Open`() = runTest {
        coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
        viewModel = BookmarkListViewModel(
            updateBookmarkUseCase,
            workManager,
            bookmarkRepository,
            context,
            settingsDataStore,
            savedStateHandle
        )
        viewModel.openCreateBookmarkDialog()
        assertTrue(viewModel.createBookmarkUiState.first() is BookmarkListViewModel.CreateBookmarkUiState.Open)
    }

    @Test
    fun `closeCreateBookmarkDialog sets CreateBookmarkUiState to Closed`() = runTest {
        coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
        viewModel = BookmarkListViewModel(
            updateBookmarkUseCase,
            workManager,
            bookmarkRepository,
            context,
            settingsDataStore,
            savedStateHandle
        )
        viewModel.openCreateBookmarkDialog()
        viewModel.closeCreateBookmarkDialog()
        assertTrue(viewModel.createBookmarkUiState.first() is BookmarkListViewModel.CreateBookmarkUiState.Closed)
    }

    @Test
    fun `updateCreateBookmarkTitle updates title and enables create button if URL is valid`() =
        runTest {
            coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
            viewModel = BookmarkListViewModel(
                updateBookmarkUseCase,
                workManager,
                bookmarkRepository,
                context,
                settingsDataStore,
                savedStateHandle
            )
            viewModel.openCreateBookmarkDialog()

            val validUrl = "https://example.com"
            viewModel.updateCreateBookmarkUrl(validUrl)
            viewModel.updateCreateBookmarkTitle("Test Title")

            val state =
                viewModel.createBookmarkUiState.first() as BookmarkListViewModel.CreateBookmarkUiState.Open
            assertEquals("Test Title", state.title)
            assertEquals(validUrl, state.url)
            assertTrue(state.isCreateEnabled)
        }

    @Test
    fun `updateCreateBookmarkUrl updates url and enables create button if title is present`() =
        runTest {
            coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
            viewModel = BookmarkListViewModel(
                updateBookmarkUseCase,
                workManager,
                bookmarkRepository,
                context,
                settingsDataStore,
                savedStateHandle
            )
            viewModel.openCreateBookmarkDialog()

            viewModel.updateCreateBookmarkTitle("Test Title")
            val validUrl = "https://example.com"
            viewModel.updateCreateBookmarkUrl(validUrl)

            val state =
                viewModel.createBookmarkUiState.first() as BookmarkListViewModel.CreateBookmarkUiState.Open
            assertEquals("Test Title", state.title)
            assertEquals(validUrl, state.url)
            assertTrue(state.isCreateEnabled)
        }

    @Test
    fun `updateCreateBookmarkUrl updates urlError if URL is invalid`() = runTest {
        coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
        viewModel = BookmarkListViewModel(
            updateBookmarkUseCase,
            workManager,
            bookmarkRepository,
            context,
            settingsDataStore,
            savedStateHandle
        )
        viewModel.openCreateBookmarkDialog()

        val invalidUrl = "invalid-url"
        viewModel.updateCreateBookmarkUrl(invalidUrl)

        val state =
            viewModel.createBookmarkUiState.first() as BookmarkListViewModel.CreateBookmarkUiState.Open
        assertEquals(R.string.account_settings_url_error, state.urlError)
        assertFalse(state.isCreateEnabled)
    }

    @Test
    fun `createBookmark calls repository and sets state to Success`() = runTest {
        coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
        viewModel = BookmarkListViewModel(
            updateBookmarkUseCase,
            workManager,
            bookmarkRepository,
            context,
            settingsDataStore,
            savedStateHandle
        )
        viewModel.openCreateBookmarkDialog()

        val title = "Test Title"
        val url = "https://example.com"
        coEvery { bookmarkRepository.createBookmark(title, url) } returns "bookmark123"

        viewModel.updateCreateBookmarkTitle(title)
        viewModel.updateCreateBookmarkUrl(url)
        viewModel.createBookmark()
        runCurrent()

        coVerify { bookmarkRepository.createBookmark(title, url) }
        println("state=${viewModel.createBookmarkUiState.value}")
        assertTrue(viewModel.createBookmarkUiState.value is BookmarkListViewModel.CreateBookmarkUiState.Success)
    }

    @Test
    fun `createBookmark sets state to Error if repository call fails`() = runTest {
        coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
        viewModel = BookmarkListViewModel(
            updateBookmarkUseCase,
            workManager,
            bookmarkRepository,
            context,
            settingsDataStore,
            savedStateHandle
        )
        viewModel.openCreateBookmarkDialog()

        val title = "Test Title"
        val url = "https://example.com"
        val errorMessage = "Failed to create bookmark"
        coEvery { bookmarkRepository.createBookmark(title, url) } throws Exception(errorMessage)

        viewModel.updateCreateBookmarkTitle(title)
        viewModel.updateCreateBookmarkUrl(url)
        viewModel.createBookmark()

        val uiStates = viewModel.createBookmarkUiState.take(2).toList()
        assertTrue(uiStates[1] is BookmarkListViewModel.CreateBookmarkUiState.Error)
        assertEquals(
            errorMessage,
            (uiStates[1] as BookmarkListViewModel.CreateBookmarkUiState.Error).message
        )
    }

    @Test
    fun `init sets CreateBookmarkUiState to Open with sharedUrl if present and valid`() = runTest {
        val sharedUrl = "https://example.com"
        every { savedStateHandle.get<String>("sharedUrl") } returns sharedUrl

        coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
        viewModel = BookmarkListViewModel(
            updateBookmarkUseCase,
            workManager,
            bookmarkRepository,
            context,
            settingsDataStore,
            savedStateHandle
        )

        val state =
            viewModel.createBookmarkUiState.first() as BookmarkListViewModel.CreateBookmarkUiState.Open
        assertEquals(sharedUrl, state.url)
        assertEquals(null, state.urlError)
        assertTrue(state.isCreateEnabled)
    }

    @Test
    fun `init sets CreateBookmarkUiState to Open with sharedUrl and urlError if present and invalid`() =
        runTest {
            val sharedUrl = "invalid-url"
            every { savedStateHandle.get<String>("sharedUrl") } returns sharedUrl

            coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
            viewModel = BookmarkListViewModel(
                updateBookmarkUseCase,
                workManager,
                bookmarkRepository,
                context,
                settingsDataStore,
                savedStateHandle
            )

            val state =
                viewModel.createBookmarkUiState.first() as BookmarkListViewModel.CreateBookmarkUiState.Open
            assertEquals(sharedUrl, state.url)
            assertEquals(R.string.account_settings_url_error, state.urlError)
            assertFalse(state.isCreateEnabled)
        }

    @Test
    fun `onToggleFavoriteBookmark updates UiState with UpdateBookmarkState Success`() =
        runTest {
            coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
            val bookmarkId = "123"
            val isFavorite = true

            val bookmarkFlow = MutableStateFlow(bookmarks)
            coEvery {
                bookmarkRepository.observeBookmarkListItems(
                    type = null,
                    unread = null,
                    archived = null,
                    favorite = null,
                    state = Bookmark.State.LOADED
                )
            } returns bookmarkFlow

            coEvery {
                updateBookmarkUseCase.updateIsFavorite(
                    bookmarkId,
                    isFavorite
                )
            } returns UpdateBookmarkUseCase.Result.Success

            viewModel = BookmarkListViewModel(
                updateBookmarkUseCase,
                workManager,
                bookmarkRepository,
                context,
                settingsDataStore,
                savedStateHandle
            )

            val uiStates = viewModel.uiState.take(2).toList()
            val emptyState = uiStates[0]
            val successState = uiStates[1]
            // Assert initial state
            assert(emptyState is BookmarkListViewModel.UiState.Empty)
            // Assert success state
            assertEquals(
                BookmarkListViewModel.UiState.Success(
                    bookmarks,
                    null
                ),
                successState
            )

            viewModel.onToggleFavoriteBookmark(bookmarkId, isFavorite)
            advanceUntilIdle()

            val updateState = viewModel.uiState.value

            assertEquals(
                BookmarkListViewModel.UiState.Success(
                    bookmarks,
                    BookmarkListViewModel.UpdateBookmarkState.Success
                ),
                updateState
            )

            coVerify { updateBookmarkUseCase.updateIsFavorite(bookmarkId, isFavorite) }
        }

    @Test
    fun `onToggleFavoriteBookmark updates UiState with UpdateBookmarkState Error on GenericError`() =
        runTest {
            coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
            val bookmarkId = "123"
            val isFavorite = true
            val errorMessage = "Generic Error"

            coEvery {
                updateBookmarkUseCase.updateIsFavorite(
                    bookmarkId,
                    isFavorite
                )
            } returns UpdateBookmarkUseCase.Result.GenericError(errorMessage)

            val bookmarkFlow = MutableStateFlow(bookmarks)
            coEvery {
            bookmarkRepository.observeBookmarkListItems(
                    type = null,
                    unread = null,
                    archived = null,
                    favorite = null,
                    state = Bookmark.State.LOADED
                )
            } returns bookmarkFlow

            viewModel = BookmarkListViewModel(
                updateBookmarkUseCase,
                workManager,
                bookmarkRepository,
                context,
                settingsDataStore,
                savedStateHandle
            )

            val uiStates = viewModel.uiState.take(2).toList()
            val emptyState = uiStates[0]
            val successState = uiStates[1]
            // Assert initial state
            assert(emptyState is BookmarkListViewModel.UiState.Empty)
            // Assert success state
            assertEquals(
                BookmarkListViewModel.UiState.Success(
                    bookmarks,
                    null
                ),
                successState
            )

            viewModel.onToggleFavoriteBookmark(bookmarkId, isFavorite)
            advanceUntilIdle()

            val errorState = viewModel.uiState.value

            assertEquals(
                BookmarkListViewModel.UiState.Success(
                    bookmarks,
                    BookmarkListViewModel.UpdateBookmarkState.Error(errorMessage)
                ),
                errorState
            )

            coVerify { updateBookmarkUseCase.updateIsFavorite(bookmarkId, isFavorite) }
        }

    @Test
    fun `onToggleFavoriteBookmark updates UiState with UpdateBookmarkState Error on NetworkError`() =
        runTest {
            coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
            val bookmarkId = "123"
            val isFavorite = true
            val errorMessage = "Network Error"

            val bookmarkFlow = MutableStateFlow(bookmarks)
            coEvery {
                bookmarkRepository.observeBookmarkListItems(
                    type = null,
                    unread = null,
                    archived = null,
                    favorite = null,
                    state = Bookmark.State.LOADED
                )
            } returns bookmarkFlow

            coEvery {
                updateBookmarkUseCase.updateIsFavorite(
                    bookmarkId,
                    isFavorite
                )
            } returns UpdateBookmarkUseCase.Result.NetworkError(errorMessage)

            viewModel = BookmarkListViewModel(
                updateBookmarkUseCase,
                workManager,
                bookmarkRepository,
                context,
                settingsDataStore,
                savedStateHandle
            )

            val uiStates = viewModel.uiState.take(2).toList()
            val emptyState = uiStates[0]
            val successState = uiStates[1]
            // Assert initial state
            assert(emptyState is BookmarkListViewModel.UiState.Empty)
            // Assert success state
            assertEquals(
                BookmarkListViewModel.UiState.Success(
                    bookmarks,
                    null
                ),
                successState
            )

            viewModel.onToggleFavoriteBookmark(bookmarkId, isFavorite)
            advanceUntilIdle()

            val errorState = viewModel.uiState.value

            assertEquals(
                BookmarkListViewModel.UiState.Success(
                    bookmarks,
                    BookmarkListViewModel.UpdateBookmarkState.Error(errorMessage)
                ),
                errorState
            )

            coVerify { updateBookmarkUseCase.updateIsFavorite(bookmarkId, isFavorite) }
        }

    @Test
    fun `onToggleArchiveBookmark updates UiState with UpdateBookmarkState Success`() =
        runTest {
            coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
            val bookmarkId = "123"
            val isArchived = true

            val bookmarkFlow = MutableStateFlow(bookmarks)
            coEvery {
                bookmarkRepository.observeBookmarkListItems(
                    type = null,
                    unread = null,
                    archived = null,
                    favorite = null,
                    state = Bookmark.State.LOADED
                )
            } returns bookmarkFlow

            coEvery {
                updateBookmarkUseCase.updateIsArchived(
                    bookmarkId,
                    isArchived
                )
            } returns UpdateBookmarkUseCase.Result.Success

            viewModel = BookmarkListViewModel(
                updateBookmarkUseCase,
                workManager,
                bookmarkRepository,
                context,
                settingsDataStore,
                savedStateHandle
            )

            val uiStates = viewModel.uiState.take(2).toList()
            val emptyState = uiStates[0]
            val successState = uiStates[1]
            // Assert initial state
            assert(emptyState is BookmarkListViewModel.UiState.Empty)
            // Assert success state
            assertEquals(
                BookmarkListViewModel.UiState.Success(
                    bookmarks,
                    null
                ),
                successState
            )

            viewModel.onToggleArchiveBookmark(bookmarkId, isArchived)
            advanceUntilIdle()

            val updateState = viewModel.uiState.value

            assertEquals(
                BookmarkListViewModel.UiState.Success(
                    bookmarks,
                    BookmarkListViewModel.UpdateBookmarkState.Success
                ),
                updateState
            )

            coVerify { updateBookmarkUseCase.updateIsArchived(bookmarkId, isArchived) }
        }

    @Test
    fun `onToggleArchivedBookmark updates UiState with UpdateBookmarkState Error on GenericError`() =
        runTest {
            coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
            val bookmarkId = "123"
            val isArchived = true
            val errorMessage = "Generic Error"

            coEvery {
                updateBookmarkUseCase.updateIsArchived(
                    bookmarkId,
                    isArchived
                )
            } returns UpdateBookmarkUseCase.Result.GenericError(errorMessage)

            val bookmarkFlow = MutableStateFlow(bookmarks)
            coEvery {
                bookmarkRepository.observeBookmarkListItems(
                    type = null,
                    unread = null,
                    archived = null,
                    favorite = null,
                    state = Bookmark.State.LOADED
                )
            } returns bookmarkFlow

            viewModel = BookmarkListViewModel(
                updateBookmarkUseCase,
                workManager,
                bookmarkRepository,
                context,
                settingsDataStore,
                savedStateHandle
            )

            val uiStates = viewModel.uiState.take(2).toList()
            val emptyState = uiStates[0]
            val successState = uiStates[1]
            // Assert initial state
            assert(emptyState is BookmarkListViewModel.UiState.Empty)
            // Assert success state
            assertEquals(
                BookmarkListViewModel.UiState.Success(
                    bookmarks,
                    null
                ),
                successState
            )

            viewModel.onToggleArchiveBookmark(bookmarkId, isArchived)
            advanceUntilIdle()

            val errorState = viewModel.uiState.value

            assertEquals(
                BookmarkListViewModel.UiState.Success(
                    bookmarks,
                    BookmarkListViewModel.UpdateBookmarkState.Error(errorMessage)
                ),
                errorState
            )

            coVerify { updateBookmarkUseCase.updateIsArchived(bookmarkId, isArchived) }
        }

    @Test
    fun `onToggleArchivedBookmark updates UiState with UpdateBookmarkState Error on NetworkError`() =
        runTest {
            coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
            val bookmarkId = "123"
            val isArchived = true
            val errorMessage = "Network Error"

            val bookmarkFlow = MutableStateFlow(bookmarks)
            coEvery {
                bookmarkRepository.observeBookmarkListItems(
                    type = null,
                    unread = null,
                    archived = null,
                    favorite = null,
                    state = Bookmark.State.LOADED
                )
            } returns bookmarkFlow

            coEvery {
                updateBookmarkUseCase.updateIsArchived(
                    bookmarkId,
                    isArchived
                )
            } returns UpdateBookmarkUseCase.Result.NetworkError(errorMessage)

            viewModel = BookmarkListViewModel(
                updateBookmarkUseCase,
                workManager,
                bookmarkRepository,
                context,
                settingsDataStore,
                savedStateHandle
            )

            val uiStates = viewModel.uiState.take(2).toList()
            val emptyState = uiStates[0]
            val successState = uiStates[1]
            // Assert initial state
            assert(emptyState is BookmarkListViewModel.UiState.Empty)
            // Assert success state
            assertEquals(
                BookmarkListViewModel.UiState.Success(
                    bookmarks,
                    null
                ),
                successState
            )

            viewModel.onToggleArchiveBookmark(bookmarkId, isArchived)
            advanceUntilIdle()

            val errorState = viewModel.uiState.value

            assertEquals(
                BookmarkListViewModel.UiState.Success(
                    bookmarks,
                    BookmarkListViewModel.UpdateBookmarkState.Error(errorMessage)
                ),
                errorState
            )

            coVerify { updateBookmarkUseCase.updateIsArchived(bookmarkId, isArchived) }
        }


    @Test
    fun `onToggleMarkReadBookmark updates UiState with UpdateBookmarkState Success`() =
        runTest {
            coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
            val bookmarkId = "123"
            val isRead = true

            val bookmarkFlow = MutableStateFlow(bookmarks)
            coEvery {
                bookmarkRepository.observeBookmarkListItems(
                    type = null,
                    unread = null,
                    archived = null,
                    favorite = null,
                    state = Bookmark.State.LOADED
                )
            } returns bookmarkFlow

            coEvery {
                updateBookmarkUseCase.updateIsRead(
                    bookmarkId,
                    isRead
                )
            } returns UpdateBookmarkUseCase.Result.Success

            viewModel = BookmarkListViewModel(
                updateBookmarkUseCase,
                workManager,
                bookmarkRepository,
                context,
                settingsDataStore,
                savedStateHandle
            )

            val uiStates = viewModel.uiState.take(2).toList()
            val emptyState = uiStates[0]
            val successState = uiStates[1]
            // Assert initial state
            assert(emptyState is BookmarkListViewModel.UiState.Empty)
            // Assert success state
            assertEquals(
                BookmarkListViewModel.UiState.Success(
                    bookmarks,
                    null
                ),
                successState
            )

            viewModel.onToggleMarkReadBookmark(bookmarkId, isRead)
            advanceUntilIdle()

            val updateState = viewModel.uiState.value

            assertEquals(
                BookmarkListViewModel.UiState.Success(
                    bookmarks,
                    BookmarkListViewModel.UpdateBookmarkState.Success
                ),
                updateState
            )

            coVerify { updateBookmarkUseCase.updateIsRead(bookmarkId, isRead) }
        }

    @Test
    fun `onToggleMarkReadBookmark updates UiState with UpdateBookmarkState Error on GenericError`() =
        runTest {
            coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
            val bookmarkId = "123"
            val isRead = true
            val errorMessage = "Generic Error"

            coEvery {
                updateBookmarkUseCase.updateIsRead(
                    bookmarkId,
                    isRead
                )
            } returns UpdateBookmarkUseCase.Result.GenericError(errorMessage)

            val bookmarkFlow = MutableStateFlow(bookmarks)
            coEvery {
                bookmarkRepository.observeBookmarkListItems(
                    type = null,
                    unread = null,
                    archived = null,
                    favorite = null,
                    state = Bookmark.State.LOADED
                )
            } returns bookmarkFlow

            viewModel = BookmarkListViewModel(
                updateBookmarkUseCase,
                workManager,
                bookmarkRepository,
                context,
                settingsDataStore,
                savedStateHandle
            )

            val uiStates = viewModel.uiState.take(2).toList()
            val emptyState = uiStates[0]
            val successState = uiStates[1]
            // Assert initial state
            assert(emptyState is BookmarkListViewModel.UiState.Empty)
            // Assert success state
            assertEquals(
                BookmarkListViewModel.UiState.Success(
                    bookmarks,
                    null
                ),
                successState
            )

            viewModel.onToggleMarkReadBookmark(bookmarkId, isRead)
            advanceUntilIdle()

            val errorState = viewModel.uiState.value

            assertEquals(
                BookmarkListViewModel.UiState.Success(
                    bookmarks,
                    BookmarkListViewModel.UpdateBookmarkState.Error(errorMessage)
                ),
                errorState
            )

            coVerify { updateBookmarkUseCase.updateIsRead(bookmarkId, isRead) }
        }

    @Test
    fun `onToggleMarkReadBookmark updates UiState with UpdateBookmarkState Error on NetworkError`() =
        runTest {
            coEvery { settingsDataStore.isInitialSyncPerformed() } returns false
            val bookmarkId = "123"
            val isRead = true
            val errorMessage = "Network Error"

            val bookmarkFlow = MutableStateFlow(bookmarks)
            coEvery {
                bookmarkRepository.observeBookmarkListItems(
                    type = null,
                    unread = null,
                    archived = null,
                    favorite = null,
                    state = Bookmark.State.LOADED
                )
            } returns bookmarkFlow

            coEvery {
                updateBookmarkUseCase.updateIsRead(
                    bookmarkId,
                    isRead
                )
            } returns UpdateBookmarkUseCase.Result.NetworkError(errorMessage)

            viewModel = BookmarkListViewModel(
                updateBookmarkUseCase,
                workManager,
                bookmarkRepository,
                context,
                settingsDataStore,
                savedStateHandle
            )

            val uiStates = viewModel.uiState.take(2).toList()
            val emptyState = uiStates[0]
            val successState = uiStates[1]
            // Assert initial state
            assert(emptyState is BookmarkListViewModel.UiState.Empty)
            // Assert success state
            assertEquals(
                BookmarkListViewModel.UiState.Success(
                    bookmarks,
                    null
                ),
                successState
            )

            viewModel.onToggleMarkReadBookmark(bookmarkId, isRead)
            advanceUntilIdle()

            val errorState = viewModel.uiState.value

            assertEquals(
                BookmarkListViewModel.UiState.Success(
                    bookmarks,
                    BookmarkListViewModel.UpdateBookmarkState.Error(errorMessage)
                ),
                errorState
            )

            coVerify { updateBookmarkUseCase.updateIsRead(bookmarkId, isRead) }
        }
    private val bookmarks = listOf(
        BookmarkListItem(
            id = "1",
            url = "https://example.com",
            title = "Test Bookmark",
            siteName = "Example Site",
            type = Bookmark.Type.Article,
            isMarked = false,
            isArchived = false,
            labels = emptyList(),
            isRead = false,
            thumbnailSrc = "",
            iconSrc = "",
            imageSrc = ""
        )
    )
}
