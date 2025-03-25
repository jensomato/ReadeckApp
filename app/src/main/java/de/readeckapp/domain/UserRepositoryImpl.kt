package de.readeckapp.domain

import de.readeckapp.domain.model.AuthenticationDetails
import de.readeckapp.domain.model.User
import de.readeckapp.io.prefs.SettingsDataStore
import de.readeckapp.io.rest.ReadeckApi
import de.readeckapp.io.rest.model.AuthenticationRequestDto
import de.readeckapp.io.rest.model.StatusMessageDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val readeckApi: ReadeckApi,
    private val json: Json
) : UserRepository {
    override fun observeAuthenticationDetails(): Flow<AuthenticationDetails?> =
        combine(
            settingsDataStore.urlFlow,
            settingsDataStore.usernameFlow,
            settingsDataStore.passwordFlow,
            settingsDataStore.tokenFlow
        ) { url, username, password, token ->
            if (url != null && username != null && password != null && token != null) {
                AuthenticationDetails(url, username, password, token)
            } else {
                null
            }
        }.flowOn(Dispatchers.IO)

    override suspend fun login(
        url: String,
        username: String,
        password: String
    ): UserRepository.LoginResult {
        return withContext(Dispatchers.IO) {
            // save url early to allow call to authenticate endpoint
            settingsDataStore.saveUrl(url)
            try {
                val response = readeckApi.authenticate(
                    AuthenticationRequestDto(
                        username,
                        password,
                        "readeck-app"
                    )
                )
                if (response.isSuccessful && response.body() != null) {
                    response.body()?.let {
                        settingsDataStore.saveCredentials(url, username, password, it.token)
                        UserRepository.LoginResult.Success
                    } ?: UserRepository.LoginResult.Error("Empty response body")
                } else {
                    val errorBodyString = response.errorBody()?.string()
                    val errorState: StatusMessageDto = if (!errorBodyString.isNullOrBlank()) {
                        try {
                            json.decodeFromString<StatusMessageDto>(errorBodyString) // Use json.decodeFromString
                        } catch (e: SerializationException) { // Catch SerializationException
                            StatusMessageDto(
                                response.code(),
                                "Failed to parse error: ${e.message}"
                            )
                        }
                    } else {
                        StatusMessageDto(response.code(), "Empty error body")
                    }
                    UserRepository.LoginResult.Error(
                        errorMessage = errorState.message,
                        code = errorState.status
                    )
                }
            } catch (e: IOException) {
                UserRepository.LoginResult.Error("Network error: ${e.message}", ex = e)
            } catch (e: Exception) {
                UserRepository.LoginResult.Error(
                    "An unexpected error occurred: ${e.message}",
                    ex = e
                )
            }.also {
                if (it !is UserRepository.LoginResult.Success) {
                    // clear credentials in case of error
                    settingsDataStore.clearCredentials()
                    Timber.e("LoginResult is $it -> clearCredentials")
                }
            }
        }
    }

    override suspend fun login(url: String, appToken: String): UserRepository.LoginResult {
        TODO("Not yet implemented")
    }

    override suspend fun logout() {
        TODO("Not yet implemented")
    }

    override fun observeIsLoggedIn(): Flow<Boolean> = observeAuthenticationDetails().map {
        it != null
    }

    override fun observeUser(): Flow<User?> = observeAuthenticationDetails().map {
        if (it != null) {
            User(it.username)
        } else {
            null
        }
    }
}

