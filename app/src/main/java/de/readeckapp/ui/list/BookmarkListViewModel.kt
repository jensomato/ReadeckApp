package de.readeckapp.ui.list

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.readeckapp.R
import de.readeckapp.domain.BookmarkRepository
import de.readeckapp.domain.model.Bookmark
import de.readeckapp.domain.usecase.UpdateBookmarkUseCase
import de.readeckapp.io.prefs.SettingsDataStore
import de.readeckapp.util.isValidUrl
import de.readeckapp.worker.LoadBookmarksWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class BookmarkListViewModel @Inject constructor(
    private val updateBookmarkUseCase: UpdateBookmarkUseCase,
    private val bookmarkRepository: BookmarkRepository,
    @ApplicationContext private val context: Context, // Inject Context
    private val settingsDataStore: SettingsDataStore, // Inject SettingsDataStore
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _navigationEvent =
        MutableStateFlow<NavigationEvent?>(null) // Using StateFlow for navigation events
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent.asStateFlow()

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Add state for the CreateBookmarkDialog
    private val _createBookmarkUiState =
        MutableStateFlow<CreateBookmarkUiState>(CreateBookmarkUiState.Closed)
    val createBookmarkUiState: StateFlow<CreateBookmarkUiState> =
        _createBookmarkUiState.asStateFlow()

    init {
        val sharedUrl = savedStateHandle.get<String>("sharedUrl")

        if (sharedUrl != null) {
            val urlError = if (!sharedUrl.isValidUrl()) {
                R.string.account_settings_url_error // Use resource ID
            } else {
                null
            }
            _createBookmarkUiState.value = CreateBookmarkUiState.Open(
                title = "",
                url = sharedUrl,
                urlError = urlError,
                isCreateEnabled = sharedUrl.isValidUrl()
            )
        }

        viewModelScope.launch {
            filterState.collectLatest { filterState ->
                bookmarkRepository.observeBookmarks(
                    type = filterState.type,
                    unread = filterState.unread,
                    archived = filterState.archived,
                    favorite = filterState.favorite
                ).collectLatest {
                    _uiState.value = UiState.Success(bookmarks = it, updateBookmarkState = null)
                }
            }

            // Check if the initial sync has been performed
            if (!settingsDataStore.isInitialSyncPerformed()) {
                Timber.d("loadBookmarks")
                loadBookmarks() // Start incremental sync when the ViewModel is created
            }
        }
    }

    // Filter update functions
    private fun setTypeFilter(type: Bookmark.Type?) {
        _filterState.value = _filterState.value.copy(type = type)
    }

    private fun setUnreadFilter(unread: Boolean?) {
        _filterState.value =
            _filterState.value.copy(unread = unread, archived = null, favorite = null)
    }

    private fun setArchivedFilter(archived: Boolean?) {
        _filterState.value =
            _filterState.value.copy(archived = archived, unread = null, favorite = null)
    }

    private fun setFavoriteFilter(favorite: Boolean?) {
        _filterState.value =
            _filterState.value.copy(favorite = favorite, unread = null, archived = null)
    }

    // UI event handlers (already present, but need modification)
    fun onClickAll() {
        Timber.d("onClickAll")
        clearFilters()
    }

    private fun clearFilters() {
        _filterState.value = FilterState()
    }

    fun onClickUnread() {
        Timber.d("onClickUnread")
        setUnreadFilter(true)
    }

    fun onClickArchive() {
        Timber.d("onClickArchive")
        setArchivedFilter(true)
    }

    fun onClickFavorite() {
        Timber.d("onClickFavorite")
        setFavoriteFilter(true)
    }

    fun onClickArticles() {
        Timber.d("onClickArticles")
        setTypeFilter(Bookmark.Type.Article)
    }

    fun onClickPictures() {
        Timber.d("onClickPictures")
        setTypeFilter(Bookmark.Type.Picture)
    }

    fun onClickVideos() {
        Timber.d("onClickVideos")
        setTypeFilter(Bookmark.Type.Video)
    }

    fun onClickSettings() {
        Timber.d("onClickSettings")
        _navigationEvent.update { NavigationEvent.NavigateToSettings }
    }

    fun onClickBookmark(bookmarkId: String) {
        Timber.d("onClickSettings")
        _navigationEvent.update { NavigationEvent.NavigateToBookmarkDetail(bookmarkId) }
    }

    fun onNavigationEventConsumed() {
        _navigationEvent.update { null } // Reset the event
    }

    fun loadBookmarks() {
        viewModelScope.launch {
            try {
                LoadBookmarksWorker.enqueue(context) // Enqueue for incremental sync
            } catch (e: Exception) {
                // Handle errors (e.g., show error message)
                println("Error loading bookmarks: ${e.message}")
            }
        }
    }

    fun onDeleteBookmark(bookmarkId: String) {
        Timber.d("onDeleteBookmark")
    }

    fun onToggleMarkReadBookmark(bookmarkId: String) {
        Timber.d("onToggleMarkReadBookmark")
    }

    fun onToggleFavoriteBookmark(bookmarkId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            val state =
                when (val result = updateBookmarkUseCase.updateIsFavorite(
                    bookmarkId = bookmarkId,
                    isFavorite = isFavorite
                )) {
                    is UpdateBookmarkUseCase.Result.Success -> {
                        UpdateBookmarkState.Success
                    }

                    is UpdateBookmarkUseCase.Result.GenericError -> {
                        UpdateBookmarkState.Error(result.message)
                    }

                    is UpdateBookmarkUseCase.Result.NetworkError -> {
                        UpdateBookmarkState.Error(result.message)
                    }
                }
            _uiState.update {
                when (it) {
                    is UiState.Success -> it.copy(updateBookmarkState = state)
                    else -> it
                }
            }
        }
    }

    fun onToggleArchiveBookmark(bookmarkId: String, isArchived: Boolean) {
        viewModelScope.launch {
            val state = when (val result = updateBookmarkUseCase.updateIsArchived(
                bookmarkId = bookmarkId,
                isArchived = isArchived
            )) {
                is UpdateBookmarkUseCase.Result.Success -> {
                    UpdateBookmarkState.Success
                }

                is UpdateBookmarkUseCase.Result.GenericError -> {
                    UpdateBookmarkState.Error(result.message)
                }

                is UpdateBookmarkUseCase.Result.NetworkError -> {
                    UpdateBookmarkState.Error(result.message)
                }
            }
            _uiState.update {
                when (it) {
                    is UiState.Success -> it.copy(updateBookmarkState = state)
                    else -> it
                }
            }
        }
    }

    // Create Bookmark Dialog
    fun openCreateBookmarkDialog() {
        _createBookmarkUiState.value = CreateBookmarkUiState.Open(
            title = "",
            url = "",
            urlError = null,
            isCreateEnabled = false
        )
    }

    fun closeCreateBookmarkDialog() {
        _createBookmarkUiState.value = CreateBookmarkUiState.Closed
    }

    fun updateCreateBookmarkTitle(title: String) {
        _createBookmarkUiState.update {
            (it as? CreateBookmarkUiState.Open)?.copy(
                title = title,
                isCreateEnabled = it.url.isValidUrl()
            ) ?: it
        }
    }

    fun updateCreateBookmarkUrl(url: String) {
        val isValidUrl = url.isValidUrl()
        val urlError = if (!isValidUrl && url.isNotEmpty()) {
            R.string.account_settings_url_error // Use resource ID
        } else {
            null
        }
        _createBookmarkUiState.update {
            (it as? CreateBookmarkUiState.Open)?.copy(
                url = url,
                urlError = urlError,
                isCreateEnabled = isValidUrl
            ) ?: it
        }
    }

    fun createBookmark() {
        viewModelScope.launch {
            val url = (_createBookmarkUiState.value as CreateBookmarkUiState.Open).url
            val title = (_createBookmarkUiState.value as CreateBookmarkUiState.Open).title

            _createBookmarkUiState.value = CreateBookmarkUiState.Loading
            try {
                bookmarkRepository.createBookmark(title = title, url = url)
                _createBookmarkUiState.value = CreateBookmarkUiState.Success
            } catch (e: Exception) {
                _createBookmarkUiState.value =
                    CreateBookmarkUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    sealed class NavigationEvent {
        data object NavigateToSettings : NavigationEvent()
        data class NavigateToBookmarkDetail(val bookmarkId: String) : NavigationEvent()
    }

    data class FilterState(
        val type: Bookmark.Type? = null,
        val unread: Boolean? = null,
        val archived: Boolean? = null,
        val favorite: Boolean? = null
    )

    sealed class UiState {
        data class Success(
            val bookmarks: List<Bookmark>,
            val updateBookmarkState: UpdateBookmarkState?
        ) : UiState()

        data object Loading : UiState()
        data object Error : UiState()
    }

    sealed class CreateBookmarkUiState {
        data object Closed : CreateBookmarkUiState()
        data class Open(
            val title: String,
            val url: String,
            val urlError: Int?,
            val isCreateEnabled: Boolean
        ) : CreateBookmarkUiState()

        data object Loading : CreateBookmarkUiState()
        data object Success : CreateBookmarkUiState()
        data class Error(val message: String) : CreateBookmarkUiState()
    }

    sealed class UpdateBookmarkState {
        data object Success : UpdateBookmarkState()
        data class Error(val message: String) : UpdateBookmarkState()
    }
}
