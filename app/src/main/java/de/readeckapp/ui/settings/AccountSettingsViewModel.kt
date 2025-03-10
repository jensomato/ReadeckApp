package de.readeckapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.readeckapp.R
import de.readeckapp.domain.usecase.AuthenticateUseCase
import de.readeckapp.domain.usecase.AuthenticationResult
import de.readeckapp.io.prefs.SettingsDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.MalformedURLException
import java.net.URL
import javax.inject.Inject

@HiltViewModel
class AccountSettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val authenticateUseCase: AuthenticateUseCase
) : ViewModel() {
    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent.asStateFlow()
    private val _uiState =
        MutableStateFlow(AccountSettingsUiState("", "", "", false, null, null, null, null))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = AccountSettingsUiState(
                url = settingsDataStore.urlFlow.value,
                username = settingsDataStore.usernameFlow.value,
                password = settingsDataStore.passwordFlow.value,
                loginEnabled = false,
                urlError = null,
                usernameError = null,
                passwordError = null,
                authenticationResult = null
            )
        }
    }

    fun login() {
        viewModelScope.launch {
            val result = authenticateUseCase.execute(
                _uiState.value.url!!,
                _uiState.value.username!!,
                _uiState.value.password!!
            )
            _uiState.update {
                it.copy(authenticationResult = result)
            }
            Timber.d("result=$result")
        }
    }

    private fun validateUrl(url: String): Boolean {
        return try {
            URL(url)
            true
        } catch (e: MalformedURLException) {
            false
        }
    }

    fun onUrlChanged(value: String) {
        val isValidUrl = validateUrl(value)
        val urlError = if (!isValidUrl && value.isNotEmpty()) {
            R.string.account_settings_url_error // Use resource ID
        } else {
            null
        }
        _uiState.update {
            it.copy(
                url = value,
                urlError = urlError,
                loginEnabled = isValidUrl && !it.username.isNullOrBlank() && !it.password.isNullOrBlank(),
                authenticationResult = null // Clear any previous result
            )
        }
    }

    fun onUsernameChanged(value: String) {
        val usernameError = if (value.isBlank()) {
            R.string.account_settings_username_error // Use resource ID
        } else {
            null
        }
        _uiState.update {
            it.copy(
                username = value,
                usernameError = usernameError,
                loginEnabled = validateUrl(uiState.value.url ?: "") && !value.isBlank() && !it.password.isNullOrBlank(),
                authenticationResult = null // Clear any previous result
            )
        }
    }

    fun onPasswordChanged(value: String) {
        val passwordError = if (value.isBlank()) {
            R.string.account_settings_password_error // Use resource ID
        } else {
            null
        }
        _uiState.update {
            it.copy(
                password = value,
                passwordError = passwordError,
                loginEnabled = validateUrl(uiState.value.url ?: "") && !it.username.isNullOrBlank() && !value.isBlank(),
                authenticationResult = null // Clear any previous result
            )
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
    val loginEnabled: Boolean,
    val urlError: Int?,
    val usernameError: Int?,
    val passwordError: Int?,
    val authenticationResult: AuthenticationResult?
)
