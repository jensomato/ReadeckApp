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
    private val showDialog = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            theme.value = settingsDataStore.getTheme()
        }
    }


    val uiState = combine(theme, showDialog) { theme, showDialog ->
        UiSettingsUiState(
            theme = theme,
            themeOptions = getThemeOptionList(theme),
            showDialog = showDialog,
            themeLabel = theme.toLabelResource(),
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue =
                UiSettingsUiState(
                    theme = Theme.SYSTEM,
                    themeOptions = getThemeOptionList(Theme.SYSTEM),
                    showDialog = false,
                    themeLabel = Theme.SYSTEM.toLabelResource(),
                )
        )

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

    sealed class NavigationEvent {
        data object NavigateBack : NavigationEvent()
    }

    private fun getThemeOptionList(selected: Theme): List<ThemeOption> {
        return Theme.entries.map {
            ThemeOption(
                theme = it,
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
}

@Immutable
data class UiSettingsUiState(
    val theme: Theme,
    val themeOptions: List<ThemeOption>,
    val showDialog: Boolean,
    @StringRes
    val themeLabel: Int,
)

data class ThemeOption(
    val theme: Theme,
    @StringRes
    val label: Int,
    val selected: Boolean
)

@StringRes
fun Theme.toLabelResource(): Int {
    return when (this) {
        Theme.LIGHT -> R.string.theme_light
        Theme.DARK -> R.string.theme_dark
        Theme.SYSTEM -> R.string.theme_system
    }
}
