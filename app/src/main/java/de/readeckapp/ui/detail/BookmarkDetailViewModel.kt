package de.readeckapp.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.readeckapp.domain.BookmarkRepository
import de.readeckapp.domain.model.Bookmark
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class BookmarkDetailViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _bookmark = MutableStateFlow<Bookmark?>(null)
    val bookmark: StateFlow<Bookmark?> = _bookmark.asStateFlow()

    private val bookmarkId: String? = savedStateHandle["bookmarkId"]

    init {
        if (bookmarkId != null) {
            loadBookmark(bookmarkId)
        }
    }

    fun loadBookmark(id: String) {
        viewModelScope.launch {
            try {
                val bookmark = bookmarkRepository.getBookmarkById(id)
                _bookmark.value = bookmark
            } catch (e: Exception) {
                Timber.e(e, "Error loading bookmark")
                // Handle error (e.g., show error message)
            }
        }
    }

    fun toggleFavorite(bookmark: Bookmark) {
        // TODO: Implement favorite functionality
        Timber.d("Toggling favorite for ${bookmark.id}")
    }

    fun toggleArchive(bookmark: Bookmark) {
        // TODO: Implement archive functionality
        Timber.d("Toggling archive for ${bookmark.id}")
    }

    fun markRead(bookmark: Bookmark) {
        // TODO: Implement mark read functionality
        Timber.d("Marking read for ${bookmark.id}")
    }

    fun deleteBookmark(bookmark: Bookmark) {
        // TODO: Implement delete functionality
        Timber.d("Deleting bookmark ${bookmark.id}")
    }
}
