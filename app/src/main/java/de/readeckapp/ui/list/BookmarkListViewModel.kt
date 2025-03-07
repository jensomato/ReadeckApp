package de.readeckapp.ui.list

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.readeckapp.domain.BookmarkRepository
import de.readeckapp.domain.model.Bookmark
import de.readeckapp.io.prefs.SettingsDataStore
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
    private val bookmarkRepository: BookmarkRepository,
    @ApplicationContext private val context: Context, // Inject Context
    private val settingsDataStore: SettingsDataStore // Inject SettingsDataStore
) : ViewModel() {
    val bookmarks = mutableStateOf<List<Bookmark>>(emptyList())
    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null) // Using StateFlow for navigation events
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent.asStateFlow()

    init {
        viewModelScope.launch {
            bookmarkRepository.getAllBookmarks().collectLatest {
                bookmarks.value = it
            }
            // Check if the initial sync has been performed
            if (!settingsDataStore.isInitialSyncPerformed()) {
                Timber.d("loadBookmarks")
                loadBookmarks() // Start incremental sync when the ViewModel is created
            }
        }
    }
    fun onClickAll() {
        Timber.d("onClickAll")
    }
    fun onClickUnread() {
        Timber.d("onClickUnread")
    }
    fun onClickArchive() {
        Timber.d("onClickArchive")
    }
    fun onClickFavorite() {
        Timber.d("onClickFavorite")
    }
    fun onClickArticles() {
        Timber.d("onClickArticles")
    }
    fun onClickPictures() {
        Timber.d("onClickPictures")
    }
    fun onClickVideos() {
        Timber.d("onClickVideos")
    }
    fun onClickCollections() {
        Timber.d("onClickCollections")
    }
    fun onClickLabels() {
        Timber.d("onClickLabels")
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

    fun onToggleFavoriteBookmark(bookmarkId: String) {
        Timber.d("onToggleFavoriteBookmark")
    }

    fun onToggleArchiveBookmark(bookmarkId: String) {
        Timber.d("onToggleArchiveBookmark")
    }

    sealed class NavigationEvent {
        data object NavigateToSettings: NavigationEvent()
        data class NavigateToBookmarkDetail(val bookmarkId: String): NavigationEvent()
    }
}
