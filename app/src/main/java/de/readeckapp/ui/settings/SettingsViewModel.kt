package de.readeckapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.readeckapp.io.prefs.SettingsDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    settingsDataStore: SettingsDataStore
) : ViewModel() {
    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent.asStateFlow()
    val uiState: StateFlow<SettingsUiState> =
        settingsDataStore.usernameFlow.map { SettingsUiState(username = it) }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            SettingsUiState(username = null)
        )

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
