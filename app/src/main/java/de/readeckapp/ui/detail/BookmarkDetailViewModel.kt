package de.readeckapp.ui.detail

import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.readeckapp.domain.BookmarkRepository
import de.readeckapp.domain.model.Template
import de.readeckapp.domain.model.Theme
import de.readeckapp.domain.usecase.UpdateBookmarkUseCase
import de.readeckapp.io.AssetLoader
import de.readeckapp.io.prefs.SettingsDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
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
import kotlin.io.encoding.ExperimentalEncodingApi

@HiltViewModel
class BookmarkDetailViewModel @Inject constructor(
    private val updateBookmarkUseCase: UpdateBookmarkUseCase,
    private val bookmarkRepository: BookmarkRepository,
    private val assetLoader: AssetLoader,
    private val settingsDataStore: SettingsDataStore,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent.asStateFlow()

    private val _openUrlEvent = MutableStateFlow<String>("")
    val openUrlEvent = _openUrlEvent.asStateFlow()

    private val _shareIntent = MutableStateFlow<Intent?>(null)
    val shareIntent: StateFlow<Intent?> = _shareIntent.asStateFlow()

    private val bookmarkId: String? = savedStateHandle["bookmarkId"]
    private val template: Flow<Template?> = settingsDataStore.themeFlow.map {
        it?.let {
            Theme.valueOf(it)
        } ?: Theme.SYSTEM
    }.map {
        when (it) {
            Theme.DARK -> assetLoader.loadAsset(Template.DARK_TEMPLATE_FILE)?.let { Template.SimpleTemplate(it) }
            Theme.LIGHT -> assetLoader.loadAsset(Template.LIGHT_TEMPLATE_FILE)?.let { Template.SimpleTemplate(it) }
            Theme.SYSTEM -> {
                val light = assetLoader.loadAsset(Template.LIGHT_TEMPLATE_FILE)
                val dark = assetLoader.loadAsset(Template.DARK_TEMPLATE_FILE)
                if (!light.isNullOrBlank() && !dark.isNullOrBlank()) {
                    Template.DynamicTemplate(light = light, dark = dark)
                } else null
            }
        }
    }
    private val updateState = MutableStateFlow<UpdateBookmarkState?>(null)

    @OptIn(ExperimentalEncodingApi::class)
    val uiState = combine(
        bookmarkRepository.observeBookmark(bookmarkId!!),
        updateState,
        template
    ) { bookmark, updateState, template ->
        if (bookmark == null) {
            Timber.e("Error loading bookmark [bookmarkId=$bookmarkId]")
            UiState.Error
        } else if (template == null) {
            Timber.e("Error loading template(s)")
            UiState.Error
        } else {
            UiState.Success(
                bookmark = Bookmark(
                    url = bookmark.url,
                    title = bookmark.title,
                    authors = bookmark.authors,
                    createdDate = formatLocalDateTimeWithDateFormat(bookmark.created),
                    bookmarkId = bookmarkId,
                    siteName = bookmark.siteName,
                    imgSrc = bookmark.image.src,
                    isFavorite = bookmark.isMarked,
                    isArchived = bookmark.isArchived,
                    isRead = bookmark.isRead(),
                    type = when (bookmark.type) {
                        is de.readeckapp.domain.model.Bookmark.Type.Article -> Bookmark.Type.ARTICLE
                        is de.readeckapp.domain.model.Bookmark.Type.Picture -> Bookmark.Type.PHOTO
                        is de.readeckapp.domain.model.Bookmark.Type.Video -> Bookmark.Type.VIDEO
                    },
                    articleContent = bookmark.articleContent
                ),
                updateBookmarkState = updateState,
                template = template
            )
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

    fun onClickShareBookmark(url: String) {

        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, url)
            type = "text/plain"
        }

        _shareIntent.value = intent
    }

    fun onShareIntentConsumed() {
        _shareIntent.value = null
    }

    fun deleteBookmark(bookmarkId: String) {
        viewModelScope.launch {
            val state = when (val result = updateBookmarkUseCase.deleteBookmark(bookmarkId)) {
                is UpdateBookmarkUseCase.Result.Success -> UpdateBookmarkState.Success
                is UpdateBookmarkUseCase.Result.GenericError -> UpdateBookmarkState.Error(result.message)
                is UpdateBookmarkUseCase.Result.NetworkError -> UpdateBookmarkState.Error(result.message)
            }
            if (state is UpdateBookmarkState.Success) {
                _navigationEvent.update { NavigationEvent.NavigateBack }
            }
            updateState.value = state
        }
    }

    fun onClickOpenUrl(url: String){
         _openUrlEvent.value = url
    }

    fun onClickBack() {
        _navigationEvent.update { NavigationEvent.NavigateBack }
    }

    fun onNavigationEventConsumed() {
        _navigationEvent.update { null } // Reset the event
    }

    fun onOpenUrlEventConsumed() {
        _openUrlEvent.value = ""
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
        data class Success(val bookmark: Bookmark, val updateBookmarkState: UpdateBookmarkState?, val template: Template) :
            UiState()

        data object Loading : UiState()
        data object Error : UiState()
    }

    data class Bookmark(
        val url: String,
        val title: String,
        val authors: List<String>,
        val createdDate: String,
        val bookmarkId: String,
        val siteName: String,
        val imgSrc: String,
        val isFavorite: Boolean,
        val isArchived: Boolean,
        val isRead: Boolean,
        val type: Type,
        val articleContent: String?
    ) {
        enum class Type {
            ARTICLE, PHOTO, VIDEO
        }
        fun getContent(template: Template, isDark: Boolean): String? {
            val htmlTemplate = when (template) {
                is Template.SimpleTemplate -> template.template
                is Template.DynamicTemplate -> {
                    if (isDark) {
                        template.dark
                    } else {
                        template.light
                    }
                }
            }
            return when (type) {
                Type.PHOTO -> {
                    htmlTemplate.replace("%s", """<img src="$imgSrc"/>""")
                }

                Type.VIDEO -> {
                    articleContent?.let {
                        htmlTemplate.replace("%s", it)
                    }
                }

                Type.ARTICLE -> {
                    articleContent?.let {
                        htmlTemplate.replace("%s", it)
                    }
                }
            }
        }
    }

    sealed class UpdateBookmarkState {
        data object Success : UpdateBookmarkState()
        data class Error(val message: String) : UpdateBookmarkState()
    }
}
