package de.readeckapp.io.prefs

import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Instant

interface SettingsDataStore {
    val tokenFlow: StateFlow<String?>
    val usernameFlow: StateFlow<String?>
    val passwordFlow: StateFlow<String?>
    val urlFlow: StateFlow<String?>
    fun saveUsername(username: String)
    fun savePassword(password: String)
    fun saveToken(token: String)
    fun saveUrl(url: String)
    suspend fun saveLastBookmarkTimestamp(timestamp: Instant)
    suspend fun getLastBookmarkTimestamp(): Instant?
    suspend fun setInitialSyncPerformed(performed: Boolean)
    suspend fun isInitialSyncPerformed(): Boolean
    suspend fun clearCredentials()
    suspend fun saveCredentials(url: String, username: String, password: String, token: String)
}
