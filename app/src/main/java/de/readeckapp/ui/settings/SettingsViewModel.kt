package de.readeckapp.ui.settings

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.readeckapp.domain.model.Bookmark
import de.readeckapp.io.prefs.SettingsDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {
    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent.asStateFlow()
    val bookmarks = mutableStateOf<List<Bookmark>>(emptyList())
    private val _uiState = MutableStateFlow(SettingsUiState(username = null))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = SettingsUiState(
                username = settingsDataStore.usernameFlow.value,
            )
        }
    }

    fun onNavigationEventConsumed() {
        _navigationEvent.update { null } // Reset the event
    }

    fun onClickAccount() {
        _navigationEvent.update { NavigationEvent.NavigateToAccountSettings }
    }

    fun onClickBack() {
        _navigationEvent.update { NavigationEvent.NavigateBack }
    }

    sealed class NavigationEvent {
        data object NavigateToAccountSettings : NavigationEvent()
        data object NavigateBack : NavigationEvent()
    }

}

data class SettingsUiState(
    val username: String?,
)
