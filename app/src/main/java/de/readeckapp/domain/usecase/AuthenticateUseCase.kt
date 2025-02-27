package de.readeckapp.domain.usecase

import de.readeckapp.io.prefs.SettingsDataStore
import de.readeckapp.io.rest.ReadeckApi
import de.readeckapp.io.rest.auth.AuthInterceptor
import de.readeckapp.io.rest.auth.TokenManager
import de.readeckapp.io.rest.model.AuthenticationRequestDto
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

sealed class AuthenticationResult {
    data object Success : AuthenticationResult()
    data class AuthenticationFailed(val message: String) : AuthenticationResult()
    data class NetworkError(val message: String) : AuthenticationResult()
    data class GenericError(val message: String) : AuthenticationResult()
}

class AuthenticateUseCase @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val readeckApi: ReadeckApi,
    private val tokenManager: TokenManager
) {
    suspend fun execute(url: String, username: String, password: String): AuthenticationResult {
        settingsDataStore.saveUrl(url)
        try {
            val response = readeckApi.authenticate(AuthenticationRequestDto(username, password, "readeck-app"))
            if (response.isSuccessful) {
                val token = response.body()!!.token
                settingsDataStore.saveToken(token)
                Timber.d("token=$token")
                readeckApi.userprofile().apply {
                    if (isSuccessful) {
                        settingsDataStore.saveToken(token)
                        settingsDataStore.saveUsername(username)
                        settingsDataStore.saveUrl(url)
                        settingsDataStore.savePassword(password)
                        return AuthenticationResult.Success
                    } else {
                        return AuthenticationResult.GenericError("Failed to fetch user profile")
                    }
                }
            } else {
                return AuthenticationResult.AuthenticationFailed("Authentication failed: ${response.message()}")
            }
        } catch (e: IOException) {
            return AuthenticationResult.NetworkError("Network error: ${e.message}")
        } catch (e: Exception) {
            return AuthenticationResult.GenericError("An unexpected error occurred: ${e.message}")
        }
    }
}
