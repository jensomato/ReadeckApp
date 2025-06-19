package de.readeckapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.readeckapp.domain.usecase.LogoutUseCase
import de.readeckapp.io.prefs.SettingsDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountSettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {
    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent.asStateFlow()
    private val _uiState =
        MutableStateFlow(AccountSettingsUiState("", "", "", false, false))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val url = settingsDataStore.urlFlow.value
            val username = settingsDataStore.usernameFlow.value
            val password = settingsDataStore.passwordFlow.value
            _uiState.value = AccountSettingsUiState(
                url = url,
                username = username,
                password = password,
                allowUnencryptedConnection = false,
                useApiToken = false
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase.execute()
        }
    }

    fun onNavigationEventConsumed() {
        _navigationEvent.update { null } // Reset the event
    }

    fun onClickBack() {
        _navigationEvent.update { NavigationEvent.NavigateBack }
    }

    sealed class NavigationEvent {
        data object NavigateBack : NavigationEvent()
    }
}

data class AccountSettingsUiState(
    val url: String?,
    val username: String?,
    val password: String?,
    val allowUnencryptedConnection: Boolean = false,
    val useApiToken: Boolean = false
)
