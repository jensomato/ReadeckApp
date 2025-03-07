package de.readeckapp.io.prefs

import kotlinx.coroutines.flow.StateFlow

interface SettingsDataStore {
    val tokenFlow: StateFlow<String?>
    val usernameFlow: StateFlow<String?>
    val passwordFlow: StateFlow<String?>
    val urlFlow: StateFlow<String?>
    fun saveUsername(username: String)
    fun savePassword(password: String)
    fun saveToken(token: String)
    fun saveUrl(url: String)
    suspend fun saveLastBookmarkTimestamp(timestamp: String)
    suspend fun getLastBookmarkTimestamp(): String?
    suspend fun setInitialSyncPerformed(performed: Boolean)
    suspend fun isInitialSyncPerformed(): Boolean
}
