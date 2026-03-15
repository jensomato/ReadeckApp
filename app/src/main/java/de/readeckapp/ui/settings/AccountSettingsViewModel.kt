package de.readeckapp.ui.settings

import android.app.Application
import android.content.Intent
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.readeckapp.BuildConfig
import de.readeckapp.R
import de.readeckapp.domain.usecase.AuthenticateUseCase
import de.readeckapp.domain.usecase.AuthenticationResult
import de.readeckapp.io.prefs.SettingsDataStore
import de.readeckapp.io.rest.auth.UnencryptedConnectionBuilder
import de.readeckapp.util.isValidUrl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.openid.appauth.AppAuthConfiguration
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.RegistrationRequest
import net.openid.appauth.GrantTypeValues
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.connectivity.DefaultConnectionBuilder
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

@HiltViewModel
class AccountSettingsViewModel @Inject constructor(
    private val application: Application,
    private val settingsDataStore: SettingsDataStore,
    private val authenticateUseCase: AuthenticateUseCase,
) : ViewModel() {
    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent.asStateFlow()
    
    private val _oAuthIntentEvent = MutableStateFlow<Intent?>(null)
    val oAuthIntentEvent: StateFlow<Intent?> = _oAuthIntentEvent.asStateFlow()
    
    private val _uiState =
        MutableStateFlow(AccountSettingsUiState("", false, null, null, false))
    val uiState = _uiState.asStateFlow()

    private var authService: AuthorizationService? = null
    
    init {
        viewModelScope.launch {
            val isLoggedIn = settingsDataStore.authStateFlow.value != null
            val url = settingsDataStore.urlFlow.value ?: ""
            _uiState.value = AccountSettingsUiState(
                url = url,
                loginEnabled = isValidUrl(url),
                urlError = null,
                authenticationResult = null,
                allowUnencryptedConnection = false,
                isLoggedIn = isLoggedIn,
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        authService?.dispose()
    }

    private fun getAuthService(): AuthorizationService {
        if (authService == null) {
            val builder = AppAuthConfiguration.Builder()
            if (_uiState.value.allowUnencryptedConnection) {
                builder.setConnectionBuilder(UnencryptedConnectionBuilder)
            } else {
                builder.setConnectionBuilder(DefaultConnectionBuilder.INSTANCE)
            }
            authService = AuthorizationService(application, builder.build())
        }
        return authService!!
    }

    fun login() {
        viewModelScope.launch {
            val url = _uiState.value.url ?: return@launch

            // Temporarily update state to show loading
            _uiState.update { it.copy(loginEnabled = false, authenticationResult = null, isLoading = true) }

            try {
                // Save URL early so it's persisted and the DataStore flows get updated
                settingsDataStore.saveUrl(url)

                val baseUri = url.removeSuffix("/api")
                val authEndpoint = "$baseUri/authorize".toUri()
                val tokenEndpoint = "$baseUri/api/oauth/token".toUri()
                val registrationEndpoint = "$baseUri/api/oauth/client".toUri()
                val redirectUri = "${BuildConfig.APPLICATION_ID}://oauth2redirect".toUri()

                val serviceConfig = AuthorizationServiceConfiguration(
                    authEndpoint,
                    tokenEndpoint,
                    registrationEndpoint
                )

                // 1. Dynamic Client Registration via AppAuth
                val registrationRequest = RegistrationRequest.Builder(
                    serviceConfig,
                    listOf(redirectUri)
                )
                    .setGrantTypeValues(listOf(GrantTypeValues.AUTHORIZATION_CODE))
                    .setAdditionalParameters(
                        mapOf(
                            "client_name" to BuildConfig.APP_NAME,
                            "client_uri" to BuildConfig.APP_URL,
                            "software_id" to BuildConfig.APPLICATION_ID,
                            "software_version" to BuildConfig.VERSION_NAME,
                            "logo_uri" to "data:image/png;base64,${BuildConfig.APP_LOGO_BASE64}"
                        )
                    )
                    .build()

                val registrationResult = suspendCancellableCoroutine { continuation ->
                    getAuthService().performRegistrationRequest(registrationRequest) { response, ex ->
                        if (response != null) {
                            continuation.resume(Result.success(response))
                        } else {
                            continuation.resume(Result.failure(ex ?: Exception("Unknown error during registration")))
                        }
                    }
                }

                if (registrationResult.isFailure) {
                    val ex = registrationResult.exceptionOrNull()
                    _uiState.update {
                        it.copy(
                            authenticationResult = AuthenticationResult.GenericError("Client registration failed: ${ex?.message}"),
                            loginEnabled = true,
                            isLoading = false,
                        )
                    }
                    return@launch
                }

                val registrationResponse = registrationResult.getOrThrow()
                val clientId = registrationResponse.clientId

                // 2. Prepare Authorization Request
                val authRequest = AuthorizationRequest.Builder(
                    serviceConfig,
                    clientId,
                    ResponseTypeValues.CODE,
                    redirectUri
                ).setScope("bookmarks:read bookmarks:write profile:read").build()

                // 3. Emit Intent to UI
                val intent = getAuthService().getAuthorizationRequestIntent(authRequest)
                _oAuthIntentEvent.value = intent

            } catch (e: Exception) {
                Timber.e(e, "OAuth flow initiation failed")
                _uiState.update { 
                    it.copy(
                        authenticationResult = AuthenticationResult.NetworkError(e.message ?: "Unknown error"),
                        loginEnabled = true,
                        isLoading = false,
                    ) 
                }
            }
        }
    }

    fun handleOAuthResult(intent: Intent?) {
        _uiState.update { it.copy(loginEnabled = true, isLoading = false) } // re-enable login button

        if (intent == null) return

        val response = AuthorizationResponse.fromIntent(intent)
        val ex = AuthorizationException.fromIntent(intent)

        if (ex != null) {
            Timber.e(ex, "Authorization failed")
            _uiState.update { 
                it.copy(authenticationResult = AuthenticationResult.AuthenticationFailed(ex.errorDescription ?: ex.message ?: "Authorization failed"))
            }
            return
        }

        if (response != null) {
            viewModelScope.launch {
                exchangeToken(response)
            }
        }
    }

    private suspend fun exchangeToken(response: AuthorizationResponse) {
        val authState = AuthState(response, null)
        val tokenRequest = response.createTokenExchangeRequest()

        val tokenResult = suspendCancellableCoroutine { continuation ->
            getAuthService().performTokenRequest(tokenRequest) { tokenResponse, ex ->
                authState.update(tokenResponse, ex)
                if (tokenResponse != null) {
                    continuation.resume(Result.success(Pair(tokenResponse.accessToken, authState.jsonSerializeString())))
                } else {
                    continuation.resume(Result.failure(ex ?: Exception("Unknown error during token exchange")))
                }
            }
        }

        if (tokenResult.isSuccess) {
            val (accessToken, authStateJson) = tokenResult.getOrThrow()
            if (accessToken != null) {
                val url = _uiState.value.url!!
                val result = authenticateUseCase.execute(url, accessToken, authStateJson)
                _uiState.update { it.copy(authenticationResult = result) }
            } else {
                _uiState.update { 
                    it.copy(authenticationResult = AuthenticationResult.GenericError("Token is null")) 
                }
            }
        } else {
            val exception = tokenResult.exceptionOrNull()
            _uiState.update { 
                it.copy(authenticationResult = AuthenticationResult.AuthenticationFailed("Token exchange failed: ${exception?.message}")) 
            }
        }
    }

    fun onOAuthIntentConsumed() {
        _oAuthIntentEvent.value = null
    }

    fun onAllowUnencryptedConnectionChanged(allow: Boolean) {
        _uiState.update {
            it.copy(allowUnencryptedConnection = allow)
        }
        
        // Re-create auth service if connection builder preference changes
        authService?.dispose()
        authService = null
        
        uiState.value.url?.apply { validateUrl(this) }
    }

    fun onUrlChanged(value: String) {
        validateUrl(value)
    }

    private fun validateUrl(value: String) {
        val isValidUrl = isValidUrl(value)
        val urlError = if (!isValidUrl && value.isNotEmpty()) {
            R.string.account_settings_url_error
        } else {
            null
        }
        _uiState.update {
            it.copy(
                url = value,
                urlError = urlError,
                loginEnabled = isValidUrl,
                authenticationResult = null
            )
        }
    }

    fun onNavigationEventConsumed() {
        _navigationEvent.update { null }
    }

    fun onClickBack() {
        _navigationEvent.update { NavigationEvent.NavigateBack }
    }

    sealed class NavigationEvent {
        data object NavigateBack : NavigationEvent()
    }

    private fun isValidUrl(url: String?): Boolean {
        val allowUnencrypted = _uiState.value.allowUnencryptedConnection
        return if (allowUnencrypted) {
            url.isValidUrl()
        } else {
            url?.startsWith("https://") == true && url.isValidUrl()
        }
    }

}

data class AccountSettingsUiState(
    val url: String?,
    val loginEnabled: Boolean,
    val urlError: Int?,
    val authenticationResult: AuthenticationResult?,
    val allowUnencryptedConnection: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
)
