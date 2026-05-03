package de.readeckapp.ui.settings

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.readeckapp.R
import de.readeckapp.domain.model.DefaultFilter
import de.readeckapp.domain.model.Theme
import de.readeckapp.io.prefs.SettingsDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalPermissionsApi::class)
@HiltViewModel
class UiSettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent.asStateFlow()
    private val theme = MutableStateFlow(Theme.SYSTEM)
    private val scrollToProgressEnabled = MutableStateFlow(true)
    private val showDialog = MutableStateFlow(false)
    private val defaultFilter = MutableStateFlow(DefaultFilter.ALL)
    private val showDefaultFilterDialog = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            theme.value = settingsDataStore.getTheme()
            scrollToProgressEnabled.value = settingsDataStore.isScrollToProgressEnabled()
            defaultFilter.value = settingsDataStore.getDefaultFilter()
        }
    }


    val uiState = combine(theme, scrollToProgressEnabled, showDialog, defaultFilter, showDefaultFilterDialog) { theme, scrollToProgressEnabled, showDialog, defaultFilter, showDefaultFilterDialog ->
        UiSettingsUiState(
            theme = theme,
            scrollToProgressEnabled = scrollToProgressEnabled,
            themeOptions = getThemeOptionList(theme),
            showDialog = showDialog,
            themeLabel = theme.toLabelResource(),
            defaultFilter = defaultFilter,
            defaultFilterLabel = defaultFilter.toLabelResource(),
            defaultFilterOptions = getDefaultFilterOptionList(defaultFilter),
            showDefaultFilterDialog = showDefaultFilterDialog,
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue =
                UiSettingsUiState(
                    theme = Theme.SYSTEM,
                    scrollToProgressEnabled = true,
                    themeOptions = getThemeOptionList(Theme.SYSTEM),
                    showDialog = false,
                    themeLabel = Theme.SYSTEM.toLabelResource(),
                    defaultFilter = DefaultFilter.ALL,
                    defaultFilterLabel = DefaultFilter.ALL.toLabelResource(),
                    defaultFilterOptions = getDefaultFilterOptionList(DefaultFilter.ALL),
                    showDefaultFilterDialog = false,
                )
        )

    fun onScrollToProgressToggle(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setScrollToProgressEnabled(enabled)
            scrollToProgressEnabled.value = settingsDataStore.isScrollToProgressEnabled()
        }
    }

    fun onNavigationEventConsumed() {
        _navigationEvent.update { null } // Reset the event
    }

    fun onClickTheme() {
        showDialog.value = true
    }

    fun onDismissDialog() {
        showDialog.value = false
    }

    fun onThemeSelected(selected: Theme) {
        Timber.d("onThemeSyncTimeframeSelected [selected=$selected]")
        updateTheme(selected)
    }

    fun onClickBack() {
        _navigationEvent.update { NavigationEvent.NavigateBack }
    }

    fun onClickDefaultFilter() {
        showDefaultFilterDialog.value = true
    }

    fun onDismissDefaultFilterDialog() {
        showDefaultFilterDialog.value = false
    }

    fun onDefaultFilterSelected(selected: DefaultFilter) {
        Timber.d("onDefaultFilterSelected [selected=$selected]")
        updateDefaultFilter(selected)
    }

    sealed class NavigationEvent {
        data object NavigateBack : NavigationEvent()
    }

    private fun getThemeOptionList(selected: Theme): List<SelectableOption<Theme>> {
        return Theme.entries.map {
            SelectableOption(
                value = it,
                label = it.toLabelResource(),
                selected = it == selected
            )
        }
    }

    private fun getDefaultFilterOptionList(selected: DefaultFilter): List<SelectableOption<DefaultFilter>> {
        return DefaultFilter.entries.map {
            SelectableOption(
                value = it,
                label = it.toLabelResource(),
                selected = it == selected
            )
        }
    }

    private fun updateTheme(value: Theme) {
        viewModelScope.launch {
            settingsDataStore.saveTheme(value)
            theme.value = settingsDataStore.getTheme()
        }
    }

    private fun updateDefaultFilter(value: DefaultFilter) {
        viewModelScope.launch {
            settingsDataStore.saveDefaultFilter(value)
            defaultFilter.value = settingsDataStore.getDefaultFilter()
        }
    }
}

@Immutable
data class UiSettingsUiState(
    val theme: Theme,
    val scrollToProgressEnabled: Boolean,
    val themeOptions: List<SelectableOption<Theme>>,
    val showDialog: Boolean,
    @StringRes
    val themeLabel: Int,
    val defaultFilter: DefaultFilter,
    @StringRes
    val defaultFilterLabel: Int,
    val defaultFilterOptions: List<SelectableOption<DefaultFilter>>,
    val showDefaultFilterDialog: Boolean,
)

@StringRes
fun Theme.toLabelResource(): Int {
    return when (this) {
        Theme.LIGHT -> R.string.theme_light
        Theme.DARK -> R.string.theme_dark
        Theme.SEPIA -> R.string.theme_sepia
        Theme.SYSTEM -> R.string.theme_system
    }
}

@StringRes
fun DefaultFilter.toLabelResource(): Int {
    return when (this) {
        DefaultFilter.ALL -> R.string.default_filter_all
        DefaultFilter.UNREAD -> R.string.default_filter_unread
        DefaultFilter.ARCHIVED -> R.string.default_filter_archived
        DefaultFilter.FAVORITES -> R.string.default_filter_favorites
    }
}
