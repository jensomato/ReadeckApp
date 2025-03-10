package de.readeckapp.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.readeckapp.domain.BookmarkRepository
import de.readeckapp.io.AssetLoader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import timber.log.Timber
import java.text.DateFormat
import java.util.Date
import javax.inject.Inject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@HiltViewModel
class BookmarkDetailViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
    private val assetLoader: AssetLoader,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent.asStateFlow()
    private val bookmarkId: String? = savedStateHandle["bookmarkId"]
    private val htmlTemplate = flow {
        emit(assetLoader.loadAsset("html_template.html"))
    }

    @OptIn(ExperimentalEncodingApi::class)
    val uiState = combine(
        bookmarkRepository.observeBookmark(bookmarkId!!),
        htmlTemplate
    ) { bookmark, htmlTemplate ->
        if (htmlTemplate != null && bookmark.articleContent != null) {
            val content = htmlTemplate.replace("%s", bookmark.articleContent)
            val encodedHtml =
                Base64.withPadding(Base64.PaddingOption.ABSENT).encode(content.toByteArray())
            UiState.Success(
                Bookmark(
                    title = bookmark.title,
                    encodedHtmlContent = encodedHtml,
                    authors = bookmark.authors,
                    createdDate = formatLocalDateTimeWithDateFormat(bookmark.created),
                    bookmarkId = bookmarkId,
                    siteName = bookmark.siteName,
                    imgSrc = bookmark.image.src,
                    htmlContent = content
                )
            )
        } else {
            UiState.Error
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    fun toggleFavorite(bookmarkId: String) {
        // TODO: Implement favorite functionality
        Timber.d("Toggling favorite for $bookmarkId")
    }

    fun toggleArchive(bookmarkId: String) {
        // TODO: Implement archive functionality
        Timber.d("Toggling archive for $bookmarkId")
    }

    fun markRead(bookmarkId: String) {
        // TODO: Implement mark read functionality
        Timber.d("Marking read for $bookmarkId")
    }

    fun deleteBookmark(bookmarkId: String) =
        // TODO: Implement delete functionality
        Timber.d("Deleting bookmark $bookmarkId")

    fun onClickBack() {
        _navigationEvent.update { NavigationEvent.NavigateBack }
    }

    fun onNavigationEventConsumed() {
        _navigationEvent.update { null } // Reset the event
    }

    private fun formatLocalDateTimeWithDateFormat(localDateTime: LocalDateTime): String {
        val dateFormat = DateFormat.getDateInstance(
            DateFormat.MEDIUM
        )
        val timeZone = TimeZone.currentSystemDefault()
        val epochMillis = localDateTime.toInstant(timeZone).toEpochMilliseconds()
        return dateFormat.format(Date(epochMillis))
    }

    sealed class NavigationEvent {
        data object NavigateBack : NavigationEvent()
    }

    sealed class UiState {
        data class Success(val bookmark: Bookmark) : UiState()
        data object Loading : UiState()
        data object Error : UiState()
    }

    data class Bookmark(
        val title: String,
        val encodedHtmlContent: String,
        val authors: List<String>,
        val createdDate: String,
        val bookmarkId: String,
        val siteName: String,
        val imgSrc: String,
        val htmlContent: String
    )
}
