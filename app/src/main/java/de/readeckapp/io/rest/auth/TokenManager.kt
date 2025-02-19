package de.readeckapp.io.rest.auth

import de.readeckapp.coroutine.ApplicationScope
import de.readeckapp.io.prefs.SettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationScope applicationScope: CoroutineScope,
    settingsDataStore: SettingsDataStore
) {
    init {
        applicationScope.launch {
            settingsDataStore.tokenFlow.collectLatest {
                token = it
            }
        }
    }

    @Volatile
    private var token: String? = null

    fun getToken(): String? = token
}