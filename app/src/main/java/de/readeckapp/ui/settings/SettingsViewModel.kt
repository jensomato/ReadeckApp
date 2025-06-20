package de.readeckapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.readeckapp.domain.UserRepository
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
    userRepository: UserRepository
) : ViewModel() {
    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent.asStateFlow()
    val uiState: StateFlow<SettingsUiState> = userRepository.observeAuthenticationDetails().map { SettingsUiState(username = it?.username) }.stateIn(
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

    fun onClickOpenSourceLibraries() {
        _navigationEvent.update { NavigationEvent.NavigateToOpenSourceLibraries }
    }

    fun onClickLogs() {
        _navigationEvent.update { NavigationEvent.NavigateToLogView }
    }

    fun onClickSync() {
        _navigationEvent.update { NavigationEvent.NavigateToSyncView }
    }

    fun onClickView() {
        _navigationEvent.update { NavigationEvent.NavigateToUiSettings }
    }

    sealed class NavigationEvent {
        data object NavigateToAccountSettings : NavigationEvent()
        data object NavigateToOpenSourceLibraries : NavigationEvent()
        data object NavigateBack : NavigationEvent()
        data object NavigateToLogView : NavigationEvent()
        data object NavigateToSyncView : NavigationEvent()
        data object NavigateToUiSettings : NavigationEvent()
    }

}

data class SettingsUiState(
    val username: String?,
)
