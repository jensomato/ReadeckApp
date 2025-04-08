package de.readeckapp.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.readeckapp.domain.BookmarkRepository
import de.readeckapp.domain.usecase.UpdateBookmarkUseCase
import de.readeckapp.io.AssetLoader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    private val updateBookmarkUseCase: UpdateBookmarkUseCase,
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
    private val updateState = MutableStateFlow<UpdateBookmarkState?>(null)

    @OptIn(ExperimentalEncodingApi::class)
    val uiState = combine(
        bookmarkRepository.observeBookmark(bookmarkId!!),
        htmlTemplate,
        updateState
    ) { bookmark, htmlTemplate, updateState ->
        if (htmlTemplate != null && bookmark.articleContent != null) {
            val content = htmlTemplate.replace("%s", bookmark.articleContent)
            val encodedHtml =
                Base64.withPadding(Base64.PaddingOption.ABSENT).encode(content.toByteArray())
            UiState.Success(
                bookmark = Bookmark(
                    title = bookmark.title,
                    encodedHtmlContent = encodedHtml,
                    authors = bookmark.authors,
                    createdDate = formatLocalDateTimeWithDateFormat(bookmark.created),
                    bookmarkId = bookmarkId,
                    siteName = bookmark.siteName,
                    imgSrc = bookmark.image.src,
                    htmlContent = content,
                    isFavorite = bookmark.isMarked,
                    isArchived = bookmark.isArchived,
                    isRead = bookmark.isRead()
                ),
                updateBookmarkState = updateState
            )
        } else {
            Timber.e("Error loading Article [bookmarkId=$bookmarkId, htmlTemplate=${htmlTemplate.isNullOrBlank()}, bookmarkArticle=${bookmark.articleContent?.isNotBlank()}")
            UiState.Error
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    fun onToggleFavorite(bookmarkId: String, isFavorite: Boolean) {
        updateBookmark {
            updateBookmarkUseCase.updateIsFavorite(
                bookmarkId = bookmarkId,
                isFavorite = isFavorite
            )
        }
    }

    fun onUpdateBookmarkStateConsumed() {
        updateState.value = null
    }

    fun onToggleArchive(bookmarkId: String, isArchived: Boolean) {
        updateBookmark {
            updateBookmarkUseCase.updateIsArchived(
                bookmarkId = bookmarkId,
                isArchived = isArchived
            )
        }
    }

    fun onToggleMarkRead(bookmarkId: String, isRead: Boolean) {
        updateBookmark {
            updateBookmarkUseCase.updateIsRead(
                bookmarkId = bookmarkId,
                isRead = isRead
            )
        }
    }

    private fun updateBookmark(update: suspend () -> UpdateBookmarkUseCase.Result) {
        viewModelScope.launch {
            val state = when (val result = update()) {
                is UpdateBookmarkUseCase.Result.Success -> UpdateBookmarkState.Success
                is UpdateBookmarkUseCase.Result.GenericError -> UpdateBookmarkState.Error(result.message)
                is UpdateBookmarkUseCase.Result.NetworkError -> UpdateBookmarkState.Error(result.message)
            }
            updateState.value = state
        }
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
        data class Success(val bookmark: Bookmark, val updateBookmarkState: UpdateBookmarkState?) :
            UiState()

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
        val htmlContent: String,
        val isFavorite: Boolean,
        val isArchived: Boolean,
        val isRead: Boolean
    )

    sealed class UpdateBookmarkState {
        data object Success : UpdateBookmarkState()
        data class Error(val message: String) : UpdateBookmarkState()
    }
}
