package de.readeckapp.io.prefs

import de.readeckapp.domain.model.AutoSyncTimeframe
import de.readeckapp.domain.model.Theme
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Instant

interface SettingsDataStore {
    val tokenFlow: StateFlow<String?>
    val usernameFlow: StateFlow<String?>
    val authStateFlow: StateFlow<String?>
    val urlFlow: StateFlow<String?>
    val themeFlow: StateFlow<String?>
    val zoomFactorFlow: StateFlow<Int>

    fun saveUsername(username: String)
    fun saveToken(token: String)
    fun saveAuthState(authState: String)
    fun saveUrl(url: String)
    suspend fun saveLastBookmarkTimestamp(timestamp: Instant)
    suspend fun getLastBookmarkTimestamp(): Instant?
    suspend fun saveLastSyncTimestamp(timestamp: Instant)
    suspend fun getLastSyncTimestamp(): Instant?
    suspend fun setInitialSyncPerformed(performed: Boolean)
    suspend fun isInitialSyncPerformed(): Boolean
    suspend fun clearCredentials()
    suspend fun saveCredentials(url: String, username: String, token: String, authState: String)
    suspend fun setAutoSyncEnabled(isEnabled: Boolean)
    suspend fun isAutoSyncEnabled(): Boolean
    suspend fun saveAutoSyncTimeframe(autoSyncTimeframe: AutoSyncTimeframe)
    suspend fun getAutoSyncTimeframe(): AutoSyncTimeframe
    suspend fun isSyncReadProgressEnabled(): Boolean
    suspend fun setSyncReadProgressEnabled(enabled: Boolean)
    suspend fun isScrollToProgressEnabled(): Boolean
    suspend fun setScrollToProgressEnabled(enabled: Boolean)
    suspend fun saveTheme(theme: Theme)
    suspend fun getTheme(): Theme
    suspend fun  getZoomFactor(): Int
    suspend fun  saveZoomFactor(zoomFactor: Int)
}
