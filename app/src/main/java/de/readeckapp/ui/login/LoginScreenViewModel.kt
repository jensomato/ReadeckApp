package de.readeckapp.ui.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.readeckapp.R
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
        val urlError = validateUrl(url)

        _uiState.update {
            it.copy(url = url, urlError = urlError)
        }
    }

    fun onUsernameChanged(username: String) {
        val usernameError = validateUsername(username)

        _uiState.update {
            it.copy(username = username, usernameError = usernameError)
        }
    }

    fun onPasswordChanged(password: String) {
        val passwordError = validatePassword(password, _uiState.value.useApiToken)

        _uiState.update {
            it.copy(password = password, passwordError = passwordError)
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

    fun onToggleUseApiToken() {
        _uiState.update {
            val passwordError = validatePassword(it.password, !it.useApiToken)
            it.copy(useApiToken = !it.useApiToken, showPassword = !it.useApiToken, passwordError = passwordError)
        }
    }

    fun onClickLogin(){
        if(!_uiState.value.loginEnabled) {
            return
        }
    }

    // I intentionally don't use Util.isValidUrl because Util... checks for an URI (with schema) but I only need a URL (without schema)
    private fun validateUrl(url: String): Int? {
        return if(url.isBlank()) {
            R.string.account_settings_url_empty_error
        } else if (!Patterns.WEB_URL.matcher(url).matches()) {
            R.string.account_settings_url_error
        } else null
    }

    private fun validateUsername(username: String): Int? {
        return if(username.isBlank()) {
            R.string.account_settings_username_error
        } else null
    }

    private fun validatePassword(password: String, useApiToken: Boolean): Int?{
        return if(password.isBlank()) {
            when(useApiToken){
                true -> R.string.account_settings_apitoken_error
                false -> R.string.account_settings_password_error
            }
        } else null
    }

}

data class LoginUiState(
    val url: String = "",
    val username: String = "",
    val password: String = "",
    val urlError: Int? = null,
    val usernameError: Int? = null,
    val passwordError: Int? = null,
    val authenticationResult: AuthenticationResult? = null,
    val useApiToken: Boolean = false,
    val allowUnencryptedConnection: Boolean = false,
    val showPassword: Boolean = false,
) {
    val loginEnabled: Boolean
        get() = urlError == null && usernameError == null && passwordError == null
}