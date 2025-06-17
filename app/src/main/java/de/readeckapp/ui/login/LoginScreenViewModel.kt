package de.readeckapp.ui.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.readeckapp.R
import de.readeckapp.domain.usecase.AuthenticateUseCase
import de.readeckapp.domain.usecase.AuthenticationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginScreenViewModel @Inject constructor (
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

    fun onToggleUseUnencryptedConnection() {
        _uiState.update {
            it.copy(useUnencryptedConnection = !it.useUnencryptedConnection)
        }
    }

    fun onToggleUseApiToken() {
        _uiState.update {
            it.copy(
                useApiToken = !it.useApiToken,
                showPassword = !it.useApiToken,
                username = "",
                usernameError = null,
                password = "",
                passwordError = null
            )
        }
    }

    fun onAuthenticationResultConsumed() {
        _uiState.update {
            it.copy(authenticationResult = null)
        }
    }

    fun onClickLogin(){
        val urlError = validateUrl(_uiState.value.url)
        val usernameError = validateUsername(_uiState.value.username)
        val passwordError = validatePassword(_uiState.value.password, _uiState.value.useApiToken)

        _uiState.update {
            it.copy(urlError = urlError, usernameError = usernameError, passwordError = passwordError)
        }

        if (!_uiState.value.loginEnabled) return

        val urlPrefix = if(_uiState.value.useUnencryptedConnection) {
            "http://"
        } else "https://"

        _uiState.value.url.also { url ->
            if (!url.endsWith("/api")) {
                _uiState.update { it.copy(url = "$url/api") }
            }
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true)
            }

            when(_uiState.value.useApiToken) {
                true -> {
                    // TODO
                }
                false -> {
                    val result = authenticateUseCase.execute(
                        url = "$urlPrefix${_uiState.value.url}",
                        username = _uiState.value.username,
                        password = _uiState.value.password
                    )

                    when(result){
                        is AuthenticationResult.Success -> {
                            _uiState.update {
                                it.copy(authenticationResult = result)
                            }
                        }
                        is AuthenticationResult.AuthenticationFailed -> {
                            _uiState.update {
                                it.copy(
                                    usernameError = R.string.account_settings_authentication_failed,
                                    passwordError = R.string.account_settings_authentication_failed,
                                    authenticationResult = result
                                )
                            }
                        }
                        is AuthenticationResult.NetworkError -> {
                            _uiState.update {
                                it.copy(
                                    urlError = R.string.account_settings_network_error,
                                    authenticationResult = result
                                )
                            }
                        }
                        is AuthenticationResult.GenericError -> {
                            _uiState.update {
                                it.copy(
                                    authenticationResult = result
                                )
                            }
                        }
                    }

                    Timber.d("result=$result")
                }
            }
            _uiState.update {
                it.copy(isLoading = false)
            }
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
    val useUnencryptedConnection: Boolean = false,
    val showPassword: Boolean = false,
    val isLoading: Boolean = false
) {
    val loginEnabled: Boolean
        get() = urlError == null && passwordError == null && (usernameError == null || useApiToken)
}