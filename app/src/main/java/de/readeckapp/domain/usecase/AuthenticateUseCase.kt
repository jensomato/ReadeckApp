package de.readeckapp.domain.usecase

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import de.readeckapp.domain.UserRepository
import de.readeckapp.io.prefs.SettingsDataStore
import de.readeckapp.worker.LoadBookmarksWorker
import javax.inject.Inject

sealed class AuthenticationResult {
    data object Success : AuthenticationResult()
    data class AuthenticationFailed(val message: String) : AuthenticationResult()
    data class NetworkError(val message: String) : AuthenticationResult()
    data class GenericError(val message: String) : AuthenticationResult()
}

class AuthenticateUseCase @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val userRepository: UserRepository,
    @ApplicationContext private val context: Context // Need context to enqueue worker
) {
    suspend fun execute(url: String, username: String, password: String): AuthenticationResult {

        return when(val loginResult = userRepository.login(url, username, password)) {
            is UserRepository.LoginResult.Success -> {
                LoadBookmarksWorker.enqueue(context, isInitialLoad = true)
                settingsDataStore.setInitialSyncPerformed(true)
                AuthenticationResult.Success
            }
            is UserRepository.LoginResult.Error -> AuthenticationResult.GenericError(loginResult.errorMessage)
            is UserRepository.LoginResult.NetworkError -> AuthenticationResult.NetworkError(loginResult.errorMessage)
        }
    }
}
