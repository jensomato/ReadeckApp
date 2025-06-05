package de.readeckapp.ui.login

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.readeckapp.domain.usecase.AuthenticateUseCase
import de.readeckapp.domain.usecase.AuthenticationResult
import de.readeckapp.io.prefs.SettingsDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LoginScreenViewModel @Inject constructor (
    private val settingsDataStore: SettingsDataStore,
    private val authenticateUseCase: AuthenticateUseCase

) : ViewModel() {

    /**
     * Declare UI State
     */
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onUrlChanged(url: String) {
        _uiState.update {
            it.copy(url = url)
        }
    }

    fun onUsernameChanged(username: String) {
        _uiState.update {
            it.copy(username = username)
        }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update {
            it.copy(password = password)
        }
    }

    fun onToggleShowPassword() {
        _uiState.update {
            it.copy(showPassword = !it.showPassword)
        }
    }

    fun onToggleAllowUnencryptedConnection() {
        _uiState.update {
            it.copy(allowUnencryptedConnection = !it.allowUnencryptedConnection)
        }
    }

}

data class LoginUiState(
    val url: String = "",
    val username: String = "",
    val password: String = "",
    val loginEnabled: Boolean = false,
    val urlError: Int? = null,
    val usernameError: Int? = null,
    val passwordError: Int? = null,
    val authenticationResult: AuthenticationResult? = null,
    val allowUnencryptedConnection: Boolean = false,
    val showPassword: Boolean = false,
)