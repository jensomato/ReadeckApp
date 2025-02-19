package de.readeckapp.domain.usecase

import de.readeckapp.io.prefs.SettingsDataStore
import de.readeckapp.io.rest.ReadeckApi
import de.readeckapp.io.rest.auth.AuthInterceptor
import de.readeckapp.io.rest.auth.TokenManager
import de.readeckapp.io.rest.model.AuthenticationRequestDto
import timber.log.Timber
import javax.inject.Inject

class AuthenticateUseCase @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val readeckApi: ReadeckApi,
    private val tokenManager: TokenManager
) {
    suspend fun execute(url: String, username: String, password: String): Boolean {
        settingsDataStore.saveUrl(url)
        val response = readeckApi.authenticate(AuthenticationRequestDto(username, password, "readeck-app"))
        if (response.isSuccessful) {
            val token = response.body()!!.token
            settingsDataStore.saveToken(token)
//            tokenManager.updateToken(token)
            Timber.d("token=$token")
            readeckApi.userprofile().apply {
                if (isSuccessful) {
                    settingsDataStore.saveToken(token)
                    settingsDataStore.saveUsername(username)
                    settingsDataStore.saveUrl(url)
                    return true
                }
            }
        }
        return false
    }
}
