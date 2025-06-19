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
import kotlin.text.Regex

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

    fun onPasswordOrApiTokenChanged(password: String) {
        val passwordError = validatePasswordOrApiToken(password, uiState.value.useApiToken)

        _uiState.update {
            it.copy(passwordOrApiToken = password, passwordOrApiTokenError = passwordError)
        }
    }

    fun onToggleShowPasswordOrApiToken() {
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
                passwordOrApiToken = "",
                passwordOrApiTokenError = null
            )
        }
    }

    fun onAuthenticationResultConsumed() {
        _uiState.update {
            it.copy(authenticationResult = null)
        }
    }

    fun onClickLogin(){
        val urlError = validateUrl(uiState.value.url)
        val usernameError = validateUsername(uiState.value.username)
        val passwordOrApiTokenError = validatePasswordOrApiToken(uiState.value.passwordOrApiToken, uiState.value.useApiToken)

        val urlPrefix = if(uiState.value.useUnencryptedConnection) {
            "http://"
        } else "https://"

        val urlSuffix = if(!uiState.value.url.endsWith("/api")) {
            "/api"
        } else ""

        _uiState.update {
            it.copy(
                // Remove schema if user entered one...
                url = "${it.url}$urlSuffix".replace(Regex("^https?://", RegexOption.IGNORE_CASE), ""),
                urlError = urlError,
                usernameError = usernameError,
                passwordOrApiTokenError = passwordOrApiTokenError
            )
        }

        if (!uiState.value.loginEnabled) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true)
            }

            when(uiState.value.useApiToken) {
                true -> {
                    // TODO
                }
                false -> {
                    val result = authenticateUseCase.execute(
                        url = "$urlPrefix${uiState.value.url}",
                        username = uiState.value.username,
                        password = uiState.value.passwordOrApiToken
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
                                    usernameError = R.string.login_authentication_failed,
                                    passwordOrApiTokenError = R.string.login_authentication_failed,
                                    authenticationResult = result
                                )
                            }
                        }
                        is AuthenticationResult.NetworkError -> {
                            _uiState.update {
                                it.copy(
                                    urlError = R.string.login_network_error,
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
            R.string.login_url_empty_error
        } else if (!Patterns.WEB_URL.matcher(url).matches()) {
            R.string.login_url_invalid_error
        } else null
    }

    private fun validateUsername(username: String): Int? {
        return if(username.isBlank()) {
            R.string.login_username_empty_error
        } else null
    }

    private fun validatePasswordOrApiToken(password: String, useApiToken: Boolean): Int?{
        return if(password.isBlank()) {
            when(useApiToken){
                true -> R.string.login_apitoken_empty_error
                false -> R.string.login_password_empty_error
            }
        } else null
    }

}

data class LoginUiState(
    val url: String = "",
    val username: String = "",
    val passwordOrApiToken: String = "",
    val urlError: Int? = null,
    val usernameError: Int? = null,
    val passwordOrApiTokenError: Int? = null,
    val authenticationResult: AuthenticationResult? = null,
    val useApiToken: Boolean = false,
    val useUnencryptedConnection: Boolean = false,
    val showPassword: Boolean = false,
    val isLoading: Boolean = false
) {
    val loginEnabled: Boolean
        get() = url.isNotEmpty() && passwordOrApiToken.isNotEmpty() && urlError == null && passwordOrApiTokenError == null && (useApiToken || (username.isNotEmpty() && usernameError == null))
}